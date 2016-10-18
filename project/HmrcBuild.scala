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

import sbt.Keys._
import sbt._
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {

  import uk.gov.hmrc._

  val appName = "address-reputation-store"

  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      scalaVersion := "2.11.8",
      libraryDependencies ++= AppDependencies(),
      crossScalaVersions := Seq("2.11.8"),
      parallelExecution in Test := false,
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"), "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/")
    )
}

private object AppDependencies {

  // Important note: Play is *not* a dependency here, nor is it a transitive dependency.
  val reactiveMongoVersion = "0.11.8"
  // Important note: Play is *not* a dependency here, nor is it a transitive dependency.

  private val jacksonVersion = "2.7.4"

  val compile = Seq(
    // netty 3.10 has a breaking API change
    "io.netty" % "netty" % "3.9.9.Final" force(),
    "uk.gov.hmrc" %% "reactivemongo" % reactiveMongoVersion withSources(),
    "uk.gov.hmrc" %% "reactivemongo-bson" % reactiveMongoVersion withSources(),
    "uk.gov.hmrc" %% "reactivemongo-bson-macros" % reactiveMongoVersion withSources(),
    "com.univocity" % "univocity-parsers" % "1.5.6" withSources(),
    "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.3.1" excludeAll ExclusionRule(organization = "io.netty"),
    "org.mongodb" %% "casbah" % "3.1.1",
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "2.2.4" % scope,
        "org.scalacheck" %% "scalacheck" % "1.12.2" % scope,
        "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "org.mockito" % "mockito-all" % "1.10.19" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}
