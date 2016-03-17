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

package uk.co.hmrc.address.services.mongo

import reactivemongo.api.ReadPreference
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}


trait MongoSearcher[T] {
  def mongoSearch(selector: BSONDocument, fn: BSONDocument => T): Future[List[T]]
}


class SwitchableMongoSearcher[T](collectionProvider: CollectionProvider)(implicit ec: ExecutionContext) extends MongoSearcher[T] {

  def mongoSearch(selector: BSONDocument, fn: BSONDocument => T): Future[List[T]] = {
    import scala.language.implicitConversions
    collectionProvider.collection.find(selector).cursor[BSONDocument](ReadPreference.nearest).collect[List]().map(_.map(fn))
  }
}
