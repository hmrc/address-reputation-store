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

package uk.gov.hmrc.address.v1

import org.scalatest.FunSuite

class AddressRecordTest extends FunSuite {

  import Countries.UK

  val lc1 = LocalCustodian(1, "One")
  val shortAddress = Address(List("1", "2"), Some("town"), Some("county"), "FX1 1ZZ", Some("XX-YYY"), UK)
  val longAddress = Address(List("a23456789 123456789", "b23456789 123456789"), Some("c23456789 123456789"), Some("d123456789 123456789"),
    "FX1 1ZZ", Some("XX-YYY"), UK)

  test("truncatedAddress") {
    val sar = AddressRecord("id", Some(123L), shortAddress, Some(lc1), "en")
    assert(sar.truncatedAddress(30) === sar)

    val lar = AddressRecord("id", Some(456L), longAddress, Some(lc1), "en")
    assert(
      lar.truncatedAddress(12) === AddressRecord("id", Some(456L), longAddress.truncatedAddress(12), Some(lc1), "en"))
  }
}
