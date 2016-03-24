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

import com.mongodb.casbah.Imports._

case class DbAddress(uprn: String, line1: String, line2: String, line3: String, town: String, postcode: String) {

  def line123Contains(filterStr: String): Boolean = {
    val filter = filterStr.toUpperCase
    line1.toUpperCase.contains(filter) ||
      line2.toUpperCase.contains(filter) ||
      line3.toUpperCase.contains(filter)
  }

  def asMongoDBObject = MongoDBObject("uprn" -> uprn, "line1" -> line1, "line2" -> line2, "line3" -> line3, "town" -> town, "postcode" -> postcode)
}

