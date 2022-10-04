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

import java.util.List;

public class BreakdownApi implements ThirdEyeApi {

  private MetricApi metric;
  private DatasetApi dataset;
  private Long start;
  private Long end;
  private String timezone;
  private Double aggregate;
  private Double threshold;
  private Double percentage;

  private Integer resultSize;
  private List<DimensionFilterContributionApi> results;

  public Long getStart() {
    return start;
  }

  public BreakdownApi setStart(final Long start) {
    this.start = start;
    return this;
  }

  public Long getEnd() {
    return end;
  }

  public BreakdownApi setEnd(final Long end) {
    this.end = end;
    return this;
  }

  public MetricApi getMetric() {
    return metric;
  }

  public BreakdownApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public DatasetApi getDataset() {
    return dataset;
  }

  public BreakdownApi setDataset(final DatasetApi dataset) {
    this.dataset = dataset;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public BreakdownApi setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public Double getAggregate() {
    return aggregate;
  }

  public BreakdownApi setAggregate(final Double aggregate) {
    this.aggregate = aggregate;
    return this;
  }

  public Double getThreshold() {
    return threshold;
  }

  public BreakdownApi setThreshold(final Double threshold) {
    this.threshold = threshold;
    return this;
  }

  public Double getPercentage() {
    return percentage;
  }

  public BreakdownApi setPercentage(final Double percentage) {
    this.percentage = percentage;
    return this;
  }

  public Integer getResultSize() {
    return resultSize;
  }

  public BreakdownApi setResultSize(final Integer resultSize) {
    this.resultSize = resultSize;
    return this;
  }

  public List<DimensionFilterContributionApi> getResults() {
    return results;
  }

  public BreakdownApi setResults(
      final List<DimensionFilterContributionApi> results) {
    this.results = results;
    return this;
  }
}
