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

package uk.gov.hmrc.address.services

import java.io.{PrintWriter, StringWriter}

import org.scalatest.FunSuite

class CsvWriterTest extends FunSuite {

  test("println should convert an empty record correctly") {
    val a = Seq("", "", "", "", "", "")
    val w = new StringWriter()
    new CsvWriter(new PrintWriter(w)).println(a).println(a).println(a)
    assert(w.toString === ",,,,,\n,,,,,\n,,,,,\n")
  }

  test("println should convert a normal record correctly") {
    val a = Seq("47070784", "Line1", "Line2", "Line3", "Tynemouth", "NE30 4HG")
    val w = new StringWriter()
    new CsvWriter(new PrintWriter(w)).println(a)
    assert(w.toString === "47070784,Line1,Line2,Line3,Tynemouth,NE30 4HG\n")
  }

  test("println should convert a record containing commas correctly") {
    val a = Seq("47070784", "Line, 1", "Line, 2", "Line, 3", "Tynemouth", "NE30 4HG")
    val w = new StringWriter()
    new CsvWriter(new PrintWriter(w)).println(a)
    assert(w.toString ===
      """47070784,"Line, 1","Line, 2","Line, 3",Tynemouth,NE30 4HG
        |""".stripMargin)
  }

  test("println should convert a record containing surrounding spaces correctly") {
    val a = Seq("47070784", " Line 1", "Line 2 ", " Line 3 ", " Tynemouth ", "NE30 4HG")
    val w = new StringWriter()
    new CsvWriter(new PrintWriter(w)).println(a)
    assert(w.toString ===
      """47070784," Line 1","Line 2 "," Line 3 "," Tynemouth ",NE30 4HG
        |""".stripMargin)
  }

  test("println should convert a record containing commas and double quotes correctly") {
    val a = Seq("47070784", """Foo, "Bar", 1""", "Line, 2", "Line, 3", "Tynemouth", "NE30 4HG")
    val w = new StringWriter()
    new CsvWriter(new PrintWriter(w)).println(a)
    assert(w.toString ===
      """47070784,"Foo, ""Bar"", 1","Line, 2","Line, 3",Tynemouth,NE30 4HG
        |""".stripMargin)
  }
}
