/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;

public class RootCauseAnalysisInfo {

  private final MergedAnomalyResultDTO mergedAnomalyResultDTO;
  private final MetricConfigDTO metricConfigDTO;
  private final DatasetConfigDTO datasetConfigDTO;

  public RootCauseAnalysisInfo(
      final MergedAnomalyResultDTO mergedAnomalyResultDTO,
      final MetricConfigDTO metricConfigDTO,
      final DatasetConfigDTO datasetConfigDTO) {
    this.mergedAnomalyResultDTO = mergedAnomalyResultDTO;
    this.metricConfigDTO = metricConfigDTO;
    this.datasetConfigDTO = datasetConfigDTO;
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
}
