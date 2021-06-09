package org.apache.pinot.thirdeye.spi.datasource;

import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;

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
