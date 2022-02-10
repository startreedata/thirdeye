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

import ai.startree.thirdeye.datasource.timeseries.TimeSeriesRow.TimeSeriesMetric;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TimeSeriesResponse {

  int numRows;
  private final Set<String> metrics = new TreeSet<>();
  private final Set<List<String>> dimensions = new HashSet<>();
  private final List<TimeSeriesRow> rows;

  public TimeSeriesResponse(List<TimeSeriesRow> rows) {
    this.rows = rows;
    for (TimeSeriesRow row : rows) {
      for (TimeSeriesMetric metric : row.getMetrics()) {
        metrics.add(metric.getMetricName());
      }
      dimensions.add(row.getDimensionNames());
    }
    numRows = rows.size();
  }

  public int getNumRows() {
    return numRows;
  }

  public Set<String> getMetrics() {
    return metrics;
  }

  public Set<List<String>> getDimensions() {
    return dimensions;
  }

  public TimeSeriesRow getRow(int index) {
    return rows.get(index);
  }

  public List<TimeSeriesRow> getRows() {
    return ImmutableList.copyOf(rows);
  }

  public static class Builder {

    List<TimeSeriesRow> rows = new ArrayList<>();

    public void add(TimeSeriesRow row) {
      rows.add(row);
    }

    TimeSeriesResponse build() {
      return new TimeSeriesResponse(rows);
    }
  }
}
