/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.hmrc.address.admin

import com.mongodb.DuplicateKeyException
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports
import uk.co.hmrc.logging.SimpleLogger

class MongoStoredMetadataItem(collection: MongoCollection, itemKey: String, initialValue: String, logger: SimpleLogger)
  extends StoredMetadataItem with AtomicLock {

  initialise()

  def initialise() = {
    // Try to write an empty record, provided none exists. This means that
    // we don't need to do 'upsert' later, and this reduces the number of failed
    // write attempts that can occur later.
    val doc = MongoDBObject("_id" -> itemKey, "value" -> initialValue)
    try {
      val result = collection.insert(doc)
      val wroteExactly1 = result.wasAcknowledged() && result.getN == 1
      logOutcome("", wroteExactly1, result.isUpdateOfExisting)
    } catch {
      case dk: DuplicateKeyException =>
        logger.info(s"At startup, MongoDB.admin already contained $itemKey.")
    }
  }

  /**
    * Sets key/value item to a new value.
    */
  def set(newValue: String) {
    val filter = "_id" $eq itemKey
    val doc = MongoDBObject("_id" -> itemKey, "value" -> newValue)
    update(initialValue, filter, doc)
  }

  /**
    * Reverts the key/value item to its initial hard-coded state.
    */
  def reset() {
    val filter = "_id" $eq itemKey
    val doc = MongoDBObject("_id" -> itemKey, "value" -> initialValue)
    update(initialValue, filter, doc)
  }

  /**
    * Gets the current value. Because of startup initialisation, there will always be such a value,
    * unless the database has been abnormally modified.
    */
  def get: String = {
    val filter = "_id" $eq itemKey
    collection.findOne(filter).get("value").toString
  }

  /**
    * Tests whether the current value is a particular value.
    */
  def verify(value: String): Boolean = {
    val filter = $and("_id" $eq itemKey, "value" $eq value)
    collection.findOne(filter).isDefined
  }

  /**
    * This key/value item can be used as a distributed atomic lock. The value should be something
    * unique, such as a GUID. Be sure to use unlock (or set/reset) later.
    *
    * @return true if the lock was acquired
    */
  def lock(value: String): Boolean = {
    require(value != initialValue)
    atomicUpdate(initialValue, value)
  }

  /**
    * Unlocks the distributed lock, provided that the supplied value is the same as was
    * used when the lock was acquired.
    *
    * @return true if the lock was released
    */
  def unlock(value: String): Boolean = {
    require(value != initialValue)
    atomicUpdate(value, initialValue)
  }

  private def atomicUpdate(oldValue: String, newValue: String): Boolean = {
    val filter = $and("_id" $eq itemKey, "value" $eq oldValue)
    val doc = MongoDBObject("_id" -> itemKey, "value" -> newValue)
    update(newValue, filter, doc) == 1
  }

  private def update(newValue: String, filter: Imports.DBObject, doc: commons.Imports.DBObject): Int = {
    val result = collection.update(filter, doc, upsert = false, multi = false)
    val n = if (result.wasAcknowledged()) result.getN else 0
    logOutcome(newValue, n == 1, result.isUpdateOfExisting)
    n
  }

  private def logOutcome(value: String, wroteExactly1: Boolean, isUpdateOfExisting: Boolean) {
    val isUpdate = if (isUpdateOfExisting) "Updated" else "Inserted"
    if (wroteExactly1) {
      logger.info(s"$isUpdate $itemKey='$value' to the admin store in Mongo.")
    } else {
      logger.info(s"Skipped saving $itemKey='$value' to the admin store.")
    }
  }
}
