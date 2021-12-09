package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * RcaMetadataApi contains all necessary info to perform RCA.
 * It is put in the AlertTemplate. This is a solution to implement RCA quickly.
 */
@JsonInclude(Include.NON_NULL)
public class RcaMetadataApi {

  private String datasource;
  private String dataset;
  private String metric;
  /**
   * For instance: sum.
   */
  private String aggregationFunction;
  /**
   * For instance: P1D. Recommendation: same as in __timeGroup macro.
   */
  private String granularity;
  /**
   * Custom properties to add features quickly if need be.
   */
  private Map<String, Object> properties;

  public String getDatasource() {
    return datasource;
  }

  public RcaMetadataApi setDatasource(final String datasource) {
    this.datasource = datasource;
    return this;
  }

  public String getDataset() {
    return dataset;
  }

  public RcaMetadataApi setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public RcaMetadataApi setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public String getAggregationFunction() {
    return aggregationFunction;
  }

  public RcaMetadataApi setAggregationFunction(final String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
    return this;
  }

  public String getGranularity() {
    return granularity;
  }

  public RcaMetadataApi setGranularity(final String granularity) {
    this.granularity = granularity;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public RcaMetadataApi setProperties(final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return this.toString();
    }
  }
}
