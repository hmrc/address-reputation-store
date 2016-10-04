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

package uk.gov.hmrc.address.osgb

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.FunSuite
import reactivemongo.bson.{BSONArray, BSONDocument, BSONInteger, BSONString}
import uk.gov.hmrc.helper.EmbeddedMongoSuite

class DbAddressIntegration extends FunSuite with EmbeddedMongoSuite {

  import DbAddress._

  val a1 = DbAddress("GB47070784", List("A1", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"),
    Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a1comp = DbAddress("GB47070784", List("A1", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"),
    Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), None)
  val a2 = DbAddress("GB47070785", List("A2", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"),
    Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a2comp = DbAddress("GB47070785", List("A2", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"),
    Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), None)

  def casbahFixtures(m: DBObject*) = {
    val collection = casbahMongoConnection.getConfiguredDb("address")
    collection.drop()

    for (x <- m) {
      collection.insert(x)
    }
    collection
  }

  test("write then read using Casbah") {
    val a1t = a1.tupled ++ List("_id" -> a1.id)
    val a2t = a2.tupled ++ List("_id" -> a2.id)
    val m1 = MongoDBObject(a1t)
    val m2 = MongoDBObject(a2t)
    val collection = casbahFixtures(m1, m2)

    assert(collection.size === 2)

    val list = collection.find(MongoDBObject("postcode" -> "NE30 4HG")).toList
    assert(list.size === 2)
    assert(list === List(m1, m2))

    assert(DbAddress(new MongoDBObject(list(0))) === a1comp)
    assert(DbAddress(new MongoDBObject(list(1))) === a2comp)
  }

  test("read (only) using ReactiveMongo - populated case") {
    val bson = BSONDocument(
      "_id" -> BSONString(a1.id),
      "lines" -> BSONArray(a1.lines.map(s => BSONString(s))),
      "town" -> BSONString(a1.town.get),
      "postcode" -> BSONString(a1.postcode),
      "subdivision" -> BSONString(a1.subdivision.get),
      "country" -> BSONString(a1.country.get),
      "localCustodianCode" -> BSONInteger(a1.localCustodianCode.get),
      "language" -> BSONString("en"),
      "blpuState" -> BSONInteger(a1.blpuState.get),
      "logicalState" -> BSONInteger(a1.logicalState.get),
      "streetClass" -> BSONInteger(a1.streetClass.get),
      "blpuClass" -> BSONString(a1.blpuClass.get))
    val r = BSONDbAddress.read(bson)

    assert(r === a1comp)
  }

  test("read (only) using ReactiveMongo - empty case") {
    val bson = BSONDocument(
      "_id" -> BSONString(a1.id),
      "lines" -> BSONArray(),
      "postcode" -> BSONString(a1.postcode))
    val r = BSONDbAddress.read(bson)

    assert(r === new DbAddress("GB47070784", Nil, None, "NE30 4HG", None, None, None, None, None, None, None, None, None))
  }

}
