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

import org.scalatest.FunSuite
import uk.gov.hmrc.util.JacksonMapper

class AddressRecordTest extends FunSuite {

  import Countries._

  val lc1 = LocalCustodian(1, "One")
  val shortAddress = Address(List("1", "2"), Some("town"), Some("county"), "FX1 1ZZ", Some(England), UK)
  val longAddress = Address(List("a23456789 123456789", "b23456789 123456789"), Some("c23456789 123456789"), Some("d123456789 123456789"),
    "FX1 1ZZ", Some(England), UK)

  val sar = AddressRecord("id", Some(123L), shortAddress, "en", Some(lc1),
    Some(BLPUState.In_Use), Some(LogicalState.Approved), Some(StreetClassification.All_Vehicles))

  val lar = AddressRecord("id", Some(456L), longAddress, "en", Some(lc1),
    Some(BLPUState.In_Use), Some(LogicalState.Approved), Some(StreetClassification.All_Vehicles))

  test("truncatedAddress") {
    assert(sar.truncatedAddress(30) === sar)

    assert(lar.truncatedAddress(12) === AddressRecord("id", Some(456L), longAddress.truncatedAddress(12), "en", Some(lc1), None, None, None))
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
}
