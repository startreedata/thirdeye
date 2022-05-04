/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTimeZone;

public class RootCauseAnalysisInfo {

  private final @NonNull MergedAnomalyResultDTO mergedAnomalyResultDTO;
  private final @NonNull MetricConfigDTO metricConfigDTO;
  private final @NonNull DatasetConfigDTO datasetConfigDTO;
  // avoid passing the whole AlertMetadataDTO
  private final @NonNull DateTimeZone timezone;

  public RootCauseAnalysisInfo(
      final @NonNull MergedAnomalyResultDTO mergedAnomalyResultDTO,
      final @NonNull MetricConfigDTO metricConfigDTO,
      final @NonNull DatasetConfigDTO datasetConfigDTO,
      final @NonNull DateTimeZone timezone) {
    this.mergedAnomalyResultDTO = mergedAnomalyResultDTO;
    this.metricConfigDTO = metricConfigDTO;
    this.datasetConfigDTO = datasetConfigDTO;
    this.timezone = timezone;
  }

  // todo cyril for refactoring I expose all objects - once refactored, only expose what is really used
  public @NonNull MergedAnomalyResultDTO getMergedAnomalyResultDTO() {
    return mergedAnomalyResultDTO;
  }

  public @NonNull MetricConfigDTO getMetricConfigDTO() {
    return metricConfigDTO;
  }

  public @NonNull DatasetConfigDTO getDatasetConfigDTO() {
    return datasetConfigDTO;
  }

  public @NonNull DateTimeZone getTimezone() {
    return timezone;
  }
}
