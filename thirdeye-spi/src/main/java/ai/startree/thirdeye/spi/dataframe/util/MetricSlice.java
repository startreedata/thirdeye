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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * Selector for time series and aggregate values of a specific metric, independent of
 * data source.
 */
public final class MetricSlice {

  private final long metricId;
  private final Interval interval;
  private final Multimap<String, String> filters;
  // make below fields non nullable if possible
  private final @Nullable
  TimeGranularity granularity;
  private final @Nullable String datasetName;


  MetricSlice(long metricId, final @NonNull Interval interval, Multimap<String, String> filters,
      @Nullable TimeGranularity granularity, @Nullable final String datasetName) {
    this.metricId = metricId;
    this.interval = interval;
    this.filters = filters;
    this.granularity = granularity;
    this.datasetName = datasetName;
  }

  public static MetricSlice from (final long metricId, final Interval interval, final String datasetName) {
    return new MetricSlice(metricId, interval, ArrayListMultimap.create(), null, datasetName);
  }

  public static MetricSlice from(final long metricId, final Interval interval,
      final Multimap<String, String> filters, final String datasetName) {
    return new MetricSlice(metricId, interval, filters, null, datasetName);
  }

  /**
   * Filters in format dim1=val1, dim2!=val2
   */
  public static MetricSlice from(final long metricId, final Interval interval,
      final List<String> filters, TimeGranularity granularity, final String datasetName) {
    List<FilterPredicate> predicates = EntityUtils.extractFilterPredicates(filters);
    Multimap<String, String> filtersMap = ParsedUrn.toFiltersMap(predicates);
    return new MetricSlice(metricId, interval, filtersMap, granularity, datasetName);
  }

  public static MetricSlice from(final long metricId, final Interval interval,
      final Multimap<String, String> filters, TimeGranularity granularity, final String datasetName) {
    return new MetricSlice(metricId, interval, filters, granularity, datasetName);
  }

  @Deprecated
  public static MetricSlice from(long metricId, long start, long end) {
    return new MetricSlice(metricId, new Interval(start, end, DateTimeZone.UTC), ArrayListMultimap.create(), null, null);
  }

  @Deprecated
  public static MetricSlice from(long metricId, long start, long end,
      Multimap<String, String> filters) {
    return new MetricSlice(metricId, new Interval(start, end, DateTimeZone.UTC), filters, null, null);
  }

  @Deprecated
  public static MetricSlice from(long metricId, long start, long end,
      Multimap<String, String> filters, TimeGranularity granularity) {
    return new MetricSlice(metricId, new Interval(start, end, DateTimeZone.UTC), filters, granularity, null);
  }

  public long getMetricId() {
    return metricId;
  }

  @Deprecated
  public long getStartMillis() {
    return interval.getStartMillis();
  }

  @Deprecated
  public long getEndMillis() {
    return interval.getEndMillis();
  }

  public DateTime getStart() {return interval.getStart();}

  public DateTime getEnd() {return interval.getEnd();}

  public Interval getInterval() {return interval;}

  public Multimap<String, String> getFilters() {
    return filters;
  }

  public @Nullable TimeGranularity getGranularity() {
    return granularity;
  }

  public @Nullable String getDatasetName() {
    return datasetName;
  }

  public MetricSlice withStart(DateTime start) {
    return new MetricSlice(metricId, interval.withStart(start), filters, granularity, datasetName);
  }

  @Deprecated
  public MetricSlice withStart(long start) {
    return new MetricSlice(metricId, interval.withStartMillis(start), filters, granularity, datasetName);
  }

  public MetricSlice withEnd(DateTime end) {
    return new MetricSlice(metricId, interval.withEnd(end), filters, granularity, datasetName);
  }

  @Deprecated
  public MetricSlice withEnd(long end) {
    return new MetricSlice(metricId, interval.withEndMillis(end), filters, granularity, datasetName);
  }

  public MetricSlice withFilters(Multimap<String, String> filters) {
    return new MetricSlice(metricId, interval, filters, granularity, datasetName);
  }

  public MetricSlice withGranularity(TimeGranularity granularity) {
    return new MetricSlice(metricId, interval, filters, granularity, datasetName);
  }

  /**
   * check if current metric slice contains another metric slice
   */
  public boolean containSlice(MetricSlice slice) {
    return slice.metricId == this.metricId && Objects.equals(slice.granularity, this.granularity) && slice
        .getFilters().equals(this.getFilters()) &&
        Objects.equals(slice.datasetName, this.datasetName) &&
        this.interval.contains(slice.interval);
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
    return metricId == that.metricId && Objects.equals(interval, that.interval) && Objects
        .equals(filters, that.filters)
        && Objects.equals(granularity, that.granularity)
        && Objects.equals(datasetName, that.datasetName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricId, interval, filters, granularity);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("metricId", metricId)
        .add("start", interval.getStart())
        .add("end", interval.getEnd())
        .add("filters", filters)
        .add("granularity", granularity)
        .add("datasetName", datasetName)
        .toString();
  }
}
