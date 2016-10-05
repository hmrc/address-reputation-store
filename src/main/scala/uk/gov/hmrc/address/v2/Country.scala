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

package uk.gov.hmrc.address.v2

import uk.gov.hmrc.address.v1

/** Represents a country as per ISO3166. */
case class Country(
                    // ISO3166-1 or ISO3166-2 code, e.g. "GB" or "GB-ENG" (note that "GB" is the official
                    // code for UK although "UK" is a reserved synonym and may be used instead)
                    // See https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
                    // and https://en.wikipedia.org/wiki/ISO_3166-2:GB
                    code: String,
                    // The printable name for the country, e.g. "United Kingdom"
                    name: String) {

  def asV1 = v1.Country(code, name)
}


object Countries {
  // note that "GB" is the official ISO code for UK, although "UK" is a reserved synonym and is less confusing
  val UK = Country("UK", "United Kingdom")
  val GG = Country("GG", "Guernsey")
  val IM = Country("IM", "Isle of Man")
  val JE = Country("JE", "Jersey")

  val England = Country("GB-ENG", "England")
  val Scotland = Country("GB-SCO", "Scotland")
  val Wales = Country("GB-WLS", "Wales")
  val NorthernIreland = Country("GB-NIR", "Northern Ireland")

  // TODO this is not good enough - should consult a reference HMG-approved list of countries
  def find(code: String): Option[Country] =
    code match {
      case "UK" | "GB" => Some(UK) // special case provided for in ISO-3166
      case GG.code => Some(GG)
      case IM.code => Some(IM)
      case JE.code => Some(JE)

      case England.code => Some(England)
      case Scotland.code => Some(Scotland)
      case Wales.code => Some(Wales)
      case NorthernIreland.code => Some(NorthernIreland)

      case _ => None
    }

  def findByName(name: String): Option[Country] =
    name match {
      case UK.name => Some(UK)
      case GG.name => Some(GG)
      case IM.name => Some(IM)
      case JE.name => Some(JE)

      case England.name => Some(England)
      case Scotland.name => Some(Scotland)
      case Wales.name => Some(Wales)
      case NorthernIreland.name => Some(NorthernIreland)

      case _ => None
    }
}
