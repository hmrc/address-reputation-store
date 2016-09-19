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

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.FunSuite
import reactivemongo.bson.{BSONArray, BSONDocument, BSONInteger, BSONString}
import uk.co.hmrc.helper.EmbeddedMongoSuite

class DbAddressIntegration extends FunSuite with EmbeddedMongoSuite {

  val a1 = DbAddress("GB47070784", "A1", "Line2", "Line3", Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some(1234))
  val a2 = DbAddress("GB47070785", "A2", "Line2", "Line3", Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some(1234))

  def casbahFixtures(m: DBObject*) = {
    val collection = casbahMongoConnection.getConfiguredDb("address")
    collection.drop()

    for (x <- m) {
      collection.insert(x)
    }
    collection
  }

  test("write then read using Casbah") {
    val m1 = MongoDBObject(a1.tupled)
    val m2 = MongoDBObject(a2.tupled)
    val collection = casbahFixtures(m1, m2)

    assert(collection.size === 2)

    val list = collection.find(MongoDBObject("postcode" -> "NE30 4HG")).toList
    assert(list.size === 2)
    assert(list === List(m1, m2))

    assert(DbAddress(new MongoDBObject(list(0))) === a1)
    assert(DbAddress(new MongoDBObject(list(1))) === a2)
  }

  test("read (only) using ReactiveMongo") {
    val id = BSONString(a1.id)
    val lines = BSONArray(a1.lines.map(s => BSONString(s)))
    val town = BSONString(a1.town.get)
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision.get)
    val localCustodianCode = BSONInteger(a1.localCustodianCode.get)
    val bson = BSONDocument("_id" -> id, "lines" -> lines, "town" -> town, "postcode" -> postcode, "subdivision" -> subdivision, "localCustodianCode" -> localCustodianCode)
    val r = BSONDbAddress.read(bson)

    assert(r === a1)
  }

  test("read (only) using ReactiveMongo - empty case, prefix") {
    val id = BSONString(a1.id)
    val lines = BSONArray()
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision.get)
    val localCustodianCode = BSONInteger(a1.localCustodianCode.get)
    val bson = BSONDocument("_id" -> id, "lines" -> lines, "postcode" -> postcode, "subdivision" -> subdivision, "localCustodianCode" -> localCustodianCode)
    val r = BSONDbAddress.read(bson)

    assert(r === new DbAddress("GB47070784", Nil, None, "NE30 4HG", Some("GB-ENG"), Some(1234)))
  }

  test("read (only) using ReactiveMongo - using lines - empty case, no prefix") {
    val id = BSONString(a1.id)
    val lines = BSONArray()
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision.get)
    val localCustodianCode = BSONInteger(a1.localCustodianCode.get)
    val bson = BSONDocument("_id" -> id, "lines"-> BSONArray(), "postcode" -> postcode, "subdivision" -> subdivision, "localCustodianCode" -> localCustodianCode)
    val r = BSONDbAddress.read(bson)

    assert(r === new DbAddress("GB47070784", Nil, None, "NE30 4HG", Some("GB-ENG"), Some(1234)


    ))
  }

}
