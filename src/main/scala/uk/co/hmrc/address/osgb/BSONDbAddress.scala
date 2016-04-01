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

import reactivemongo.bson.{BSONDocument, BSONDocumentReader}

object BSONDbAddress extends BSONDocumentReader[DbAddress] {

  def read(bson: BSONDocument): DbAddress = {
    val opt: Option[DbAddress] = for {
      uprn <- bson.getAs[String]("id")
      lines <- bson.getAs[List[String]]("lines")
      town <- bson.getAs[String]("town")
      postcode <- bson.getAs[String]("postcode")
    } yield new DbAddress(uprn, lines, town, postcode)

    opt.get // the address is required (or let throw an exception)
  }
}
