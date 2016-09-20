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

import scala.annotation.tailrec

trait Document {
  def tupled: List[(String, Any)]

  def normalise: Document
}

/**
  * Address typically represents a postal address.
  * For UK addresses, 'town' will always be present.
  * For non-UK addresses, 'town' may be absent and there may be an extra line instead.
  */
// id typically consists of some prefix and the uprn
case class DbAddress(id: String, lines: List[String], town: Option[String], postcode: String, subdivision: Option[String], localCustodianCode: Option[Int]) extends Document {

  // UPRN is specified to be an integer of up to 12 digits (it can also be assumed to be always positive)
  def uprn: Long = DbAddress.trimLeadingLetters(id).toLong

  def linesContainIgnoreCase(filterStr: String): Boolean = {
    val filter = filterStr.toUpperCase
    lines.map(_.toUpperCase).exists(_.contains(filter))
  }

  def line1 = if (lines.nonEmpty) lines.head else ""

  def line2 = if (lines.size > 1) lines(1) else ""

  def line3 = if (lines.size > 2) lines(2) else ""

  // For use as input to MongoDbObject (hence it's not a Map)
  def tupled: List[(String, Any)] = List("_id" -> id, "lines" -> lines) ++
    town.map("town" -> _) ++
    List("postcode" -> postcode) ++
    subdivision.map("subdivision" -> _) ++
    localCustodianCode.map("localCustodianCode" -> _)

  def splitPostcode = Postcode(postcode)

  def normalise = this
}


object DbAddress {

  def apply(o: MongoDBObject): DbAddress = {
    val id = o.as[String]("_id")
    val town = if (o.containsField("town")) Some(o.as[String]("town")) else None
    val postcode = o.as[String]("postcode")
    val subdivision = if (o.containsField("subdivision")) Some(o.as[String]("subdivision")) else None
    val localCustodianCode = if (o.containsField("localCustodianCode")) Some(o.as[Int]("localCustodianCode")) else None
    val lines = o.as[List[String]]("lines")
    new DbAddress(id, lines, town, postcode, subdivision, localCustodianCode)
  }

  @tailrec
  private[osgb] def trimLeadingLetters(id: String): String = {
    if (id.isEmpty || Character.isDigit(id.head)) id
    else trimLeadingLetters(id.tail)
  }
}
