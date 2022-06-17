package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AlertMetadataDTO {

  private DataSourceDTO datasource;
  private DatasetConfigDTO dataset;
  private MetricConfigDTO metric;
  /**
   * For instance: P1D. Recommendation: same as in __timeGroup macro.
   */
  private String granularity;
  private String timezone;

  public DataSourceDTO getDatasource() {
    return datasource;
  }

  public AlertMetadataDTO setDatasource(
      final DataSourceDTO datasource) {
    this.datasource = datasource;
    return this;
  }

  public DatasetConfigDTO getDataset() {
    return dataset;
  }

  public AlertMetadataDTO setDataset(
      final DatasetConfigDTO dataset) {
    this.dataset = dataset;
    return this;
  }

  public MetricConfigDTO getMetric() {
    return metric;
  }

  public AlertMetadataDTO setMetric(final MetricConfigDTO metric) {
    this.metric = metric;
    return this;
  }

  public String getGranularity() {
    return granularity;
  }

  public AlertMetadataDTO setGranularity(final String granularity) {
    this.granularity = granularity;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public AlertMetadataDTO setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }
}
