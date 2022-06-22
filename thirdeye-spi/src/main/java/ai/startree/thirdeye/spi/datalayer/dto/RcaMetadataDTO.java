/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * RcaMetadataDTO contains all necessary info to perform RCA.
 * It is put in the AlertTemplate. This is a solution to implement RCA quickly.
 */
@JsonInclude(Include.NON_NULL)
@Deprecated // use AlertMetadataDTO
public class RcaMetadataDTO {

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

  @Deprecated
  public String getDatasource() {
    return datasource;
  }

  @Deprecated
  public RcaMetadataDTO setDatasource(final String datasource) {
    this.datasource = datasource;
    return this;
  }

  @Deprecated
  public String getDataset() {
    return dataset;
  }

  @Deprecated
  public RcaMetadataDTO setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  @Deprecated
  public String getMetric() {
    return metric;
  }

  @Deprecated
  public RcaMetadataDTO setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  @Deprecated
  public String getAggregationFunction() {
    return aggregationFunction;
  }

  @Deprecated
  public RcaMetadataDTO setAggregationFunction(final String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
    return this;
  }

  @Deprecated
  public String getGranularity() {
    return granularity;
  }

  @Deprecated
  public RcaMetadataDTO setGranularity(final String granularity) {
    this.granularity = granularity;
    return this;
  }

  @Deprecated
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Deprecated
  public RcaMetadataDTO setProperties(final Map<String, Object> properties) {
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
