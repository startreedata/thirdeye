/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.datasource.timeseries;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

public class TimeSeriesRequest {

  private String collectionName;
  private List<MetricExpression> metricExpressions;
  /**
   * Dimensions to group by. multiple dimensions will generate multiple client requests, as opposed
   * to a single request grouped by multiple dimensions (not supported).
   */
  private List<String> groupByDimensions;
  /**
   * easy way to represent AND of multiple dimensions
   * for example
   * filterMap = new HashMap
   * filterMap.put("country", "us")
   * filterMap.put("device", "android")
   * is equivalent to setting filterClause country='us' and device='android'
   */
  private Multimap<String, String> filterSet;
  /**
   * Allows one to specify complex boolean expressions, advanced usage
   */
  private String filterClause;
  // TIME RELATED PARAMETERs
  private DateTime start;
  private DateTime end;
  private TimeGranularity aggregationTimeGranularity;
  private boolean isEndDateInclusive = false;

  public TimeSeriesRequest() {
  }

  /**
   * TODO use builder pattern?
   */
  public TimeSeriesRequest(TimeSeriesRequest timeSeriesRequest) {
    this.collectionName = timeSeriesRequest.collectionName;
    if (timeSeriesRequest.metricExpressions != null) {
      this.metricExpressions = new ArrayList<>(timeSeriesRequest.metricExpressions);
    }
    if (timeSeriesRequest.groupByDimensions != null) {
      this.groupByDimensions = new ArrayList<>(timeSeriesRequest.groupByDimensions);
    }
    if (timeSeriesRequest.filterSet != null) {
      this.filterSet = ArrayListMultimap.create(timeSeriesRequest.filterSet);
    }
    this.filterClause = timeSeriesRequest.filterClause;
    this.start = timeSeriesRequest.start;
    this.end = timeSeriesRequest.end;
    if (timeSeriesRequest.aggregationTimeGranularity != null) {
      this.aggregationTimeGranularity =
          new TimeGranularity(timeSeriesRequest.aggregationTimeGranularity.getSize(),
              timeSeriesRequest.aggregationTimeGranularity.getUnit());
    }
    this.isEndDateInclusive = timeSeriesRequest.isEndDateInclusive;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public List<MetricExpression> getMetricExpressions() {
    return metricExpressions;
  }

  public void setMetricExpressions(List<MetricExpression> metricExpressions) {
    this.metricExpressions = metricExpressions;
  }

  public List<String> getGroupByDimensions() {
    return groupByDimensions;
  }

  public void setGroupByDimensions(List<String> groupByDimensions) {
    this.groupByDimensions = groupByDimensions;
  }

  public Multimap<String, String> getFilterSet() {
    return filterSet;
  }

  public void setFilterSet(Multimap<String, String> filterSet) {
    this.filterSet = filterSet;
  }

  public String getFilterClause() {
    return filterClause;
  }

  public void setFilterClause(String filterClause) {
    this.filterClause = filterClause;
  }

  /**
   * Get start time, inclusive.
   */
  public DateTime getStart() {
    return start;
  }

  /**
   * Set start time, inclusive.
   */
  public void setStart(DateTime start) {
    this.start = start;
  }

  public DateTime getEnd() {
    return end;
  }

  public void setEnd(DateTime end) {
    this.end = end;
  }

  public TimeGranularity getAggregationTimeGranularity() {
    return aggregationTimeGranularity;
  }

  public void setAggregationTimeGranularity(TimeGranularity aggregationTimeGranularity) {
    this.aggregationTimeGranularity = aggregationTimeGranularity;
  }

  public boolean isEndDateInclusive() {
    return isEndDateInclusive;
  }

  public void setEndDateInclusive(boolean includeEndDate) {
    this.isEndDateInclusive = includeEndDate;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("collectionName", collectionName)
        .add("metricExpressions", metricExpressions).add("groupByDimensions", groupByDimensions)
        .add("filterSet", filterSet).add("filterClause", filterClause).add("start", start)
        .add("end", end).add("aggregationTimeGranularity", aggregationTimeGranularity).toString();
  }
}
