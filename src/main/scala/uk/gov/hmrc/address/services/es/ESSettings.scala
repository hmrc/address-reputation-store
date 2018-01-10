/*
 * Copyright 2018 HM Revenue & Customs
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

case class ESSettings(
                       bulkSize: Int,
                       loopDelay: Int,
                       includeDPA: Boolean,
                       includeLPI: Boolean,
                       prefer: String,
                       streetFilter: Int
                     // note that the completion date has a different lifecycle and is therefore not present here
                     ) {

  def tupled: List[(String, String)] =
    List(
      "bulkSize" -> bulkSize.toString,
      "loopDelay" -> loopDelay.toString,
      "includeDPA" -> includeDPA.toString,
      "includeLPI" -> includeLPI.toString,
      "prefer" -> prefer.toString,
      "streetFilter" -> streetFilter.toString)
}


object ESSettings {

  def apply(settings: Map[String, String]): ESSettings = {
    new ESSettings(
      settings("bulkSize").toInt,
      settings("loopDelay").toInt,
      settings("includeDPA").toBoolean,
      settings("includeLPI").toBoolean,
      settings("prefer"),
      settings("streetFilter").toInt
    )
  }
}
