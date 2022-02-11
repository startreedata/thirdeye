/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import java.time.Duration;

/**
 * Duration filter. Filter the anomaly based on the anomaly duration.
 * USE WITH CAUTION. If min duration is set larger than the maximum possible anomaly duration
 * the detection module produced, all anomalies would potentially be filtered.
 */
public class DurationAnomalyFilter implements AnomalyFilter<DurationAnomalyFilterSpec> {

  private Duration minDuration;
  private Duration maxDuration;

  @Override
  public void init(DurationAnomalyFilterSpec spec) {
    if (spec.getMinDuration() != null) {
      this.minDuration = Duration.parse(spec.getMinDuration());
    }
    if (spec.getMaxDuration() != null) {
      this.maxDuration = Duration.parse(spec.getMaxDuration());
    }
  }

  @Override
  public void init(DurationAnomalyFilterSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    long anomalyDuration = anomaly.getEndTime() - anomaly.getStartTime();
    return anomalyDuration >= this.minDuration.toMillis() && anomalyDuration <= this.maxDuration
        .toMillis();
  }
}
