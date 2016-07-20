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
import reactivemongo.bson.{BSONArray, BSONDocument, BSONString}
import uk.co.hmrc.helper.EmbeddedMongoSuite

class DbAddressIntegration extends FunSuite with EmbeddedMongoSuite {

  val a1 = DbAddress("GB47070784", "A1", "Line2", "Line3", "Tynemouth", "NE30 4HG", "GB-ENG")
  val a2 = DbAddress("GB47070785", "A2", "Line2", "Line3", "Tynemouth", "NE30 4HG", "GB-ENG")

  def casbahFixtures(m: DBObject*) = {
    val mongoConnection = casbahMongoConnection()
    val collection = mongoConnection.getConfiguredDb("address")
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

  test("write then read using Casbah - old representation using line1,line2,line3") {
    val tuples = List("_id" -> a1.id, "line1" -> a1.line1, "line2" -> a1.line2, "line3" -> a1.line3, "town" -> a1.town, "postcode" -> a1.postcode, "subdivision" -> a1.subdivision)
    val m1 = MongoDBObject(tuples)
    val collection = casbahFixtures(m1)

    assert(collection.size === 1)

    val r1 = collection.findOne(MongoDBObject("postcode" -> "NE30 4HG")).get
    assert(r1 === m1)

    assert(DbAddress(new MongoDBObject(r1)) === a1)
  }

  test("read (only) using ReactiveMongo") {
    val id = BSONString(a1.id)
    val lines = BSONArray(a1.lines.map(s => BSONString(s)))
    val town = BSONString(a1.town)
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision)
    val bson = BSONDocument("_id" -> id, "lines" -> lines, "town" -> town, "postcode" -> postcode, "subdivision" -> subdivision)
    val r = BSONDbAddress.read(bson)

    assert(r === a1)
  }

  test("read (only) using ReactiveMongo - old representation using line1,line2,line3 - full case") {
    val id = BSONString(a1.id)
    val line1 = BSONString(a1.line1)
    val line2 = BSONString(a1.line2)
    val line3 = BSONString(a1.line3)
    val town = BSONString(a1.town)
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision)
    val bson = BSONDocument("_id" -> id, "line1" -> line1, "line2" -> line2, "line3" -> line3, "town" -> town, "postcode" -> postcode, "subdivision" -> subdivision)
    val r = BSONDbAddress.read(bson)

    assert(r === a1)
  }

  test("read (only) using ReactiveMongo - old representation using line1,line2,line3 - empty case, prefix") {
    val uprn = BSONString("47070784")
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision)
    val bson = BSONDocument("uprn" -> uprn, "postcode" -> postcode, "subdivision" -> subdivision)
    val r = BSONDbAddress.read(bson)

    assert(r === new DbAddress("GB47070784", Nil, "", "NE30 4HG", "GB-ENG"))
  }

  test("read (only) using ReactiveMongo - old representation using line1,line2,line3 - empty case, no prefix") {
    val uprn = BSONString("GB47070784")
    val postcode = BSONString(a1.postcode)
    val subdivision = BSONString(a1.subdivision)
    val bson = BSONDocument("uprn" -> uprn, "postcode" -> postcode, "subdivision" -> subdivision)
    val r = BSONDbAddress.read(bson)

    assert(r === new DbAddress("GB47070784", Nil, "", "NE30 4HG", "GB-ENG"))
  }

}
