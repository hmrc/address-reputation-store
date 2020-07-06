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

package uk.gov.hmrc.address.osgb

import org.scalatest.FunSuite

import scala.util.Sorting

class DbAddressOrderingTest extends FunSuite {
  import DbAddress._

  val a1 = DbAddress("GB47070781", List("1 The Street"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a2 = DbAddress("GB47070782", List("2 The Street"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a3 = DbAddress("GB47070783", List("3 The Street"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a4 = DbAddress("GB47070784", List("4 The Street"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val b1 = DbAddress("GB47070801", List("1 The Lane"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a11 = DbAddress("GB47070791", List("11 The Street"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"), Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))

  test("ordering by line1 correctly differentiates pairs (only needing naive string comparison)") {
    val arr1 = Array(b1, a1, a2, a3, a4)
    Sorting.quickSort(arr1)(DbAddressOrderingByLine1)
    assert(arr1 === Array(b1, a1, a2, a3, a4))

    val arr2 = Array(a2, a4, b1, a3, a1)
    Sorting.quickSort(arr2)(DbAddressOrderingByLine1)
    assert(arr2 === Array(b1, a1, a2, a3, a4))
  }

  // TODO future work
  ignore("ordering by line1 correctly differentiates pairs involving numbers (needs number-aware string comparison)") {
    val arr1 = Array(a1, a3, a4, a11, a2)
    Sorting.quickSort(arr1)(DbAddressOrderingByLine1)
    assert(arr1 === Array(a1, a2, a3, a4, a11))
  }
}
