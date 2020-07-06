/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.address.services.es

import uk.gov.hmrc.util._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

trait IndexState {
  def productName: String
  def epoch: Option[Int]
  def timestamp: Option[String]
  def toPrefix: String
  def formattedName: String
}


case class IndexName(productName: String,
                     epoch: Option[Int],
                     timestamp: Option[String] = None) extends Ordered[IndexName] with IndexState {

  def toPrefix: String = s"${productName}_${epoch.get}"

  override lazy val formattedName: String =
    if (timestamp.isDefined) IndexName.format(productName, epoch.get, timestamp.get)
    else if (epoch.isDefined) toPrefix
    else productName

  override def toString: String = formattedName

  override def compare(that: IndexName): Int = this.formattedName compare that.formattedName
}


object IndexName {
  def apply(name: String): Option[IndexName] =
    if (name.isEmpty) None
    else {
      val parts = qsplit(name, '_')
      if (parts.size <= 3) doParseName(parts) else None
    }

  private def doParseName(parts: List[String]): Option[IndexName] = {
    try {
      val epoch = if (parts.size >= 2) Some(parts(1).toInt) else None
      val timestamp = if (parts.size >= 3) Some(parts(2)) else None
      Some(IndexName(parts.head, epoch, timestamp))
    } catch {
      case n: NumberFormatException => None
    }
  }

  def format(productName: String, epoch: Int, timestamp: String): String = "%s_%d_%s".format(productName, epoch, timestamp)

  def newTimestamp: String = timestampFormatter.print(new DateTime())

  // note: no underscores (would break our logic) and no dashes (they are problematic in Mongo)
  private val timestampFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm")
}
