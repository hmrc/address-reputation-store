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

package uk.gov.hmrc.address.services.es

import org.scalatest.FunSuite

class IndexNameTest extends FunSuite {

  test("format IndexName using toString") {
    assert(IndexName("fooey", Some(1), Some("ts2")).toString === "fooey_1_ts2")
    assert(IndexName("fooey", Some(40), Some("ts13")).toString === "fooey_40_ts13")
    assert(IndexName("fooey", Some(40), None).toString === "fooey_40")
    assert(IndexName("fooey", None, None).toString === "fooey")
  }

  test("IndexName toPrefix") {
    assert(IndexName("fooey", Some(1), Some("ts2")).toPrefix === "fooey_1")
    assert(IndexName("fooey", Some(40), None).toPrefix === "fooey_40")
  }

  test("parse via apply") {
    assert(IndexName("") === None)
    assert(IndexName("foo") === Some(IndexName("foo", None, None)))
    assert(IndexName("foo_bar") === None)
    assert(IndexName("foo_bar_baz") === None)
    assert(IndexName("abp_40") === Some(IndexName("abp", Some(40), None)))
    assert(IndexName("abp_40_ts2") === Some(IndexName("abp", Some(40), Some("ts2"))))
    assert(IndexName("abp_40_ts2_zz") === None)
    assert(IndexName("abp_foo_ts2") === None)
  }
}
