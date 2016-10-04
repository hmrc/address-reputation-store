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

package uk.gov.hmrc.address.admin


trait StoredMetadataItem {
  /**
    * Gets the current value. Because of startup initialisation, there will always be such a value,
    * unless the database has been abnormally modified.
    */
  def get: String

  /**
    * Sets key/value item to a new value.
    */
  def set(value: String)

  /**
    * Reverts the key/value item to its initial hard-coded state.
    */
  def reset()
}


trait AtomicLock {
  /**
    * This key/value item can be used as a distributed atomic lock. The value should be something
    * unique, such as a GUID. Be sure to use unlock (or set/reset) later.
    *
    * @return true if the lock was acquired
    */
  def lock(value: String): Boolean

  /**
    * Unlocks the distributed lock, provided that the supplied value is the same as was
    * used when the lock was acquired.
    *
    * @return true if the lock was released
    */
  def unlock(value: String): Boolean

  /**
    * Tests whether the current value is a particular value.
    */
  def verify(value: String): Boolean

  /**
    * Reverts the key/value item to its initial hard-coded state.
    */
  def reset()
}
