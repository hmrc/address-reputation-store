/*
 * Copyright 2020 HM Revenue & Customs
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


case class IndexMetadataItem(name: IndexName,
                             size: Option[Int],
                             completedAt: Option[Date] = None,
                             bulkSize: Option[String] = None,
                             loopDelay: Option[String] = None,
                             includeDPA: Option[String] = None,
                             includeLPI: Option[String] = None,
                             prefer: Option[String] = None,
                             streetFilter: Option[String] = None,
                             buildVersion: Option[String] = None,
                             buildNumber: Option[String] = None,
                             aliases: List[String] = Nil,
                             doNotDelete: Boolean = false) {

  def completedAfter(date: Date): Boolean = completedAt.isDefined && completedAt.get.after(date)

  def isIncomplete: Boolean = completedAt.isEmpty

  def isComplete: Boolean = completedAt.isDefined
}
