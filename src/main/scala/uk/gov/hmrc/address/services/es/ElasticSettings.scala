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

case class ElasticNetClientSettings(connectionString: String,
                                    isCluster: Boolean,
                                    clusterName: String,
                                    numShards: Map[String, Int])


case class ElasticDiskClientSettings(homeDir: String,
                                     preDelete: Boolean)


case class ElasticSettings(diskClient: Option[ElasticDiskClientSettings] = None,
                           netClient: Option[ElasticNetClientSettings] = None) {

  def isCluster: Boolean = netClient.isDefined && netClient.get.isCluster
}


object ElasticSettings {
  def apply(localMode: Boolean,
            homeDir: Option[String], preDelete: Boolean,
            connectionString: String, isCluster: Boolean, clusterName: String, numShards: Map[String, Int]): ElasticSettings =
    if (localMode)
      ElasticSettings(None, None)

    else if (homeDir.isDefined)
      ElasticSettings(diskClient = Some(ElasticDiskClientSettings(homeDir.get, preDelete)))

    else
      ElasticSettings(netClient = Some(ElasticNetClientSettings(connectionString, isCluster, clusterName, numShards)))
}
