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
import uk.gov.hmrc.util.{JacksonMapper, PrettyMapper}

class AddressRecordTest extends FunSuite {

  import Countries._

  val lc1 = LocalCustodian(1, "One")
  val shortAddress = Address(List("1", "2"), Some("town"), Some("county"), "FX1 1ZZ", Some(England), UK)
  val longAddress = Address(List("a23456789 123456789", "b23456789 123456789"), Some("c23456789 123456789"), Some("d123456789 123456789"),
    "FX1 1ZZ", Some(England), UK)

  val location = Seq(BigDecimal("54.9714759"), BigDecimal("-1.6187319"))

  val sar = AddressRecord("id", Some(123L), shortAddress, "en", Some(lc1), Some(location),
    Some(BLPUState.In_Use.name), Some(LogicalState.Approved.name), Some(StreetClassification.All_Vehicles.name))

  val lar = AddressRecord("id", Some(456L), longAddress, "en", Some(lc1), Some(location),
    Some(BLPUState.In_Use.name), Some(LogicalState.Approved.name), Some(StreetClassification.All_Vehicles.name))

  test("truncatedAddress") {
    assert(sar.truncatedAddress(30) === sar)

    assert(lar.truncatedAddress(12) === AddressRecord("id", Some(456L), longAddress.truncatedAddress(12), "en", Some(lc1), Some(location),
      Some(BLPUState.In_Use.name), Some(LogicalState.Approved.name), Some(StreetClassification.All_Vehicles.name)))
  }

  test("withoutMetadata") {
    assert(sar.withoutMetadata === AddressRecord("id", Some(123L), shortAddress, "en", Some(lc1), Some(location), None, None, None))
  }

  test("asV1") {
    assert(sar.asV1.id === sar.id)
    assert(sar.asV1.address.lines === sar.address.lines)
  }

  test("json round-trip should preserve all data and should not fail on any synthetic fields") {
    val s = JacksonMapper.writeValueAsString(sar)
    val x = JacksonMapper.readValue(s, classOf[AddressRecord])
    assert(x === sar)
  }

  test("view pretty json") {
    val os = LocalCustodian(7655, "Ordnance Survey")
    val frontDoor = Seq(BigDecimal("51.5069393"), BigDecimal("-.1068806"))
    val se19pyAdd = Address(List("Dorset House 27-45", "Stamford Street"), Some("London"), None, "SE1 9PY", Some(England), UK)
    val se19pyAr = AddressRecord("GB10091836674", Some(10091836674L), se19pyAdd, "en", Some(os), Some(frontDoor),
      Some(BLPUState.In_Use.name), Some(LogicalState.Approved.name), Some(StreetClassification.All_Vehicles.name))
    val s = PrettyMapper.writeValueAsString(se19pyAr)
    println(s)
    // for manual inspection
    val x = JacksonMapper.readValue(s, classOf[AddressRecord])
    assert(x === se19pyAr)
  }

  test("location must have size 2 or be undefined") {
    intercept[IllegalArgumentException] {
      lar.copy(location = Some(Nil))
    }
    intercept[IllegalArgumentException] {
      lar.copy(location = Some(Seq(BigDecimal(0))))
    }
    val e = intercept[IllegalArgumentException] {
      lar.copy(location = Some(Seq(BigDecimal(1), BigDecimal(2), BigDecimal(3))))
    }
    assert(e.getMessage.contains("(1, 2, 3)"))
  }
}
