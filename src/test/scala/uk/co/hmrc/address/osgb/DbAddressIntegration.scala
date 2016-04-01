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

package uk.co.hmrc.address.osgb

import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.FunSuite
import uk.co.hmrc.helper.EmbeddedMongoSuite
import uk.co.hmrc.logging.StubLogger

class DbAddressIntegration extends FunSuite with EmbeddedMongoSuite {

  val a1 = DbAddress("GB47070784", "A1", "Line2", "Line3", "Tynemouth", "NE30 4HG")
  val a2 = DbAddress("GB47070785", "A2", "Line2", "Line3", "Tynemouth", "NE30 4HG")

  test("write then read using Casbah") {
    val logger = new StubLogger(true)
    val mongoConnection = casbahMongoConnection()
    val collection = mongoConnection.getConfiguredDb("address")
    collection.drop()

    val m1 = MongoDBObject(a1.tupled)
    val m2 = MongoDBObject(a2.tupled)
    collection.insert(m1)
    collection.insert(m2)

    assert(collection.size === 2)

    val list = collection.find(MongoDBObject("postcode" -> "NE30 4HG")).toList
    assert(list.size === 2)
    assert(list === List(m1, m2))

    assert(DbAddress(new MongoDBObject(list(0))) === a1)
    assert(DbAddress(new MongoDBObject(list(1))) === a2)
  }

}
