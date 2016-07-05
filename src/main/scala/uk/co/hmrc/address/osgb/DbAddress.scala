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

trait Document {
  def tupled: List[(String, Any)]

  def normalise: Document
}

// id typically consists of some prefix and the uprn
case class DbAddress(id: String, lines: List[String], town: String, postcode: String) extends Document {

  def linesContainIgnoreCase(filterStr: String): Boolean = {
    val filter = filterStr.toUpperCase
    lines.map(_.toUpperCase).exists(_.contains(filter))
  }

  def line1 = if (lines.nonEmpty) lines.head else ""

  def line2 = if (lines.size > 1) lines(1) else ""

  def line3 = if (lines.size > 2) lines(2) else ""

  // For use as input to MongoDbObject (hence it's not a Map)
  def tupled = List("_id" -> id, "lines" -> lines, "town" -> town, "postcode" -> postcode)

  // For use as input to CSV generation etc.
  // Could instead use `this.productIterator.map(_.toString).toSeq`, but this is simpler.
  def toSeq: Seq[String] = Seq(id, line1, line2, line3, town, postcode)

  def splitPostcode = Postcode(postcode)

  def normalise = this
}


object DbAddress {

  def apply(id: String, line1: String, line2: String, line3: String, town: String, postcode: String): DbAddress = {
    apply(id, List(line1, line2, line3).filterNot(_ == ""), town, postcode)
  }

  def apply(o: MongoDBObject): DbAddress = {
    val id = o.as[String]("_id")
    val town = o.as[String]("town")
    val postcode = o.as[String]("postcode")
    if (o.containsField("lines")) {
      val lines = o.as[List[String]]("lines")
      new DbAddress(id, lines, town, postcode)
    } else {
      // backward compatibility
      val line1 = o.as[String]("line1")
      val line2 = o.as[String]("line2")
      val line3 = o.as[String]("line3")
      DbAddress(id, line1, line2, line3, town, postcode)
    }
  }

}
