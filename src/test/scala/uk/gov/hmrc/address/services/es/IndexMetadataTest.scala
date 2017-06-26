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

import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.BuildProvenance
import uk.gov.hmrc.address.services.writers.{Algorithm, WriterSettings}
import uk.gov.hmrc.logging.StubLogger

class IndexMetadataTest extends WordSpec with MockitoSugar {

  private implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  "deleteIndexIfExists" in {
    val name = IndexName("abc", Some(12), Some("1010101"))
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger
    when(esAdmin.indexExists(name.toString)) thenReturn true

    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
    indexMetadata.deleteIndexIfExists(name)

    verify(esAdmin).deleteIndex(name.toString)
  }

  "existingIndexes" in {
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger
    when(esAdmin.existingIndexNames) thenReturn List("foo_bar", "abc_12_1010101", "abc_13_3030303")

    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
    val indexes = indexMetadata.existingIndexes

    assert(indexes === List(IndexName("abc", Some(12), Some("1010101")), IndexName("abc", Some(13), Some("3030303"))))
  }

  "existingIndexNamesLike" in {
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger
    when(esAdmin.existingIndexNames) thenReturn List("foo_bar", "abc_12_1010101", "abc_12_2020202", "abc_13_3030303")

    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
    val indexes = indexMetadata.existingIndexNamesLike(IndexName("abc", Some(12), None))

    assert(indexes === List(IndexName("abc", Some(12), Some("1010101")), IndexName("abc", Some(12), Some("2020202"))))
  }

  "getIndexNameInUseFor" must {
    "when an existing name is parseable" in {
      val esAdmin = mock[ESAdmin]
      val logger = new StubLogger
      when(esAdmin.getIndexInUseFor("abc")) thenReturn Some("abc_12_1010101")

      val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
      val index = indexMetadata.getIndexNameInUseFor("abc")

      assert(index === Some(IndexName("abc", Some(12), Some("1010101"))))
    }

    "when an existing name is unparseable" in {
      val esAdmin = mock[ESAdmin]
      val logger = new StubLogger
      when(esAdmin.getIndexInUseFor("abc")) thenReturn Some("foo_bar")

      val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
      val index = indexMetadata.getIndexNameInUseFor("abc")

      assert(index === None)
    }

    "when there is none" in {
      val esAdmin = mock[ESAdmin]
      val logger = new StubLogger
      when(esAdmin.getIndexInUseFor("abc")) thenReturn None

      val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
      val index = indexMetadata.getIndexNameInUseFor("abc")

      assert(index === None)
    }
  }

  "findMetadata" in {
    val tstamp = 123456789L
    val date = new Date(tstamp)
    val name = IndexName("abc", Some(12), Some("1010101"))
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger
    when(esAdmin.countDocuments(name.toString, IndexMetadata.address)) thenReturn Some(123)
    when(esAdmin.aliasesFor(name.toString)) thenReturn List("aaa", "zzz")
    when(esAdmin.getIndexSettings(name.toString)) thenReturn Map(
      "index.completedAt" -> tstamp.toString,
      "index.bulkSize" -> "234",
      "index.loopDelay" -> "345",
      "index.includeDPA" -> "true",
      "index.includeLPI" -> "false",
      "index.prefer" -> "DPA",
      "index.streetFilter" -> "7",
      "index.buildVersion" -> "V1",
      "index.buildNumber" -> "987"
    )

    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
    val metadata = indexMetadata.findMetadata(name)

    assert(metadata === Some(IndexMetadataItem(
      name = name,
      size = Some(123),
      completedAt = Some(date),
      bulkSize = Some("234"),
      loopDelay = Some("345"),
      includeDPA = Some("true"),
      includeLPI = Some("false"),
      prefer = Some("DPA"),
      streetFilter = Some("7"),
      buildVersion = Some("V1"),
      buildNumber = Some("987"),
      aliases = List("aaa", "zzz")
    )))
  }

