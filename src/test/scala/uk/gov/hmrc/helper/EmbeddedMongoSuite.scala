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

package uk.gov.hmrc.helper

import com.github.simplyscala.MongoEmbedDatabase
import org.scalatest.{Args, Status, Suite}
import uk.gov.hmrc.address.admin.MetadataStoreTest
import uk.gov.hmrc.address.osgb.DbAddressIntegration
import uk.gov.hmrc.address.services.mongo.CasbahMongoConnection

// The primary objective is to start and stop embedded MongoDB cleanly exactly once.

object EmbeddedMongoSuite extends Suite with MongoEmbedDatabase {

  // Currently, we have to set the number of test suites explicitly (pah!)
  //------------------------------------------------
  var HowManyTestSuitesAreUsingThisServerWrapper =
    Set(
      classOf[MetadataStoreTest],
      classOf[DbAddressIntegration]
    ).size
  //------------------------------------------------

  lazy val mongoTestConnection = new MongoTestConnection(mongoStart())

  def stop() {
    HowManyTestSuitesAreUsingThisServerWrapper -= 1
    if (HowManyTestSuitesAreUsingThisServerWrapper <= 0) {
      mongoTestConnection.stop()
    }
  }
}


trait EmbeddedMongoSuite extends Suite {

  override def run(testName: Option[String], args: Args): Status = {
    try {
      val status = super.run(testName, args)
      status.waitUntilCompleted()
      status
    }
    finally {
      casbahMongoConnection.close()
      Thread.sleep(500)
      EmbeddedMongoSuite.stop()
    }
  }

  def mongoTestConnection = EmbeddedMongoSuite.mongoTestConnection

  lazy val casbahMongoConnection = new CasbahMongoConnection(mongoTestConnection.uri)
}
