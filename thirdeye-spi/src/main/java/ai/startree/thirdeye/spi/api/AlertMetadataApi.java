package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AlertMetadataApi {

  private DataSourceApi datasource;
  private DatasetApi dataset;
  private MetricApi metric;
  /**
   * For instance: P1D. Recommendation: same as in __timeGroup macro.
   */
  private String granularity;

  public DataSourceApi getDatasource() {
    return datasource;
  }

  public AlertMetadataApi setDatasource(final DataSourceApi datasource) {
    this.datasource = datasource;
    return this;
  }

  public DatasetApi getDataset() {
    return dataset;
  }

  public AlertMetadataApi setDataset(final DatasetApi dataset) {
    this.dataset = dataset;
    return this;
  }

  public MetricApi getMetric() {
    return metric;
  }

  public AlertMetadataApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public String getGranularity() {
    return granularity;
  }

  public AlertMetadataApi setGranularity(final String granularity) {
    this.granularity = granularity;
    return this;
  }
}
