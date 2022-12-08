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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertMetadataApi {

  private DataSourceApi datasource;
  private DatasetApi dataset;
  private MetricApi metric;
  /**
   * For instance: P1D. Recommendation: same as in __timeGroup macro.
   */
  private String granularity;
  private String timezone;
  private EventContextApi eventContext;

  @JsonIgnore
  @Deprecated // now set in the AnomalyMergerPostProcessor node
  private String mergeMaxGap;
  @JsonIgnore
  @Deprecated // now set in the AnomalyMergerPostProcessor node
  private String mergeMaxDuration;

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

  public EventContextApi getEventContext() {
    return eventContext;
  }

  public AlertMetadataApi setEventContext(final EventContextApi eventContext) {
    this.eventContext = eventContext;
    return this;
  }
}
