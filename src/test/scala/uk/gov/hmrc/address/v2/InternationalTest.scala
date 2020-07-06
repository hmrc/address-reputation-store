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

class InternationalTest extends FunSuite {
  import Countries._

  test("nonEmptyFields") {
    assert(International(Nil, None, Some(UK)).nonEmptyFields === Nil)

    assert(International(Nil, Some("FX1 1ZZ"), Some(UK)).nonEmptyFields === List("FX1 1ZZ"))

    assert(International(List("1", "2"), None, Some(UK)).nonEmptyFields === List("1", "2"))

    assert(International(List("1", "2"), Some("FX1 1ZZ"), Some(UK)).nonEmptyFields === List("1", "2", "FX1 1ZZ"))
  }

  test("longestLineLength") {
    assert(International(List("a123456789 123456789", "b"), Some("FX1 1ZZ"), Some(UK)).longestLineLength === 20)
    assert(International(List("a", "b123456789 123456789"), Some("FX1 1ZZ"), Some(UK)).longestLineLength === 20)
    assert(International(List("a", "b"), Some("FX1 1ZZ"), Some(UK)).longestLineLength === 7)
  }

  val shortAddress = International(List("1", "2"), Some( "FX1 1ZZ"), Some(UK))
  val longAddress = International(List("a23456789 123456789", "b23456789 123456789"), Some( "FX1 1ZZ"), Some(UK))

  test("truncatedAddress") {
    assert(
      shortAddress.truncatedAddress(30) ===
        International(List("1", "2"), Some( "FX1 1ZZ"), Some(UK)))

    assert(
      longAddress.truncatedAddress(12) ===
        International(List("a23456789 12", "b23456789 12"), Some( "FX1 1ZZ"), Some(UK)))
  }
}
