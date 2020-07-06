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

import org.scalatest.FunSuite
import uk.gov.hmrc.address.osgb.DbAddress

class AddressRecordConverterTest extends FunSuite {

  import Countries._

  private val en = Some("en")
  private val NewcastleUponTyne = Some("Newcastle upon Tyne")
  private val TyneAndWear = Some("Tyne & Wear")
  private val NE1_6JN = "NE1 6JN"

  // BLPU state
  private val InUse = Some(2)
  // Logical state
  private val Approved = Some(1)
  // Street Classification
  private val AllVehicles = Some(8)

  val lc4510 = LocalCustodian(4510, "Newcastle upon Tyne")

  val location = Location("54.9714759", "-1.6187319")

  val ne15xdD1 = DbAddress("GB4510123533", List("10 Taylors Court", "1 Monk Street"), NewcastleUponTyne, "NE1 5XD", Some("GB-ENG"), Some("UK"),
    Some(lc4510.code), en, InUse, Approved, AllVehicles, None, Some(location.toString))

  test(
    """when the reference item is absent,
       and metadata is needed,
       should convert DbAddress to AddressRecord with metadata but without county or local custodian
    """) {
    val adr = AddressRecordConverter.convert(ne15xdD1, None, true)
    assert(adr === AddressRecord(
      "GB4510123533", Some(4510123533L), Address(List("10 Taylors Court", "1 Monk Street"),
        NewcastleUponTyne, None, "NE1 5XD", Some(England), UK), "en", None, Some(location.toSeq),
      Some(BLPUState.In_Use.name), Some(LogicalState.Approved.name), Some(StreetClassification.All_Vehicles.name)
    ))
  }

  test(
    """when the reference item is provided,
       should convert DbAddress to AddressRecord without county etc
    """) {
    val ri = ReferenceItem(lc4510.code, lc4510.name, TyneAndWear)
    val adr = AddressRecordConverter.convert(ne15xdD1, Some(ri), false)
    assert(adr === AddressRecord(
      "GB4510123533", Some(4510123533L), Address(List("10 Taylors Court", "1 Monk Street"),
        NewcastleUponTyne, TyneAndWear, "NE1 5XD", Some(England), UK), "en", Some(lc4510), Some(location.toSeq),
      None, None, None
    ))
  }

  test(
    """when the reference item is provided,
       and metadata is needed,
       should convert DbAddress to AddressRecord with county, local custodian and other metadata
    """) {
    val ri = ReferenceItem(lc4510.code, lc4510.name, TyneAndWear)
    val adr = AddressRecordConverter.convert(ne15xdD1, Some(ri), true)
    assert(adr === AddressRecord(
      "GB4510123533", Some(4510123533L), Address(List("10 Taylors Court", "1 Monk Street"),
        NewcastleUponTyne, TyneAndWear, "NE1 5XD", Some(England), UK), "en", Some(lc4510), Some(location.toSeq),
      Some(BLPUState.In_Use.name), Some(LogicalState.Approved.name), Some(StreetClassification.All_Vehicles.name)
    ))
  }

  test(
    """should convert location, ignoring whitespace
    """) {
    val locationString2 = " 54.9714759, -1.6187319 "
    val ne15xdD1B = ne15xdD1.copy(location = Some(locationString2))
    val ri = ReferenceItem(lc4510.code, lc4510.name, TyneAndWear)
    val adr = AddressRecordConverter.convert(ne15xdD1B, Some(ri), true)
    assert(adr.location === Some(location.toSeq))
    assert(adr.locationValue === Some(location))
  }

}

// AddressRecord(GB4510123533,Some(4510123533),Address(List(10 Taylors Court, 1 Monk Street),Some(Newcastle upon Tyne),None,NE1 5XD,Some(Country(GB-ENG,England)),Country(UK,United Kingdom)),en,None,Some(In_Use),Some(Approved),Some(All_Vehicles))
// AddressRecord(GB4510123533,Some(4510123533),Address(List(10 Taylors Court, 1 Monk Street),Some(Newcastle upon Tyne),None,NE1 5XD,Some(Country(GB-ENG,England)),Country(UK,United Kingdom)),en,None,None,None,None)
