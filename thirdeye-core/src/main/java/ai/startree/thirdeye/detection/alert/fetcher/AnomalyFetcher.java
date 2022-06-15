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

import ai.startree.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFetcherConfig;
import java.util.Collection;
import org.joda.time.DateTime;

public interface AnomalyFetcher {

  /**
   * Initialize the AnomalyFetcher with properties
   *
   * @param anomalyFetcherConfig the configuration of the anomaly fetcher
   */
  void init(AnomalyFetcherConfig anomalyFetcherConfig);

  /**
   * Get the alert candidates with the given current date time
   *
   * @param current the current DateTime
   * @param alertSnapShot the snapshot of the alert, containing the time and
   */
  Collection<MergedAnomalyResultDTO> getAlertCandidates(DateTime current,
      AlertSnapshotDTO alertSnapShot);
}
