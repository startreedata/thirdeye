/*
 * Copyright 2024 StarTree Inc
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

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventContextDto;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Chronology;
import org.joda.time.Period;

// fixme cyril authz - move this to service if it's actually a service
public class RcaInfo {

  private final @NonNull AnomalyDTO anomaly;
  private final @NonNull MetricConfigDTO metric;
  private final @NonNull DatasetConfigDTO dataset;
  // avoid passing the whole AlertMetadataDTO
  private final @NonNull Chronology chronology;
  private final @NonNull Period granularity;


  private final @NonNull EventContextDto eventContext;

  public RcaInfo(
      final @NonNull AnomalyDTO anomaly,
      final @NonNull MetricConfigDTO metric,
      final @NonNull DatasetConfigDTO dataset,
      final @NonNull Chronology chronology, final @NonNull Period granularity, @NonNull final EventContextDto eventContext) {
    this.anomaly = anomaly;
    this.metric = metric;
    this.dataset = dataset;
    this.chronology = chronology;
    this.granularity = granularity;
    this.eventContext = eventContext;
  }

  // todo cyril for refactoring I expose all objects - once refactored, only expose what is really used
  public @NonNull AnomalyDTO getAnomaly() {
    return anomaly;
  }

  public @NonNull MetricConfigDTO getMetric() {
    return metric;
  }

  public @NonNull DatasetConfigDTO getDataset() {
    return dataset;
  }

  public @NonNull Chronology getChronology() {
    return chronology;
  }

  public @NonNull EventContextDto getEventContext() {
    return eventContext;
  }

  public Period getGranularity() {
    return granularity;
  }
}
