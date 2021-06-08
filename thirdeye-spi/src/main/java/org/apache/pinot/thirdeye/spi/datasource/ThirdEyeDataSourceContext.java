package org.apache.pinot.thirdeye.spi.datasource;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;

public class ThirdEyeDataSourceContext {

  /* TODO remove. ThirdEye should not expose internal persistence layer APIs to a plugin. */
  @Deprecated
  private MetricConfigManager metricConfigManager;

  /* TODO remove. ThirdEye should not expose internal persistence layer APIs to a plugin. */
  @Deprecated
  private DatasetConfigManager datasetConfigManager;

  private Map<String, Object> properties;

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

  public Map<String, Object> getProperties() {
    return properties;
  }

  public ThirdEyeDataSourceContext setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
