/*
 * Copyright 2015 HM Revenue & Customs
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

import sbt._

object AppDependencies {

  val appName = "address-reputation-store"

  private val jacksonVersion = "2.8.9"

  val compile = Seq(
    "uk.gov.hmrc" %% "logging" % "0.6.0" withSources(),
    "com.univocity" % "univocity-parsers" % "1.5.6" withSources(),
    "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.4.0",
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.scalacheck" %% "scalacheck" % "1.12.2" % Test,
    "org.pegdown" % "pegdown" % "1.5.0" % Test,
    "org.mockito" % "mockito-all" % "1.10.19" % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
