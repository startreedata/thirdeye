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

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
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
  private TimeUnit timeUnit;
  private Integer timeDuration;
  private String timeFormat;
  private String timezone;
  private String dataSource;
  private Set<String> owners;
  private Boolean active;
  /**
   * Expected delay for data to be complete. In ISO 8601. Eg P1D
   */
  private String completenessDelay;
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

  // the actual time duration for non-additive dataset
  private Integer nonAdditiveBucketSize;

  // the actual time unit for non-additive dataset
  private TimeUnit nonAdditiveBucketUnit;
  /**
   * End of Configuration for non-additive dataset
   **/

  private boolean realtime = false;

  @Deprecated // use completenessDelay
  @JsonIgnore
  private TimeGranularity expectedDelay;
  // latest timestamp of the dataset updated by external events
  private long lastRefreshTime;
  // timestamp of receiving the last update event
  private long lastRefreshEventTime = 0;

  private Map<String, String> properties = new HashMap<>();

  @JsonIgnore
  private TimeGranularity bucketTimeGranularity;

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

  /**
   * This method is preserved for reading object from database via object mapping (i.e., Java
   * reflection)
   *
   * @return the time unit of the granularity of the timestamp of each data point.
   */
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public DatasetConfigDTO setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

  /**
   * Use DatasetConfigDTO.bucketTimeGranularity instead of this method for considering the additives
   * of the dataset.
   *
   * This method is preserved for reading object from database via object mapping (i.e., Java
   * reflection)
   *
   * @return the duration of the granularity of the timestamp of each data point.
   */
  @Deprecated
  public Integer getTimeDuration() {
    return timeDuration;
  }

  public DatasetConfigDTO setTimeDuration(Integer timeDuration) {
    this.timeDuration = timeDuration;
    return this;
  }

  public String getTimeFormat() {
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

  public Integer getNonAdditiveBucketSize() {
    return nonAdditiveBucketSize;
  }

  public DatasetConfigDTO setNonAdditiveBucketSize(Integer nonAdditiveBucketSize) {
    this.nonAdditiveBucketSize = nonAdditiveBucketSize;
    return this;
  }

  public TimeUnit getNonAdditiveBucketUnit() {
    return nonAdditiveBucketUnit;
  }

  public DatasetConfigDTO setNonAdditiveBucketUnit(TimeUnit nonAdditiveBucketUnit) {
    this.nonAdditiveBucketUnit = nonAdditiveBucketUnit;
    return this;
  }

  public boolean isRealtime() {
    return realtime;
  }

  public DatasetConfigDTO setRealtime(boolean realtime) {
    this.realtime = realtime;
    return this;
  }

  @Deprecated // use completenessDelay
  public TimeGranularity getExpectedDelay() {
    return expectedDelay;
  }

  @Deprecated // use completenessDelay
  public DatasetConfigDTO setExpectedDelay(TimeGranularity expectedDelay) {
    this.expectedDelay = expectedDelay;
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
            that.preAggregatedKeyword) && Objects
        .equals(nonAdditiveBucketSize, that.nonAdditiveBucketSize)
        && nonAdditiveBucketUnit == that.nonAdditiveBucketUnit
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(dataset, dimensions, timeColumn, timeUnit, timeDuration, timeFormat,
            timezone,
            dataSource, owners, active, additive, dimensionsHaveNoPreAggregation,
            preAggregatedKeyword,
            nonAdditiveBucketSize, nonAdditiveBucketUnit, realtime, properties);
  }

  /**
   * Returns the granularity of a bucket (i.e., a data point) of this dataset if such information is
   * available.
   *
   * The granularity that is defined in dataset configuration actually defines the granularity of
   * the timestamp of each
   * data point. For instance, timestamp's granularity (in database) could be 1-MILLISECONDS but the
   * bucket's
   * granularity is 1-HOURS. In real applications, the granularity of timestamp is never being used.
   * Therefore, this
   * method returns the actual granularity of the bucket (data point) if such information is
   * available in the cnofig.
   * This information is crucial for non-additive dataset.
   *
   * @return the granularity of a bucket (a data point) of this dataset.
   */
  @Deprecated
  public TimeGranularity bucketTimeGranularity() {
    if (bucketTimeGranularity == null) {
      Integer size =
          getNonAdditiveBucketSize() != null ? getNonAdditiveBucketSize() : getTimeDuration();
      TimeUnit timeUnit =
          getNonAdditiveBucketUnit() != null ? getNonAdditiveBucketUnit() : getTimeUnit();
      bucketTimeGranularity =
          (size != null && timeUnit != null) ? new TimeGranularity(size, timeUnit) : null;
    }
    return bucketTimeGranularity;
  }
}
