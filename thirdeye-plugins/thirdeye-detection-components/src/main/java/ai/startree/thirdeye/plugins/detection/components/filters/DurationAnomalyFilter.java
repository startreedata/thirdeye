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
package ai.startree.thirdeye.plugins.detection.components.filters;

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
