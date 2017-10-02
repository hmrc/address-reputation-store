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

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, GetAliasDefinition}
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.common.unit.TimeValue
import uk.gov.hmrc.logging.SimpleLogger

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/** Provides a facade for low-level ES administration operations. */
trait ESAdmin {
  def clients: List[ElasticClient]

  def indexExists(name: String): Boolean

  def existingIndexNames: List[String]

  def deleteIndex(name: String)

  def countDocuments(indexName: String, documentName: String): Option[Int]

  def getIndexSettings(indexName: String): Map[String, String]

  def writeIndexSettings(indexName: String, settings: Map[String, String])

  def shardsAreStable(indexName: String): Boolean

  def waitForGreenStatus(indices: String*)

  def getIndexInUseFor(product: String): Option[String]

  def aliasesFor(indexName: String): List[String]

  def allAliases: Map[String, List[String]]

  def setReplicationCount(indexName: String, replicaCount: Int)

  /**
    * Atomic transfer from existing to new.
    * Will typically yield a set of just one name, this being the deposed index(es).
    */
  def switchAliases(newIndexName: String,
                    productName: String,
                    umbrellaAlias: String = "address-reputation-data"): Set[String]

}


/** Provides a facade for low-level ES administration operations. */
class ESAdminImpl(override val clients: List[ElasticClient], logger: SimpleLogger, ec: ExecutionContext, settings: ElasticSettings) extends ESAdmin {

  private implicit val xec = ec

  val healthCheckTimeout: TimeValue = TimeValue.timeValueMinutes(10)

  val connectionAttemptLimit: Int = 3

  def indexExists(name: String): Boolean = existingIndexNames.contains(name)

  private val wrapper = new ElasticClientWrapper(clients, settings, logger)

  def existingIndexNames: List[String] = {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      val healths = c.head.execute {
        get cluster health
      } await()

      healths.getIndices.keySet.asScala.toList.sorted
    }
  }

  def deleteIndex(name: String) {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      c foreach { client =>
        client.admin.indices.delete(new DeleteIndexRequest(name)).actionGet
      }
    }
  }

  def countDocuments(indexName: String, documentName: String): Option[Int] = {
    if (!shardsAreStable(indexName))
      None // not yet in a steady state
    else {
      wrapper.withReinitialization(0, connectionAttemptLimit) {
        val c = wrapper.clients
        val n = c.head.execute {
          search in indexName / documentName size 0
        }.await.totalHits
        Some(n.toInt)
      }
    }
  }

  def getIndexSettings(indexName: String): Map[String, String] = {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      val indexSettingsResponse = c.head.execute {
        get settings indexName
      } await()

      indexSettingsResponse.getIndexToSettings.get(indexName).getAsMap.asScala.toMap
    }
  }

  def writeIndexSettings(indexName: String, settings: Map[String, String]) {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      c foreach { client =>
        greenHealth(client, healthCheckTimeout, indexName)

        client execute {
          closeIndex(indexName)
        } await()

        client execute {
          update settings indexName set settings
        } await()

        client.execute {
          openIndex(indexName)
        } await()

        greenHealth(client, healthCheckTimeout, indexName)
      }
    }
  }

  def shardsAreStable(indexName: String): Boolean = {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      val indicesStatsResponse = c.head.admin.indices.prepareStats(indexName).all.execute.actionGet
      indicesStatsResponse.getTotalShards == indicesStatsResponse.getSuccessfulShards + indicesStatsResponse.getFailedShards
    }
  }

  def waitForGreenStatus(indices: String*) {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      c.foreach(client => greenHealth(client, healthCheckTimeout, indices: _*))
    }
  }

  private def greenHealth(client: ElasticClient, timeout: TimeValue, index: String*) = {
    client.java.admin().cluster().prepareHealth(index: _*).setWaitForGreenStatus().setTimeout(timeout).get
  }

  def getIndexInUseFor(product: String): Option[String] = {
    indexesAliasedBy(product).headOption
  }

  private def indexesAliasedBy(aliasName: String): List[String] = {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      val gar = queryAliases(c.head) {
        getAlias(aliasName)
      }
      gar.keys.toList
    }
  }

  def aliasesFor(indexName: String): List[String] = {
    val found = allAliases.find(_._1 == indexName)
    val optionalMatchingList = found.toList map (_._2)
    optionalMatchingList.flatten
  }

  def allAliases: Map[String, List[String]] = {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      queryAliases(c.head) {
        getAlias("*")
      }
    }
  }

  /**
    * Result is map keyed by index name containing lists of aliases. Example:
    * Map(
    * abi_44_201610251708 -> List(abi, address-reputation-data),
    * abp_44_201610182310 -> List(abp, address-reputation-data)
    * )
    */
  private def queryAliases(client: ElasticClient)(gad: GetAliasDefinition): Map[String, List[String]] = {
    val gar = client.execute(gad) await()
    val rawTree = gar.getAliases.asScala
    val converted = rawTree.map {
      kv =>
        val k = kv.key
        val v = kv.value.asScala.map(_.alias).toList
        k -> v
    }
    converted.toMap
  }

  def setReplicationCount(indexName: String, replicaCount: Int) {
    require(0 <= replicaCount && replicaCount < 16)
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      val fr = c map {
        client => Future {
          logger.info(s"Setting replica count to $replicaCount for $indexName")
          client execute {
            update settings indexName set Map(
              "index.number_of_replicas" -> replicaCount.toString
            )
          } await()
        }
      }
      awaitAll(fr)
    }
  }

  // atomic transfer from existing to new
  def switchAliases(newIndexName: String,
                    productName: String,
                    umbrellaAlias: String = "address-reputation-data"): Set[String] = {
    wrapper.withReinitialization(0, connectionAttemptLimit) {
      val c = wrapper.clients
      val fr = c map {
        client => Future {
          val existingMap = queryAliases(client) {
            getAlias(productName).on("*")
          }

          val existingIndexes = existingMap.keys.toSeq

          val removeStatements = existingIndexes.flatMap {
            indexName =>
              logger.info(s"Removing index $indexName from $umbrellaAlias and $productName aliases")
              Seq(remove alias umbrellaAlias on indexName, remove alias productName on indexName)
          }

          val addStatements = Seq(add alias umbrellaAlias on newIndexName, add alias productName on newIndexName)

          logger.info(s"Adding index $newIndexName to $umbrellaAlias and $productName")

          client execute {
            aliases(removeStatements ++ addStatements)
          } await(10.minutes)

          existingIndexes
        }
      }
      awaitAll(fr).flatten.toSet // will typically yield a set of just one name
    }
  }

  private def awaitAll[T](fr: Seq[Future[T]]): Seq[T] = {
    Await.result(Future.sequence(fr), 60.seconds)
  }

}
