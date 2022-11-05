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

public class CohortComputationApi implements ThirdEyeApi {

  private MetricApi metric;
  private Long start;
  private Long end;
  private String timezone;
  private Double aggregate;
  private Double threshold;
  private Double percentage;
  private Boolean RoundOffThreshold;
  private boolean generateEnumerationItems;
  private String enumerationItemParamKey;
  private List<String> dimensions;
  private Integer limit;
  private Integer maxDepth;
  private String where;
  private String having;

  /* Output Fields */
  private Integer resultSize;
  private List<DimensionFilterContributionApi> results;
  private List<EnumerationItemApi> enumerationItems;

  public Integer getLimit() {
    return limit;
  }

  public CohortComputationApi setLimit(final Integer limit) {
    this.limit = limit;
    return this;
  }

  public MetricApi getMetric() {
    return metric;
  }

  public CohortComputationApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public Long getStart() {
    return start;
  }

  public CohortComputationApi setStart(final Long start) {
    this.start = start;
    return this;
  }

  public Long getEnd() {
    return end;
  }

  public CohortComputationApi setEnd(final Long end) {
    this.end = end;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public CohortComputationApi setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public Double getAggregate() {
    return aggregate;
  }

  public CohortComputationApi setAggregate(final Double aggregate) {
    this.aggregate = aggregate;
    return this;
  }

  public Double getThreshold() {
    return threshold;
  }

  public CohortComputationApi setThreshold(final Double threshold) {
    this.threshold = threshold;
    return this;
  }

  public Double getPercentage() {
    return percentage;
  }

  public CohortComputationApi setPercentage(final Double percentage) {
    this.percentage = percentage;
    return this;
  }

  public Boolean getRoundOffThreshold() {
    return RoundOffThreshold;
  }

  public CohortComputationApi setRoundOffThreshold(final Boolean roundOffThreshold) {
    RoundOffThreshold = roundOffThreshold;
    return this;
  }

  public boolean isGenerateEnumerationItems() {
    return generateEnumerationItems;
  }

  public CohortComputationApi setGenerateEnumerationItems(final boolean generateEnumerationItems) {
    this.generateEnumerationItems = generateEnumerationItems;
    return this;
  }

  public String getEnumerationItemParamKey() {
    return enumerationItemParamKey;
  }

  public CohortComputationApi setEnumerationItemParamKey(final String enumerationItemParamKey) {
    this.enumerationItemParamKey = enumerationItemParamKey;
    return this;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public CohortComputationApi setDimensions(final List<String> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public Integer getResultSize() {
    return resultSize;
  }

  public CohortComputationApi setResultSize(final Integer resultSize) {
    this.resultSize = resultSize;
    return this;
  }

  public List<DimensionFilterContributionApi> getResults() {
    return results;
  }

  public CohortComputationApi setResults(
      final List<DimensionFilterContributionApi> results) {
    this.results = results;
    return this;
  }

  public List<EnumerationItemApi> getEnumerationItems() {
    return enumerationItems;
  }

  public CohortComputationApi setEnumerationItems(
      final List<EnumerationItemApi> enumerationItems) {
    this.enumerationItems = enumerationItems;
    return this;
  }

  public Integer getMaxDepth() {
    return maxDepth;
  }

  public CohortComputationApi setMaxDepth(final Integer maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  public String getWhere() {
    return where;
  }

  public CohortComputationApi setWhere(final String where) {
    this.where = where;
    return this;
  }

  public String getHaving() {
    return having;
  }

  public CohortComputationApi setHaving(final String having) {
    this.having = having;
    return this;
  }
}