  "writeCompletionDateTo" in {
    val tstamp = 123456789L
    val date = new Date(tstamp)
    val name = IndexName("abc", Some(12), Some("1010101"))
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger

    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
    indexMetadata.writeCompletionDateTo(name, date)

    verify(esAdmin).writeIndexSettings(name.toString, Map("index.completedAt" -> tstamp.toString))
  }

  "writeIngestSettingsTo" in {
    val tstamp = 123456789L
    val date = new Date(tstamp)
    val name = IndexName("abc", Some(12), Some("1010101"))
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger

    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
    indexMetadata.writeIngestSettingsTo(name, WriterSettings(123, 456, Algorithm.default), BuildProvenance(Some("v1"), Some("987")))

    verify(esAdmin).writeIndexSettings(name.toString, Map(
      "index.includeDPA" -> "true",
      "index.includeLPI" -> "true",
      "index.prefer" -> "DPA",
      "index.bulkSize" -> "123",
      "index.loopDelay" -> "456",
      "index.streetFilter" -> "0",
      "index.buildNumber" -> "987",
      "index.buildVersion" -> "v1"
    ))
  }

  "setIndexInUse" must {
    "handle non-clustered" in {
      val name = IndexName("abc", Some(12), Some("1010101"))
      val esAdmin = mock[ESAdmin]
      val logger = new StubLogger

      val indexMetadata = new IndexMetadata(esAdmin, false, Map(), logger, ec)
      indexMetadata.setIndexInUse(name)

      verify(esAdmin).switchAliases(name.toString, name.productName, IndexMetadata.ariAliasName)
      verify(esAdmin).waitForGreenStatus(name.toString)
      verifyNoMoreInteractions(esAdmin)
    }

    "handle clustered" in {
      val oldName = IndexName("abc", Some(12), Some("1010101"))
      val newName = IndexName("abc", Some(13), Some("3030303"))
      val esAdmin = mock[ESAdmin]
      val logger = new StubLogger
      when(esAdmin.switchAliases(newName.toString, newName.productName, IndexMetadata.ariAliasName)) thenReturn Set(oldName.toString)

      val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)
      indexMetadata.setIndexInUse(newName)

      verify(esAdmin).setReplicationCount(newName.toString, 1)
      verify(esAdmin).switchAliases(newName.toString, newName.productName, IndexMetadata.ariAliasName)
      verify(esAdmin,times(0)).setReplicationCount(oldName.toString, 0)
      verify(esAdmin, times(2)).waitForGreenStatus(newName.toString)
      verifyNoMoreInteractions(esAdmin)
    }
  }

  "toggleDoNotdelete" in {
    val name = IndexName("abc", Some(12), Some("1010101"))
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger
    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)

    when(esAdmin.getIndexSettings(name.toString)) thenReturn Map.empty[String, String]
    indexMetadata.toggleDoNotDelete(name)
    verify(esAdmin).writeIndexSettings(name.toString, Map("index.doNotDelete" -> "true"))

    when(esAdmin.getIndexSettings(name.toString)) thenReturn Map("index.doNotDelete" -> "true")
    indexMetadata.toggleDoNotDelete(name)
    verify(esAdmin).writeIndexSettings(name.toString, Map("index.doNotDelete" -> "false"))
  }

  "setDoNotdelete" in {
    val name = IndexName("abc", Some(12), Some("1010101"))
    val esAdmin = mock[ESAdmin]
    val logger = new StubLogger
    val indexMetadata = new IndexMetadata(esAdmin, true, Map(), logger, ec)

    when(esAdmin.getIndexSettings(name.toString)) thenReturn Map.empty[String, String]
    indexMetadata.setDoNotDelete(name, true)
    verify(esAdmin).writeIndexSettings(name.toString, Map("index.doNotDelete" -> "true"))

    when(esAdmin.getIndexSettings(name.toString)) thenReturn Map("index.doNotDelete" -> "true")
    indexMetadata.setDoNotDelete(name, false)
    verify(esAdmin).writeIndexSettings(name.toString, Map("index.doNotDelete" -> "false"))
  }
}
