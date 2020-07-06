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

class AddressTest extends FunSuite {
  import Countries._

  test("nonEmptyFields") {
    assert(Address(Nil, None, None, "FX1 1ZZ", None, UK).nonEmptyFields === List("FX1 1ZZ"))

    assert(Address(List("1", "2"), None, None, "FX1 1ZZ", None, UK).nonEmptyFields === List("1", "2", "FX1 1ZZ"))

    assert(Address(Nil, Some("town"), None, "FX1 1ZZ", None, UK).nonEmptyFields === List("town", "FX1 1ZZ"))

    assert(Address(Nil, None, Some("county"), "FX1 1ZZ", None, UK).nonEmptyFields === List("county", "FX1 1ZZ"))

    assert(Address(List("1", "2"), Some("town"), Some("county"), "FX1 1ZZ", Some(England), UK).nonEmptyFields ===
      List("1", "2", "town", "county", "FX1 1ZZ"))
  }

  test("longestLineLength") {
    assert(Address(List("a123456789 123456789", "b"), Some("c"), Some("d"), "FX1 1ZZ", Some(England), UK).longestLineLength === 20)
    assert(Address(List("a", "b123456789 123456789"), Some("c"), Some("d"), "FX1 1ZZ", Some(England), UK).longestLineLength === 20)
    assert(Address(List("a", "b"), Some("c123456789 123456789"), Some("d"), "FX1 1ZZ", Some(England), UK).longestLineLength === 20)
    assert(Address(List("a", "b"), Some("c"), Some("d123456789 123456789"), "FX1 1ZZ", Some(England), UK).longestLineLength === 20)
    assert(Address(List("a", "b"), Some("c"), Some("d"), "FX1 1ZZ", Some(England), UK).longestLineLength === 7)
  }

  val shortAddress = Address(List("1", "2"), Some("town"), Some("county"), "FX1 1ZZ", Some(England), UK)
  val longAddress = Address(List("a23456789 123456789", "b23456789 123456789"), Some("c23456789 123456789"), Some("d123456789 123456789"), "FX1 1ZZ", Some(England), UK)
  val ne15xdAr = Address(List("10 Taylors Court", "Monk Street", "Byker"), Some("Newcastle upon Tyne"), Some("Tyne & Wear"), "NE1 5XD", Some(England), UK)

  test("truncatedAddress") {
    assert(
      shortAddress.truncatedAddress(30) ===
        Address(List("1", "2"), Some("town"), Some("county"), "FX1 1ZZ", Some(England), UK))

    assert(
      longAddress.truncatedAddress(12) ===
        Address(List("a23456789 12", "b23456789 12"), Some("c23456789 12"), Some("d123456789"), "FX1 1ZZ", Some(England), UK))
  }

  test("asInternational") {
    assert(
      longAddress.asInternational ===
        International(List("a23456789 123456789", "b23456789 123456789", "c23456789 123456789", "d123456789 123456789", "England"), Some("FX1 1ZZ"), Some(UK)))

    assert(
      ne15xdAr.asInternational ===
        International(List("10 Taylors Court", "Monk Street", "Byker", "Newcastle upon Tyne", "Tyne & Wear", "England"), Some("NE1 5XD"), Some(UK)))
  }
}
