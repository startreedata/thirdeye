/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.detection.alert.fetcher;

import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFetcherConfig;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.Collection;
import java.util.Properties;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAnomalyFetcher implements AnomalyFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(BaseAnomalyFetcher.class);

  public static final String ANOMALY_SOURCE_TYPE = "anomalySourceType";
  public static final String ANOMALY_SOURCE = "anomalySource";

  protected Properties properties;
  protected AnomalyFetcherConfig anomalyFetcherConfig;
  protected MergedAnomalyResultManager mergedAnomalyResultManager;
  protected boolean active = true;

  public BaseAnomalyFetcher(final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  public static String getSnapshotKey(MergedAnomalyResultDTO anomaly) {
    return anomaly.getMetric() + "::" + AnomalyUtils.generateFilterSetForTimeSeriesQuery(anomaly);
  }

  @Override
  public void init(AnomalyFetcherConfig anomalyFetcherConfig) {
    this.anomalyFetcherConfig = anomalyFetcherConfig;
    this.properties = SpiUtils
        .decodeCompactedProperties(anomalyFetcherConfig.getProperties());
  }

  @Override
  public abstract Collection<MergedAnomalyResultDTO> getAlertCandidates(DateTime current,
      AlertSnapshotDTO alertSnapShot);

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
