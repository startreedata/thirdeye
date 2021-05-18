package org.apache.pinot.thirdeye.datasource;

import java.util.Map;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;

public class ThirdEyeDataSourceContext {

  private MetricConfigManager metricConfigManager;
  private DatasetConfigManager datasetConfigManager;
  private Map<String, Object> properties;

  public MetricConfigManager getMetricConfigManager() {
    return metricConfigManager;
  }

  public ThirdEyeDataSourceContext setMetricConfigManager(
      final MetricConfigManager metricConfigManager) {
    this.metricConfigManager = metricConfigManager;
    return this;
  }

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
