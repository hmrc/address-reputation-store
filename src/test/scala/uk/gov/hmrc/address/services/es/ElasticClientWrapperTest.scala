/*
 * Copyright 2019 HM Revenue & Customs
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

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.NoNodeAvailableException
import org.mockito.Mockito._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.logging.SimpleLogger

class ElasticClientWrapperTest extends WordSpec with MustMatchers with MockitoSugar {

  class Scenario {
    val underlying = mock[Client]
    val client = mock[ElasticClient]
    val reinitialized = mock[ElasticClient]
    val settings = mock[ElasticSettings]
    val logger = mock[SimpleLogger]
    val reigniter = new ElasticReinitializer {
      var reignited: Boolean = false
      override def reinitialize: List[ElasticClient] = {
        reignited = true
        List(reinitialized)
      }
    }
    val wrapper = new ElasticClientWrapper(List(client), settings, logger) {
      override val reinitializer = reigniter
    }
  }

  "wrapper" should {

    "reinitialize" in new Scenario {
      when(client.client)
        .thenThrow(new NoNodeAvailableException("IP has changed!"))
      when(reinitialized.client)
        .thenReturn(underlying)
      val actual: Client = wrapper.withReinitialization[Client](0, 2) {
        wrapper.clients.head.client
      }
      reigniter.reignited must be (true)
      actual must be (underlying)
    }

    "rethrow after limit reached" in new Scenario {
      when(client.client)
        .thenThrow(new NoNodeAvailableException("IP has changed!"))
      when(reinitialized.client)
        .thenThrow(new NoNodeAvailableException("Still not there!"))
      intercept[NoNodeAvailableException] {
        wrapper.withReinitialization[Client](0, 1) {
          wrapper.clients.head.client
        }
      }
    }

  }

}
