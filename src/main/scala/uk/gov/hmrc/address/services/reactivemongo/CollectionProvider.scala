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

package uk.gov.hmrc.address.services.reactivemongo

import java.time.Clock
import java.util.concurrent.atomic.AtomicReference

import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection
import uk.gov.hmrc.address.admin.StoredMetadataItem
import uk.gov.hmrc.logging.SimpleLogger


private case class CacheValue[T](value: T, expires: Long)


trait CollectionProvider {
  def collection: BSONCollection
}


class MongoDBCollectionProvider(db: DB, metadataItem: StoredMetadataItem) extends CollectionProvider {
  def collection: BSONCollection = db.collection[BSONCollection](metadataItem.get)
}


class CachedSwitchableCollection(real: CollectionProvider, lifetime: Long, clock: Clock, logger: SimpleLogger) extends CollectionProvider {

  private val cache = new AtomicReference[CacheValue[BSONCollection]](CacheValue(real.collection, clock.millis + lifetime))

  def collection: BSONCollection = {
    val v1 = cache.get
    if (clock.millis < v1.expires) v1.value
    else {
      val newCollection = real.collection
      if (newCollection.name == v1.value.name) v1.value
      else {
        val v2 = CacheValue(newCollection, clock.millis + lifetime)
        logger.info("Rotating collection from " + v1.value.name + " to " + v2.value.name)
        cache.set(v2)
        v2.value
      }
    }
  }
}

