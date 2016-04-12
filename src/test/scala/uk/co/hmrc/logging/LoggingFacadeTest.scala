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

package uk.co.hmrc.logging

/**
  * LoggingFacade interacts by side-effect and is intrinsically hard to auto-test,
  * but it is sufficient to demonstrate that it is working manually.
  */
object LoggingFacadeTest extends App {

  Stdout.info("T1 {} {}", "a", "b")
  Stdout.info("T2", new Exception("foo1"))

  Stdout.warn("T3 {} {}", "a", "b")
  Stdout.warn("T4", new Exception("foo2"))

  Stdout.error("T5 {} {}", "a", "b")
  Stdout.error("T6", new Exception("foo3"))
}
