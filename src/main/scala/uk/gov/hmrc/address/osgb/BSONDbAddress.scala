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

import reactivemongo.bson.{BSONDocument, BSONDocumentReader}

object BSONDbAddress extends BSONDocumentReader[DbAddress] {

  def read(bson: BSONDocument): DbAddress = {
    val id = bson.getAs[String]("_id")
    val lines = bson.getAs[List[String]]("lines")
    val town = bson.getAs[String]("town")
    val postcode = bson.getAs[String]("postcode")
    val subdivision = bson.getAs[String]("subdivision")
    val country = bson.getAs[String]("country")
    val localCustodianCode = bson.getAs[Int]("localCustodianCode")
    val language = bson.getAs[String]("language")
    val blpuState = bson.getAs[Int]("blpuState")
    val logicalState = bson.getAs[Int]("logicalState")
    val streetClass = bson.getAs[Int]("streetClass")

    new DbAddress(id.get, lines.get, town, postcode.get, subdivision, country, localCustodianCode, language,
      blpuState, logicalState, streetClass, None, None)
  }
}
