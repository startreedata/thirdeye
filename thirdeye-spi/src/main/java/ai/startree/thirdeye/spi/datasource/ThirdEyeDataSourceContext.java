/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;

public class ThirdEyeDataSourceContext {

  /* TODO remove. ThirdEye should not expose internal persistence layer APIs to a plugin. */
  @Deprecated
  private MetricConfigManager metricConfigManager;

  /* TODO remove. ThirdEye should not expose internal persistence layer APIs to a plugin. */
  @Deprecated
  private DatasetConfigManager datasetConfigManager;

  private DataSourceDTO dataSourceDTO;

  @Deprecated
  public MetricConfigManager getMetricConfigManager() {
    return metricConfigManager;
  }

  public ThirdEyeDataSourceContext setMetricConfigManager(
      final MetricConfigManager metricConfigManager) {
    this.metricConfigManager = metricConfigManager;
    return this;
  }

  @Deprecated
  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public ThirdEyeDataSourceContext setDatasetConfigManager(
      final DatasetConfigManager datasetConfigManager) {
    this.datasetConfigManager = datasetConfigManager;
    return this;
  }

  public DataSourceDTO getDataSourceDTO() {
    return dataSourceDTO;
  }

  public ThirdEyeDataSourceContext setDataSourceDTO(
      final DataSourceDTO dataSourceDTO) {
    this.dataSourceDTO = dataSourceDTO;
    return this;
  }
}
