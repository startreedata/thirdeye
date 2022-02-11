/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.dataframe.util;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.rootcause.util.FilterPredicate;
import ai.startree.thirdeye.spi.rootcause.util.ParsedUrn;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;

/**
 * Selector for time series and aggregate values of a specific metric, independent of
 * data source.
 */
public final class MetricSlice {

  private final long metricId;
  private final long start;
  private final long end;
  private final Multimap<String, String> filters;
  private final @Nullable
  TimeGranularity granularity;

  MetricSlice(long metricId, long start, long end, Multimap<String, String> filters,
      TimeGranularity granularity) {
    this.metricId = metricId;
    this.start = start;
    this.end = end;
    this.filters = filters;
    this.granularity = granularity;
  }

  public static MetricSlice from(long metricId, long start, long end) {
    return new MetricSlice(metricId, start, end, ArrayListMultimap.create(), null);
  }

  public static MetricSlice from(long metricId, long start, long end,
      Multimap<String, String> filters) {
    return new MetricSlice(metricId, start, end, filters, null);
  }

  /**
   * Filters in format dim1=val1, dim2!=val2
   */
  public static MetricSlice from(long metricId, long start, long end,
      List<String> filters, TimeGranularity granularity) {
    List<FilterPredicate> predicates = EntityUtils.extractFilterPredicates(filters);
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

  public @Nullable TimeGranularity getGranularity() {
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
