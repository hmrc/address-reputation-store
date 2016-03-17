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

  val dummyInAddr = DbAddress("47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG")

  test("ShouldKeep - check we can find a match in line1, case insensitive") {
    assert(dummyInAddr.line123Contains("e1") === true)
    assert(dummyInAddr.line123Contains("E1") === true)
  }

  test("ShouldKeep - check we can find a match in line2, case insensitive") {
    assert(dummyInAddr.line123Contains("e2") === true)
    assert(dummyInAddr.line123Contains("E2") === true)
  }

  test("ShouldKeep - check we can find a match in line3, case insensitive") {
    assert(dummyInAddr.line123Contains("e3") === true)
    assert(dummyInAddr.line123Contains("E3") === true)
  }

  test("ShouldKeep - unmatched string should lead to the address being rejected") {
    assert(dummyInAddr.line123Contains("SOMETHING") === false)
  }
}
