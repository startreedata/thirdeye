/*
 * Copyright 2024 StarTree Inc
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

import ai.startree.thirdeye.spi.api.AlertMetadataApi;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertMetadataDTO {

  private DataSourceDTO datasource;
  private DatasetConfigDTO dataset;
  private MetricConfigDTO metric;
  /**
   * For instance: P1D. Recommendation: same as in __timeGroup macro.
   */
  private String granularity;
  private String timezone;
  private EventContextDto eventContext;
  private String detectionAggregationFunction;

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

  public String getDetectionAggregationFunction() {
    return detectionAggregationFunction;
  }

  public AlertMetadataDTO setDetectionAggregationFunction(
      final String detectionAggregationFunction) {
    this.detectionAggregationFunction = detectionAggregationFunction;
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

  public EventContextDto getEventContext() {
    return eventContext;
  }

  public AlertMetadataDTO setEventContext(
      final EventContextDto eventContext) {
    this.eventContext = eventContext;
    return this;
  }
}
