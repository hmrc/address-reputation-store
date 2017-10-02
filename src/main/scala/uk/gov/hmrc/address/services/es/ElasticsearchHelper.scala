/*
 * Copyright 2017 HM Revenue & Customs
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

import java.io.File
import java.util.concurrent.TimeUnit._

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.client.transport.{NoNodeAvailableException, TransportClient}
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.LocalTransportAddress
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.util.FileUtils

import scala.concurrent.duration.Duration

// allows construction without loading Play
object ElasticsearchHelper {

  def buildClients(settings: ElasticSettings, logger: SimpleLogger): List[ElasticClient] = {
    if (settings.netClient.isDefined)
      buildNetClients(settings.netClient.get, logger)
    else if (settings.diskClient.isDefined)
      List(buildDiskClient(settings.diskClient.get))
    else
      List(buildNodeLocalClient())
  }

  /** Normal client suite (for production). */
  def buildNetClients(settings: ElasticNetClientSettings, logger: SimpleLogger): List[ElasticClient] = {
    val esSettings = Settings.settingsBuilder()
      .put("cluster.name", settings.clusterName)
      .put("node.name", "address-lookup-client")
      .build()

    settings.connectionString.split("\\+").toList.map {
      uri =>
        checkStatus(ElasticClient.transport(esSettings, uri), logger)
    }
  }

  /** Local network instance (e.g. for integration testing). */
  def buildNodeLocalClient(): ElasticClient = {
    val esSettings = Settings.settingsBuilder()
      .put("http.enabled", false)
      .put("node.local", true)

    val esClient = TransportClient.builder()
    esClient.settings(esSettings.build)

    val tc = esClient.build()
    tc.addTransportAddress(new LocalTransportAddress("1"))

    ElasticClient.fromClient(tc)
  }

  /** Disk-based local storage without network port (e.g. for unit testing). */
  def buildDiskClient(settings: ElasticDiskClientSettings): ElasticClient = {
    val esHomePath = new File(settings.homeDir)
    if (settings.preDelete)
      FileUtils.deleteDir(esHomePath)
    esHomePath.mkdirs()

    val esSettings = Settings.settingsBuilder()
      .put("http.enabled", false)
      .put("path.home", settings.homeDir)

    ElasticClient.local(esSettings.build)
  }

  def checkStatus(client: ElasticClient, logger: SimpleLogger): ElasticClient = {
    logger.info("Getting cluster health... ")
    var timeout = 0
    while (timeout < 60) {
      try {
        val chr = client.execute {
          get cluster health
        } await Duration(1, MINUTES)

        val status = clusterHealthName(chr.getStatus)
        logger.info(s"cluster ${chr.getClusterName} status is $status")
        return client
      } catch {
        case nne: NoNodeAvailableException => {
          logger.info("Node not found retrying")
          Thread.sleep(1000)
        }
      }
      timeout = timeout + 1
    }
    client
  }

  private def clusterHealthName(chr: ClusterHealthStatus) = {
    chr match {
      case ClusterHealthStatus.GREEN => "green"
      case ClusterHealthStatus.YELLOW => "yellow"
      case ClusterHealthStatus.RED => "red"
      case _ => "invalid"
    }
  }
}

trait ElasticReinitializer {

  def reinitialize: List[ElasticClient]

}

class DefaultElasticReinitializer(settings: ElasticSettings, logger: SimpleLogger) extends ElasticReinitializer {

  override def reinitialize = ElasticsearchHelper.buildClients(settings, logger)

}

class ElasticClientWrapper(clientList: List[ElasticClient], settings: ElasticSettings, logger: SimpleLogger) {

  val reinitializer: ElasticReinitializer = new DefaultElasticReinitializer(settings, logger)

  private val mutable: java.util.Deque[List[ElasticClient]] = new java.util.concurrent.ConcurrentLinkedDeque()
  updateClients(clientList)

  def clients: List[ElasticClient] = {
    mutable.peekFirst()
  }

  // attempt count is zero-based
  def withReinitialization[T](attempt: Int, limit: Int)(f: => T): T = {
    try {
      f
    } catch {
      case nnae: NoNodeAvailableException => {
        if (attempt < limit) {
          reinitialize
          withReinitialization(attempt + 1, limit) {
            f
          }
        } else throw nnae
      }
      case e => throw e
    }
  }

  private def reinitialize: Unit = updateClients(reinitializer.reinitialize)

  private def updateClients(clients: List[ElasticClient]) {
    mutable.addFirst(clients)
    if (mutable.size() > 1) {
      mutable.removeLast()
    }
  }

}
