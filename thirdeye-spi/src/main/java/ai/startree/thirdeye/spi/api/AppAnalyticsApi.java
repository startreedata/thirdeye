/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AppAnalyticsApi implements ThirdEyeApi {

  private String version;
  private Integer nMonitoredMetrics;
  private AnomalyStatsApi anomalyStats;

  public String getVersion() {
    return version;
  }

  public AppAnalyticsApi setVersion(final String version) {
    this.version = version;
    return this;
  }

  public Integer getnMonitoredMetrics() {
    return nMonitoredMetrics;
  }

  public AppAnalyticsApi setnMonitoredMetrics(final Integer nMonitoredMetrics) {
    this.nMonitoredMetrics = nMonitoredMetrics;
    return this;
  }

  public AnomalyStatsApi getAnomalyStats() {
    return anomalyStats;
  }

  public AppAnalyticsApi setAnomalyStats(final AnomalyStatsApi anomalyStats) {
    this.anomalyStats = anomalyStats;
    return this;
  }
}
