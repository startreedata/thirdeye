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
  private String timezone;
  /**
   * ISO-8601 format. Max gap between to anomalies for the anomalies to be merged.
   */
  private String mergeMaxGap;
  /**
   * ISO-8601 format. Max duration of a merger of anomalies.
   */
  private String mergeMaxDuration;

  private EventContextApi eventContext;

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

  public String getTimezone() {
    return timezone;
  }

  public AlertMetadataApi setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getMergeMaxGap() {
    return mergeMaxGap;
  }

  public AlertMetadataApi setMergeMaxGap(final String mergeMaxGap) {
    this.mergeMaxGap = mergeMaxGap;
    return this;
  }

  public String getMergeMaxDuration() {
    return mergeMaxDuration;
  }

  public AlertMetadataApi setMergeMaxDuration(final String mergeMaxDuration) {
    this.mergeMaxDuration = mergeMaxDuration;
    return this;
  }

  public EventContextApi getEventContext() {
    return eventContext;
  }

  public AlertMetadataApi setEventContext(final EventContextApi eventContext) {
    this.eventContext = eventContext;
    return this;
  }
}
