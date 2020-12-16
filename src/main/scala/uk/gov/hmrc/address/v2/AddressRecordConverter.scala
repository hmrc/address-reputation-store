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

package uk.gov.hmrc.address.v2

import uk.gov.hmrc.address.osgb.DbAddress



case class ReferenceItem(code: Int, localCustodian: String, county: Option[String])


object AddressRecordConverter {

  def convert(d: DbAddress, refItem: Option[ReferenceItem], incMetadata: Boolean): AddressRecord = {
    // blank the county if it is the same name as the city
    val optCounty = if (refItem.isEmpty || refItem.get.county.isEmpty || d.town.contains(refItem.get.county.get)) None else refItem.get.county

    val optLC = refItem.map(it => LocalCustodian(it.code, it.localCustodian))

    val optSubdivision = d.subdivision.flatMap(code => Countries.find(code))

    val country = if (d.country.isDefined) Countries.find(d.country.get).getOrElse(Countries.UK) else Countries.UK

    val language = d.language.getOrElse(English)

    val location = d.location.map(latlong => Location(latlong).toSeq)

    val a = new Address(d.lines, d.town, optCounty, d.postcode, optSubdivision, country)

    val blpuState = if (incMetadata) d.blpuState.flatMap(BLPUStateHelper.codeToString) else None

    val logicalState = if (incMetadata) d.logicalState.flatMap(LogicalStateHelper.codeToString) else None

    val streetClassification = if (incMetadata) d.streetClass.flatMap(StreetClassificationHelper.codeToString) else None

    new AddressRecord(d.id, Some(d.uprn), a, language, optLC, location, blpuState, logicalState, streetClassification, d.administrativeArea)
  }

  final val English = "en"
}
