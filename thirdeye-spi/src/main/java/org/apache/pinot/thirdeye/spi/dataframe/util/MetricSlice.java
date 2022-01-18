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

package org.apache.pinot.thirdeye.spi.dataframe.util;

import static org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils.extractFilterPredicates;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.rootcause.util.FilterPredicate;
import org.apache.pinot.thirdeye.spi.rootcause.util.ParsedUrn;
import org.joda.time.DateTime;

/**
 * Selector for time series and aggregate values of a specific metric, independent of
 * data source.
 */
public final class MetricSlice {

  public static final TimeGranularity NATIVE_GRANULARITY = new TimeGranularity(0,
      TimeUnit.MILLISECONDS);

  public final long metricId;
  public final long start;
  public final long end;
  public final Multimap<String, String> filters;
  public final TimeGranularity granularity;

  MetricSlice(long metricId, long start, long end, Multimap<String, String> filters,
      TimeGranularity granularity) {
    this.metricId = metricId;
    this.start = start;
    this.end = end;
    this.filters = filters;
    this.granularity = granularity;
  }

  public static MetricSlice from(long metricId, long start, long end) {
    return new MetricSlice(metricId, start, end, ArrayListMultimap.create(),
        NATIVE_GRANULARITY);
  }

  public static MetricSlice from(long metricId, long start, long end,
      Multimap<String, String> filters) {
    return new MetricSlice(metricId, start, end, filters, NATIVE_GRANULARITY);
  }

  /**
   * Filters in format dim1=val1, dim2!=val2
   * */
  public static MetricSlice from(long metricId, long start, long end,
      List<String> filters, TimeGranularity granularity) {
    List<FilterPredicate> predicates = extractFilterPredicates(filters);
    Multimap<String, String> filtersMap = ParsedUrn.toFiltersMap(predicates);
    return new MetricSlice(metricId, start, end, filtersMap, granularity);
  }

  public static MetricSlice from(long metricId, long start, long end,
      Multimap<String, String> filters, TimeGranularity granularity) {
    return new MetricSlice(metricId, start, end, filters, granularity);
  }

  public long getMetricId() {
    return metricId;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public Multimap<String, String> getFilters() {
    return filters;
  }

  public TimeGranularity getGranularity() {
    return granularity;
  }

  public MetricSlice withStart(long start) {
    return new MetricSlice(metricId, start, end, filters, granularity);
  }

  public MetricSlice withEnd(long end) {
    return new MetricSlice(metricId, start, end, filters, granularity);
  }

  public MetricSlice withFilters(Multimap<String, String> filters) {
    return new MetricSlice(metricId, start, end, filters, granularity);
  }

  public MetricSlice withGranularity(TimeGranularity granularity) {
    return new MetricSlice(metricId, start, end, filters, granularity);
  }

  /**
   * check if current metric slice contains another metric slice
   */
  public boolean containSlice(MetricSlice slice) {
    return slice.metricId == this.metricId && slice.granularity.equals(this.granularity) && slice
        .getFilters().equals(this.getFilters()) &&
        slice.start >= this.start && slice.end <= this.end;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetricSlice that = (MetricSlice) o;
    return metricId == that.metricId && start == that.start && end == that.end && Objects
        .equals(filters, that.filters)
        && Objects.equals(granularity, that.granularity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricId, start, end, filters, granularity);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("metricId", metricId)
        .add("start", new DateTime(start))
        .add("end", new DateTime(end))
        .add("filters", filters)
        .add("granularity", granularity)
        .toString();
  }
}
