package ai.startree.thirdeye.alert;

import com.google.common.base.Objects;

public class MonitoredMetricWrapper {
  private String datasource;
  private String dataset;
  private String metric;

  public String getDatasource() {
    return datasource;
  }

  public MonitoredMetricWrapper setDatasource(final String datasource) {
    this.datasource = datasource;
    return this;
  }

  public String getDataset() {
    return dataset;
  }

  public MonitoredMetricWrapper setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public MonitoredMetricWrapper setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MonitoredMetricWrapper that = (MonitoredMetricWrapper) o;
    return Objects.equal(getDatasource(), that.getDatasource())
        && Objects.equal(getDataset(), that.getDataset())
        && Objects.equal(getMetric(), that.getMetric());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getDatasource(), getDataset(), getMetric());
  }
}
