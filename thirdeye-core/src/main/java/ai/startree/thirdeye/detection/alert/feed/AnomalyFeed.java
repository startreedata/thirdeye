/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.feed;

import ai.startree.thirdeye.detection.detector.email.filter.AlertFilterFactory;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedConfig;
import java.util.Collection;

/**
 * AnomalyFeed aggregates the output from AnomalyFetchers, and filter the anomalies based on
 * user-input criteria.
 * This anomaly feed can simply union the output, or filter then ongoing anomalies if they are less
 * severe than before.
 */
public interface AnomalyFeed {

  /**
   * Initialize the anomaly feed
   */
  void init(AlertFilterFactory alertFilterFactory, AnomalyFeedConfig anomalyFeedConfig);

  /**
   * Get the aggregated anomalies
   */
  Collection<MergedAnomalyResultDTO> getAnomalyFeed();

  /**
   * Update the snapshots by the alerted anomalies
   */
  void updateSnapshot(Collection<MergedAnomalyResultDTO> alertedAnomalies);
}
