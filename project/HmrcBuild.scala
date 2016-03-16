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

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      scalaVersion := "2.11.7",
      libraryDependencies ++= AppDependencies(),
      crossScalaVersions := Seq("2.11.7"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"), "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/")
    )
}

private object AppDependencies {

  import play.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current,
    ws,
//    "uk.gov.hmrc" %% "time" % "2.0.0",
    "uk.gov.hmrc" %% "http-exceptions" % "1.0.0",
//    "uk.gov.hmrc" %% "play-reactivemongo" % "4.7.1",
//    "uk.gov.hmrc" %% "microservice-bootstrap" % "4.2.1",
//    "uk.gov.hmrc" %% "play-authorisation" % "3.1.0",
//    "uk.gov.hmrc" %% "play-health" % "1.1.0",
//    "uk.gov.hmrc" %% "play-url-binders" % "1.0.0",
//    "uk.gov.hmrc" %% "play-config" % "2.0.1",
//    "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
//    "uk.gov.hmrc" %% "domain" % "3.3.0",
//    "org.apache.ftpserver" % "ftpserver" % "1.0.5",
//    "org.simpleflatmapper" % "sfm" % "2.2",
//    "org.apache.commons" % "commons-compress" % "1.10",
//    "commons-net" % "commons-net" % "3.4",
    "com.univocity" % "univocity-parsers" % "1.5.6",
    "org.mongodb" %% "casbah" % "3.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
//        "commons-codec" % "commons-codec" % "1.7" % scope,
        "org.scalatest" %% "scalatest" % "2.2.4" % scope,
        "org.scalacheck" %% "scalacheck" % "1.12.2" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope
//        "com.github.tomakehurst" % "wiremock" % "1.52" % scope,
//        "uk.gov.hmrc" %% "http-verbs-test" % "0.1.0" % scope
//        "org.mockito" % "mockito-all" % "1.10.19" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}
