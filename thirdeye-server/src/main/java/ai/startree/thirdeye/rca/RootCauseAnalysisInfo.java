/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.joda.time.DateTimeZone;

public class RootCauseAnalysisInfo {

  private final MergedAnomalyResultDTO mergedAnomalyResultDTO;
  private final MetricConfigDTO metricConfigDTO;
  private final DatasetConfigDTO datasetConfigDTO;
  // avoid passing the whole AlertMetadataDTO
  private final DateTimeZone timezone;

  public RootCauseAnalysisInfo(
      final MergedAnomalyResultDTO mergedAnomalyResultDTO,
      final MetricConfigDTO metricConfigDTO,
      final DatasetConfigDTO datasetConfigDTO,
      final DateTimeZone timezone) {
    this.mergedAnomalyResultDTO = mergedAnomalyResultDTO;
    this.metricConfigDTO = metricConfigDTO;
    this.datasetConfigDTO = datasetConfigDTO;
    this.timezone = timezone;
  }

  // todo cyril for refactoring I expose all objects - once refactored, only expose what is really used
  public MergedAnomalyResultDTO getMergedAnomalyResultDTO() {
    return mergedAnomalyResultDTO;
  }

  public MetricConfigDTO getMetricConfigDTO() {
    return metricConfigDTO;
  }

  public DatasetConfigDTO getDatasetConfigDTO() {
    return datasetConfigDTO;
  }

  public DateTimeZone getTimezone() {
    return timezone;
  }
}
