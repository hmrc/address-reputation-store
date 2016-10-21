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

package uk.gov.hmrc.address.osgb

import java.io.File

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{RichGetResponse, RichSearchResponse}
import org.elasticsearch.common.unit.TimeValue
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import uk.gov.hmrc.address.services.es.{ESSchema, ElasticsearchHelper}
import uk.gov.hmrc.address.uk.Postcode
import uk.gov.hmrc.util.FileUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class EsAddressIntegration extends FunSuite with BeforeAndAfterAll {

  val esDataPath = System.getProperty("java.io.tmpdir") + "/es-ars"

  lazy val esClient = ElasticsearchHelper.buildDiskClient(esDataPath)

  override def beforeAll() {
    FileUtils.deleteDir(new File(esDataPath))

    esClient execute {
      ESSchema.createIndexDefinition(idx, doc,
        ESSchema.Settings(1, 0, "1s"))
    } await()

    waitForIndex(idx)
  }

  override def afterAll() {
    FileUtils.deleteDir(new File(esDataPath))
  }

  //-----------------------------------------------------------------------------------------------

  val idx = "abp_39_ts5"
  val doc = "address"

  private def waitForIndex(idx: String, timeout: TimeValue = TimeValue.timeValueSeconds(2)) {
    esClient.java.admin.cluster.prepareHealth(idx).setWaitForGreenStatus().setTimeout(timeout).get
  }

  private def output(a: DbAddress) {
    val tuples = a.forElasticsearch
    esClient execute {
      index into idx -> doc fields tuples id a.id routing a.postcode
    }
  }

  private def convertGetResponse(response: RichGetResponse): List[DbAddress] = {
    List(DbAddress(response.fields))
  }

  private def convertSearchResponse(response: RichSearchResponse): List[DbAddress] = {
    response.hits.map(hit => DbAddress(hit.sourceAsMap)).toList
  }

  private def findID(id: String): Future[List[DbAddress]] = {
    val searchResponse = esClient.execute {
      search in idx -> doc query matchQuery("id", id)
    }
    searchResponse map convertSearchResponse
  }

  def findPostcode(postcode: Postcode): Future[List[DbAddress]] = {
    val searchResponse = esClient.execute {
      search in idx -> doc query matchQuery("postcode.raw", postcode.toString) routing postcode.toString size 100
    }
    searchResponse map convertSearchResponse
  }

  //-----------------------------------------------------------------------------------------------

  import DbAddress._

  val a1 = DbAddress("GB47070784", List("A1", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"),
    Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))
  val a2 = DbAddress("GB47070785", List("A2", "Line2", "Line3"), Some("Tynemouth"), "NE30 4HG", Some("GB-ENG"),
    Some("UK"), Some(1234), Some(English), Some(2), Some(1), Some(8), Some("1"), Some("1.0,-1.0"))

  test("ES write then findPostcode") {
    output(a1)
    output(a2)

    Thread.sleep(1200) // more than 1 second needed for ES stabilisation
    waitForIndex(idx)

    val r = Await.result(findPostcode(Postcode(a1.postcode)), 20.seconds)
    assert(r.toSet === Set(a1, a2))
  }

  test("ES write then findID") {
    output(a1)
    output(a2)

    Thread.sleep(1200) // more than 1 second needed for ES stabilisation
    waitForIndex(idx)

    val r = Await.result(findID(a1.id), 20.seconds)
    assert(r === List(a1))
  }

}
