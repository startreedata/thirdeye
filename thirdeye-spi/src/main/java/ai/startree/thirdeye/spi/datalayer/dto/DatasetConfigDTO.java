/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetConfigDTO extends AbstractDTO {

  public static String DEFAULT_PREAGGREGATED_DIMENSION_VALUE = "all";

  private String dataset;
  private Templatable<List<String>> dimensions;
  private String timeColumn;
  /**
   * Only meaningful when the timeFormat is EPOCH. Contains MILLISECONDS, SECONDS. etc...
   * kept until dataset entities using the legacy format are migrated to the new format
   */
  @Deprecated
  private TimeUnit timeUnit;
  /**
   * Only meaningful when he timeFormat is EPOCH. The number of Units. In most cases it should be
   * one.
   * kept until dataset entities using the legacy format are migrated to the new format
   */
  @Deprecated
  private Integer timeDuration;
  /**
   * Legacy format is EPOCH + timeUnit=x + timeDuration=X or SIMPLE_DATE_FORMAT:yyyy-MM-xx
   * New format is
   * x:Units:EPOCH or x:Units:SIMPLE_DATE_FORMAT:yyyy-MM-xx
   * Until all dataset entities are migrated to the new format,
   * consumer of the timeFormat should be compatible with both legacy and new formats.
   * */
  private String timeFormat;
  private String timezone;
  private String dataSource;
  private Set<String> owners;
  private Boolean active;
  private List<MetricConfigDTO> metrics;
  /**
   * List of timeColumn metadata.
   * This info is used to optimize generated queries.
   * For instance
   * [ {
   * "name": "timeBucket1hour",
   * "granularity": "PT1H",
   * "timezone: "UTC"
   * },
   * {
   * "name": "timeBucket5minute",
   * "granularity": "PT5M"
   * },
   * ]
   */
  private List<TimeColumnApi> timeColumns;

  /**
   * Expected delay for data to be complete. In ISO 8601. Eg P1D
   */
  private String completenessDelay;
  /**
   * ISO-8601 period. Eg P7D. At detection run, TE re-runs the detection starting from
   * min(lastTimestamp,
   * endTime-replayPeriod). Used to manage data mutability.
   */
  private String mutabilityPeriod;
  /**
   * Dimensions to exclude from RCA algorithm runs.
   */
  private Templatable<List<String>> rcaExcludedDimensions;

  /**
   * Configuration for non-additive dataset
   **/
  // By default, every dataset is additive and this section of configurations should be ignored.
  private boolean additive = true;

  // We assume that non-additive dataset has a default TOP dimension value, which is specified via preAggregatedKeyword,
  // for each dimension. When ThirdEye constructs query string to backend database, it automatically appends the keyword
  // for ALL dimensions except the dimension that has been specified by filter the one specified in
  // dimensionsHaveNoPreAggregation (because some dimension may not have such pre-aggregated dimension value).
  // For example, assume that we have three dimensions: D1, D2, D3. The preAggregatedKeyword is "all" and D2 does not
  // has any pre-aggregated dimension value, i.e., dimensionsHaveNoPreAggregation=[D2]. The filter given by user is
  // {D3=V3}. Then the query should append the WHERE condition {D1=all, D2=V3} in order to get the correct results from
  // this non-additive dataset.
  private List<String> dimensionsHaveNoPreAggregation = Collections.emptyList();

  // The pre-aggregated keyword
  private String preAggregatedKeyword = DEFAULT_PREAGGREGATED_DIMENSION_VALUE;

  @Deprecated
  @JsonIgnore
  private Integer nonAdditiveBucketSize;
  @Deprecated
  @JsonIgnore
  private TimeUnit nonAdditiveBucketUnit;
  /**
   * End of Configuration for non-additive dataset
   **/

  private boolean realtime = false;

  // latest timestamp of the dataset updated by external events
  private long lastRefreshTime;
  // timestamp of receiving the last update event
  private long lastRefreshEventTime = 0;

  private Map<String, String> properties = new HashMap<>();

  public String getDataset() {
    return dataset;
  }

  public DatasetConfigDTO setDataset(String dataset) {
    this.dataset = dataset;
    return this;
  }

  public Templatable<List<String>> getDimensions() {
    return dimensions;
  }

  public DatasetConfigDTO setDimensions(Templatable<List<String>> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public String getTimeColumn() {
    return timeColumn;
  }

  public DatasetConfigDTO setTimeColumn(String timeColumn) {
    this.timeColumn = timeColumn;
    return this;
  }

  @Deprecated
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  @Deprecated
  public DatasetConfigDTO setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

  @Deprecated
  public Integer getTimeDuration() {
    return timeDuration;
  }

  @Deprecated
  public DatasetConfigDTO setTimeDuration(Integer timeDuration) {
    this.timeDuration = timeDuration;
    return this;
  }

  public String getTimeFormat() {
    // todo cyril - can be removed around November 2023/December 2024 - see ResourcesBootstrapService#bootstrap()
    if ("EPOCH".equals(timeFormat) && timeUnit != null) {
      // legacy format that has not been migrated yet - convert to new format on the fly for backward compatibility
      // also, an update on a dataset DTO will result in an upgrade to the new format
      return "1:" + timeUnit + ":EPOCH";
    }
    return timeFormat;
  }

  public DatasetConfigDTO setTimeFormat(String timeFormat) {
    this.timeFormat = timeFormat;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public DatasetConfigDTO setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getDataSource() {
    return dataSource;
  }

  public DatasetConfigDTO setDataSource(String dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public Set<String> getOwners() {
    return owners;
  }

  public DatasetConfigDTO setOwners(Set<String> owners) {
    this.owners = owners;
    return this;
  }

  public Boolean getActive() {
    return active;
  }

  public DatasetConfigDTO setActive(Boolean active) {
    this.active = active;
    return this;
  }

  public boolean isAdditive() {
    return additive;
  }

  public DatasetConfigDTO setAdditive(boolean additive) {
    this.additive = additive;
    return this;
  }

  public List<String> getDimensionsHaveNoPreAggregation() {
    return dimensionsHaveNoPreAggregation;
  }

  public DatasetConfigDTO setDimensionsHaveNoPreAggregation(
      List<String> dimensionsHaveNoPreAggregation) {
    this.dimensionsHaveNoPreAggregation = dimensionsHaveNoPreAggregation;
    return this;
  }

  public String getPreAggregatedKeyword() {
    return preAggregatedKeyword;
  }

  public DatasetConfigDTO setPreAggregatedKeyword(String preAggregatedKeyword) {
    this.preAggregatedKeyword = preAggregatedKeyword;
    return this;
  }

  public boolean isRealtime() {
    return realtime;
  }

  public DatasetConfigDTO setRealtime(boolean realtime) {
    this.realtime = realtime;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public DatasetConfigDTO setProperties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public long getLastRefreshTime() {
    return lastRefreshTime;
  }

  public DatasetConfigDTO setLastRefreshTime(long lastRefreshTime) {
    this.lastRefreshTime = lastRefreshTime;
    return this;
  }

  public long getLastRefreshEventTime() {
    return lastRefreshEventTime;
  }

  public DatasetConfigDTO setLastRefreshEventTime(long lastRefreshEventTime) {
    this.lastRefreshEventTime = lastRefreshEventTime;
    return this;
  }

  public String getCompletenessDelay() {
    return completenessDelay;
  }

  public DatasetConfigDTO setCompletenessDelay(final String completenessDelay) {
    this.completenessDelay = completenessDelay;
    return this;
  }

  public Templatable<List<String>> getRcaExcludedDimensions() {
    return rcaExcludedDimensions;
  }

  public DatasetConfigDTO setRcaExcludedDimensions(
      final Templatable<List<String>> rcaExcludedDimensions) {
    this.rcaExcludedDimensions = rcaExcludedDimensions;
    return this;
  }

  public List<MetricConfigDTO> getMetrics() {
    return metrics;
  }

  public DatasetConfigDTO setMetrics(
      final List<MetricConfigDTO> metrics) {
    this.metrics = metrics;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DatasetConfigDTO)) {
      return false;
    }
    DatasetConfigDTO that = (DatasetConfigDTO) o;
    return active == that.active && additive == that.additive && realtime == that.realtime &&
        Objects.equals(dataset, that.dataset)
        && Objects.equals(dimensions, that.dimensions) && Objects
        .equals(timeColumn, that.timeColumn)
        && timeUnit == that.timeUnit && Objects.equals(timeDuration, that.timeDuration)
        && Objects.equals(timeFormat, that.timeFormat) && Objects.equals(timezone, that.timezone)
        && Objects.equals(dataSource, that.dataSource) && Objects.equals(owners, that.owners)
        && Objects.equals(
        dimensionsHaveNoPreAggregation, that.dimensionsHaveNoPreAggregation) && Objects
        .equals(preAggregatedKeyword,
            that.preAggregatedKeyword)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(dataset, dimensions, timeColumn, timeUnit, timeDuration, timeFormat,
            timezone,
            dataSource, owners, active, additive, dimensionsHaveNoPreAggregation,
            preAggregatedKeyword,
            realtime, properties);
  }

  public String getMutabilityPeriod() {
    return mutabilityPeriod;
  }

  public DatasetConfigDTO setMutabilityPeriod(final String mutabilityPeriod) {
    this.mutabilityPeriod = mutabilityPeriod;
    return this;
  }

  public List<TimeColumnApi> getTimeColumns() {
    return timeColumns;
  }

  public DatasetConfigDTO setTimeColumns(
      final List<TimeColumnApi> timeColumns) {
    this.timeColumns = timeColumns;
    return this;
  }
}
