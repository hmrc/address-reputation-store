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

import org.scalatest.WordSpec
import uk.co.hmrc.helper.EmbeddedMongoSuite
import uk.co.hmrc.logging.StubLogger

class MetadataStoreTest extends WordSpec with EmbeddedMongoSuite {

  "StoredMetadataItem" when {
    "used as key/value store" should {
      """
           1. start up with the initial value
           2. allow the value to be changed
      """ in {
        val logger = new StubLogger(true)
        val mongoConnection = casbahMongoConnection()
        val collection = mongoConnection.getConfiguredDb("test")
        collection.drop()

        val keyValue = new MongoStoredMetadataItem(collection, "keyValue", "started", logger)
        assert(keyValue.get === "started")

        keyValue.set("foo")
        assert(keyValue.get === "foo")
        assert(keyValue.verify("foo") === true)
        assert(keyValue.verify("bar") === false)

        keyValue.set("foo")
        assert(keyValue.get === "foo")
        assert(keyValue.verify("foo") === true)
        assert(keyValue.verify("bar") === false)

        keyValue.set("bar")
        assert(keyValue.get === "bar")

        keyValue.reset()
        assert(keyValue.get === "started")
      }
    }

    "used as a distributed lock" should {
      """
           1. grab the lock
           2. disallow another attempt to grab the lock
           3. allow the lock to be released
      """ in {
        val logger = new StubLogger(true)
        val mongoConnection = casbahMongoConnection()
        val collection = mongoConnection.getConfiguredDb("test")
        collection.drop()

        val dLock = new MongoStoredMetadataItem(collection, "dLock", "started", logger)
        assert(dLock.get === "started")

        assert(dLock.lock("foo") === true)
        assert(dLock.get === "foo")
        assert(dLock.verify("foo") === true)
        assert(dLock.verify("bar") === false)

        assert(dLock.lock("bar") === false)
        assert(dLock.get === "foo")
        assert(dLock.verify("foo") === true)
        assert(dLock.verify("bar") === false)

        assert(dLock.unlock("foo") === true)
        assert(dLock.get === "started")
        assert(dLock.verify("foo") === false)
        assert(dLock.verify("bar") === false)
      }
    }
  }
}
