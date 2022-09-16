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
package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTimeZone;

public class RcaInfo {

  private final @NonNull MergedAnomalyResultDTO anomaly;
  private final @NonNull MetricConfigDTO metric;
  private final @NonNull DatasetConfigDTO dataset;
  // avoid passing the whole AlertMetadataDTO
  private final @NonNull DateTimeZone timezone;

  public RcaInfo(
      final @NonNull MergedAnomalyResultDTO anomaly,
      final @NonNull MetricConfigDTO metric,
      final @NonNull DatasetConfigDTO dataset,
      final @NonNull DateTimeZone timezone) {
    this.anomaly = anomaly;
    this.metric = metric;
    this.dataset = dataset;
    this.timezone = timezone;
  }

  // todo cyril for refactoring I expose all objects - once refactored, only expose what is really used
  public @NonNull MergedAnomalyResultDTO getAnomaly() {
    return anomaly;
  }

  public @NonNull MetricConfigDTO getMetric() {
    return metric;
  }

  public @NonNull DatasetConfigDTO getDataset() {
    return dataset;
  }

  public @NonNull DateTimeZone getTimezone() {
    return timezone;
  }
}
