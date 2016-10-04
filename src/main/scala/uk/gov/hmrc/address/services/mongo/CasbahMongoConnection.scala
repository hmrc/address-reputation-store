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

package uk.gov.hmrc.address.services.mongo

import com.mongodb.casbah._
import uk.gov.hmrc.util._

class CasbahMongoConnection(mongoUri: String) {

  val hasReplicas = mongoUri.indexOf(',') > 0
  val mongoDbName: String = mongoUri.divideLast('/')(1)
  private lazy val mongoClient = MongoClient(MongoClientURI(mongoUri))

  lazy val getConfiguredDb: MongoDB = {
    val db = mongoClient(mongoDbName)
    val writeConcern = if (hasReplicas) WriteConcern.Majority else WriteConcern.Acknowledged
    db.setWriteConcern(writeConcern)
    db
  }

  def close(): Unit = {
    mongoClient.close()
  }
}
