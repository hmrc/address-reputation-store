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

  val a = DbAddress("GB47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG", "GB-ENG")

  test("linesContainIgnoreCase - check we can find a match in line1, case insensitive") {
    assert(a.linesContainIgnoreCase("e1") === true)
    assert(a.linesContainIgnoreCase("E1") === true)
  }

  test("linesContainIgnoreCase - check we can find a match in line2, case insensitive") {
    assert(a.linesContainIgnoreCase("e2") === true)
    assert(a.linesContainIgnoreCase("E2") === true)
  }

  test("linesContainIgnoreCase - check we can find a match in line3, case insensitive") {
    assert(a.linesContainIgnoreCase("e3") === true)
    assert(a.linesContainIgnoreCase("E3") === true)
  }

  test("linesContainIgnoreCase - unmatched string should lead to the address being rejected") {
    assert(a.linesContainIgnoreCase("SOMETHING") === false)
  }

  test("lines") {
    assert(DbAddress("GB47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG", "GB-ENG").lines === List("Line1", "Line2", "Line3"))
    assert(DbAddress("GB47070784", "Line1", "Line2", "", "Tynemouth", "NE30 4HG", "GB-ENG").lines === List("Line1", "Line2"))
    assert(DbAddress("GB47070784", "Line1", "", "", "Tynemouth", "NE30 4HG", "GB-ENG").lines === List("Line1"))
  }

  test("tupled") {
    assert(a.tupled === List("_id" -> "GB47070784", "lines" -> List("Line1", "Line2", "Line3"), "town" -> "Tynemouth", "postcode" -> "NE30 4HG", "subdivision" -> "GB-ENG"))
  }

  test("toSeq") {
    assert(a.toSeq === Seq("GB47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG", "GB-ENG"))
  }

  test("splitPostcode") {
    assert(a.splitPostcode === Postcode("NE", "30", "4", "HG"))
  }
}
