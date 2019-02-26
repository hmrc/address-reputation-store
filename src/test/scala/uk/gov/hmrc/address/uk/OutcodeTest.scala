/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.address.uk

import org.scalatest.FunSuite

class OutcodeTest extends FunSuite {

  test(
    """Given a outcode in arbitrary format (as occasionally entered by a user)
       when cleanupOutcode is used,
       then the outcode should become uppercase
       and surrounding whitespace should have been removed
    """) {
    assert(Outcode.cleanupOutcode("cr3") === Some(Outcode("CR", "3")), "lowercase outcode to uppercase")
    assert(Outcode.cleanupOutcode("  CR3  ") === Some(Outcode("CR", "3")), "normalise spaces")
    assert(Outcode.cleanupOutcode("  SW1a  ") === Some(Outcode("SW", "1A")), "normalise spaces")
    assert(Outcode.cleanupOutcode("6QJ") === None, "regex mismatch")
    assert(Outcode.cleanupOutcode("12345678") === None, "regex mismatch")
    assert(Outcode.cleanupOutcode("CR3QJJ") === None, "regex mismatch")
    assert(Outcode.cleanupOutcode("CR3 QJJ") === None, "regex mismatch")
    assert(Outcode.cleanupOutcode("CRV6QJ") === None, "regex mismatch")
    assert(Outcode.cleanupOutcode("CRV 6QJ") === None, "regex mismatch")
  }

  test("Split a outcode: case 1") {
    assert(Outcode("B9") === Outcode("B", "9"))
  }

  test("Split a outcode: case 2") {
    assert(Outcode("B79") === Outcode("B", "79"))
  }

  test("Split a outcode: case 3") {
    assert(Outcode("BT29") === Outcode("BT", "29"))
  }

  test("Split a outcode: case 4") {
    assert(Outcode("SW1A") === Outcode("SW", "1A"))
  }

  test("toString") {
    assert(Outcode("SK9").toString === "SK9")
  }

  test("null outcode cannot be cleaned") {
    assert(Outcode.cleanupOutcode(null) === None)
  }
}
