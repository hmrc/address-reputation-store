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

import java.util.Date

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.unit.TimeValue
import uk.gov.hmrc.BuildProvenance
import uk.gov.hmrc.address.services.writers.WriterSettings
import uk.gov.hmrc.logging.SimpleLogger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object IndexMetadata {
  val replicaCount = "1"
  val ariAliasName = "address-reputation-data"
  val indexAlias = "addressbase-index"
  val address = "address"
}

class IndexMetadata(esAdmin: ESAdmin, val isCluster: Boolean, numShardsMap: Map[String, Int], status: SimpleLogger, ec: ExecutionContext) {

  import IndexMetadata._

  private implicit val xec = ec

  private val iCompletedAt = "index.completedAt"
  private val iBulkSize = "index.bulkSize"
  private val iLoopDelay = "index.loopDelay"
  private val iIncludeDPA = "index.includeDPA"
  private val iIncludeLPI = "index.includeLPI"
  private val iPrefer = "index.prefer"
  private val iStreetFilter = "index.streetFilter"
  private val iBuildVersion = "index.buildVersion"
  private val iBuildNumber = "index.buildNumber"
  private val iDoNotDelete = "index.doNotDelete"
  private val twoSeconds = TimeValue.timeValueSeconds(2)

  //-------------------- Simple facade methods --------------------

  def clients: List[ElasticClient] = esAdmin.clients

  def indexExists(name: IndexState): Boolean = esAdmin.indexExists(name.formattedName)

  def deleteIndex(name: IndexState) {
    esAdmin.deleteIndex(name.formattedName)
  }

  //-------------------- Core methods --------------------

  def deleteIndexIfExists(name: IndexState) {
    val s = name.formattedName
    if (esAdmin.indexExists(s)) {
      esAdmin.deleteIndex(s)
    }
  }

  def existingIndexes: List[IndexName] = {
    esAdmin.existingIndexNames.flatMap(name => IndexName(name))
  }

  def existingIndexMetadata: List[IndexMetadataItem] = {
    existingIndexes.flatMap(name => findMetadata(name))
  }

  def existingIndexNamesLike(name: IndexState): List[IndexName] = {
    val collectionNamePrefix = name.toPrefix + "_"
    val stringNames = esAdmin.existingIndexNames.filter(_.startsWith(collectionNamePrefix)).sorted
    stringNames.flatMap(s => IndexName.apply(s))
  }

  def getIndexNameInUseFor(product: String): Option[IndexName] = {
    esAdmin.getIndexInUseFor(product).flatMap(n => IndexName(n))
  }

  val defaultNumShards = 12

  def numShards(productName: String): Int = {
    numShardsMap.getOrElse(productName, defaultNumShards)
  }

  def findMetadata(name: IndexName): Option[IndexMetadataItem] = {
    val index = name.formattedName
    val count = esAdmin.countDocuments(index, address)
    val settings = esAdmin.getIndexSettings(index)

    val completedDate = settings.get(iCompletedAt).map(s => new Date(s.toLong))
    val bSize = settings.get(iBulkSize)
    val lDelay = settings.get(iLoopDelay)
    val iDPA = settings.get(iIncludeDPA)
    val iLPI = settings.get(iIncludeLPI)
    val pref = settings.get(iPrefer)
    val sFilter = settings.get(iStreetFilter)
    val buildVersion = settings.get(iBuildVersion)
    val buildNumber = settings.get(iBuildNumber)
    val doNotDelete = settings.get(iDoNotDelete).fold(false)(_.toBoolean)

    Some(IndexMetadataItem(name = name, size = count, completedAt = completedDate,
      bulkSize = bSize, loopDelay = lDelay,
      includeDPA = iDPA, includeLPI = iLPI, prefer = pref, streetFilter = sFilter,
      buildVersion = buildVersion, buildNumber = buildNumber,
      aliases = esAdmin.aliasesFor(index), doNotDelete = doNotDelete))
  }

  def writeCompletionDateTo(indexName: IndexState, date: Date = new Date()) {
    esAdmin.writeIndexSettings(indexName.formattedName, Map(iCompletedAt -> date.getTime.toString))
  }

  def writeIngestSettingsTo(indexName: IndexState, writerSettings: WriterSettings, provenance: BuildProvenance) {
    val buildVersion = provenance.version.map(iBuildVersion -> _)
    val buildNumber = provenance.number.map(iBuildNumber -> _)
    val newIndexName = indexName.formattedName
    esAdmin.writeIndexSettings(newIndexName,
      Map(
        iBulkSize -> writerSettings.bulkSize.toString,
        iLoopDelay -> writerSettings.loopDelay.toString,
        iIncludeDPA -> writerSettings.algorithm.includeDPA.toString,
        iIncludeLPI -> writerSettings.algorithm.includeLPI.toString,
        iPrefer -> writerSettings.algorithm.prefer,
        iStreetFilter -> writerSettings.algorithm.streetFilter.toString
      ) ++ buildVersion ++ buildNumber
    )
    if (isCluster) {
      status.info(s"Increasing replication count for $newIndexName")
      esAdmin.setReplicationCount(newIndexName, 1)
    }
  }

  def setIndexInUse(name: IndexName) {
    val newIndexName = name.formattedName
    val productName = name.productName

    if (isCluster) {
      status.info(s"Waiting for $ariAliasName to go green after increasing replica count")
      esAdmin.waitForGreenStatus(newIndexName)
    }

    val priorIndexes = esAdmin.switchAliases(newIndexName, productName, ariAliasName)

    esAdmin.waitForGreenStatus(newIndexName)
  }

  /** @deprecated because it isn't idempotent. */
  def toggleDoNotDelete(name: IndexName): Unit = {
    val index = name.formattedName
    val settings = esAdmin.getIndexSettings(index)
    val currentVal = settings.get(iDoNotDelete).map(_.toBoolean)
    val newVal = !currentVal.contains(true)
    esAdmin.writeIndexSettings(index, Map(iDoNotDelete -> newVal.toString))
  }

  def setDoNotDelete(name: IndexName, newValue: Boolean): Unit = {
    val index = name.formattedName
    esAdmin.writeIndexSettings(index, Map(iDoNotDelete -> newValue.toString))
  }

  private def awaitAll[T](fr: Seq[Future[T]]): Seq[T] = {
    Await.result(Future.sequence(fr), Duration("60s"))
  }
}
