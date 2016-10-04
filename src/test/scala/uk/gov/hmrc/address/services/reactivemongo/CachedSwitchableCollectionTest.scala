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

package uk.gov.hmrc.address.services.reactivemongo

import java.time.Clock

import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import reactivemongo.api.collections.bson.BSONCollection
import uk.gov.hmrc.logging.StubLogger

class CachedSwitchableCollectionTest extends FunSuite with MockitoSugar {

  test("when successive lookups occur (before any timeout), the same collection is returned each time") {
    val coll = mock[BSONCollection]
    val cp = mock[CollectionProvider]
    when(cp.collection) thenReturn coll
    val clock = mock[Clock]
    when(clock.millis) thenReturn(1000000, 1000001, 1000002, 1000003)

    val logger = new StubLogger
    val csc = new CachedSwitchableCollection(cp, 123, clock, logger)
    assert(csc.collection === coll)
    assert(csc.collection === coll)
    assert(csc.collection === coll)
  }

  test("when successive lookups occur (after timeout), and there is a new name for the collection, a new collection is returned") {
    val coll1 = mock[BSONCollection]
    val coll2 = mock[BSONCollection]
    when(coll1.name) thenReturn "addressbase1"
    when(coll2.name) thenReturn "addressbase2"

    val cp = mock[CollectionProvider]
    when(cp.collection) thenReturn coll1 thenReturn coll2

    val clock = mock[Clock]
    when(clock.millis) thenReturn(1000000, 1000001, 1001111)

    val logger = new StubLogger
    val csc = new CachedSwitchableCollection(cp, 123, clock, logger)
    assert(csc.collection === coll1)
    assert(csc.collection === coll2)
  }

  test("when successive lookups occur (after timeout), but there is the same name for the collection, the same collection is returned") {
    val coll1 = mock[BSONCollection]
    val coll2 = mock[BSONCollection]
    when(coll1.name) thenReturn "addressbase1"
    when(coll2.name) thenReturn "addressbase1"

    val cp = mock[CollectionProvider]
    when(cp.collection) thenReturn coll1 thenReturn coll2

    val clock = mock[Clock]
    when(clock.millis) thenReturn(1000000, 1000001, 1001111)

    val logger = new StubLogger
    val csc = new CachedSwitchableCollection(cp, 123, clock, logger)
    assert(csc.collection === coll1)
    assert(csc.collection === coll1)
  }
}
