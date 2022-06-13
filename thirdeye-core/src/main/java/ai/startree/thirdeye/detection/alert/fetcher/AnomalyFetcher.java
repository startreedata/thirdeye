/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
