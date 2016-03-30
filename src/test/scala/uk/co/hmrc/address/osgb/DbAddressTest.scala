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

import org.scalatest.FunSuite

class DbAddressTest extends FunSuite {

  val a = DbAddress("47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG")

  test("line123Contains - check we can find a match in line1, case insensitive") {
    assert(a.line123Contains("e1") === true)
    assert(a.line123Contains("E1") === true)
  }

  test("line123Contains - check we can find a match in line2, case insensitive") {
    assert(a.line123Contains("e2") === true)
    assert(a.line123Contains("E2") === true)
  }

  test("line123Contains - check we can find a match in line3, case insensitive") {
    assert(a.line123Contains("e3") === true)
    assert(a.line123Contains("E3") === true)
  }

  test("line123Contains - unmatched string should lead to the address being rejected") {
    assert(a.line123Contains("SOMETHING") === false)
  }

  test("tupled") {
    assert(a.tupled === List("uprn" -> "47070784", "line1" -> "Line1", "line2" -> "Line2", "line3" -> "Line3", "town" -> "Tynemouth", "postcode" -> "NE30 4HG"))
  }

  test("toSeq") {
    assert(a.toSeq === Seq("47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG"))
  }
}
