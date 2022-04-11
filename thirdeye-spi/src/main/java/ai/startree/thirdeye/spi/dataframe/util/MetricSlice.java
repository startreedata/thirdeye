/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.dataframe.util;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
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

  //todo cyril make the metric slice directly contain the datasetDTO

  private final @NonNull MetricConfigDTO metricConfigDTO;
  private final Interval interval;
  private final Multimap<String, String> filters;
  // make below fields non nullable if possible
  private final @Nullable
  TimeGranularity granularity;
  private final @Nullable String datasetName;


  MetricSlice(final @NonNull MetricConfigDTO metricConfigDTO, final @NonNull Interval interval, Multimap<String, String> filters,
      @Nullable TimeGranularity granularity, @Nullable final String datasetName) {
    this.metricConfigDTO = metricConfigDTO;
    this.interval = interval;
    this.filters = filters;
    this.granularity = granularity;
    this.datasetName = datasetName;
  }

  public static MetricSlice from (final @NonNull MetricConfigDTO metricConfigDTO, final Interval interval, final String datasetName) {
    return new MetricSlice(metricConfigDTO, interval, ArrayListMultimap.create(), null, datasetName);
  }

  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO, final Interval interval,
      final Multimap<String, String> filters, final String datasetName) {
    return new MetricSlice(metricConfigDTO, interval, filters, null, datasetName);
  }

  /**
   * Filters in format dim1=val1, dim2!=val2
   */
  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO, final Interval interval,
      final List<String> filters, TimeGranularity granularity, final String datasetName) {
    List<FilterPredicate> predicates = EntityUtils.extractFilterPredicates(filters);
    Multimap<String, String> filtersMap = ParsedUrn.toFiltersMap(predicates);
    return new MetricSlice(metricConfigDTO, interval, filtersMap, granularity, datasetName);
  }

  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO, final Interval interval,
      final Multimap<String, String> filters, TimeGranularity granularity, final String datasetName) {
    return new MetricSlice(metricConfigDTO, interval, filters, granularity, datasetName);
  }

  @Deprecated
  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO, long start, long end) {
    return new MetricSlice(metricConfigDTO, new Interval(start, end, DateTimeZone.UTC), ArrayListMultimap.create(), null, null);
  }

  @Deprecated
  public static MetricSlice from(final@NonNull MetricConfigDTO metricConfigDTO, long start, long end,
      Multimap<String, String> filters) {
    return new MetricSlice(metricConfigDTO, new Interval(start, end, DateTimeZone.UTC), filters, null, null);
  }

  @Deprecated
  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO, long start, long end,
      Multimap<String, String> filters, TimeGranularity granularity) {
    return new MetricSlice(metricConfigDTO, new Interval(start, end, DateTimeZone.UTC), filters, granularity, null);
  }

  @Deprecated
  public long getMetricId() {
    return metricConfigDTO.getId();
  }

  @Deprecated
  public long getStartMillis() {
    return interval.getStartMillis();
  }

  @Deprecated
  public long getEndMillis() {
    return interval.getEndMillis();
  }

  public @NonNull MetricConfigDTO getMetricConfigDTO() {
    return metricConfigDTO;
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
    return new MetricSlice(metricConfigDTO, interval.withStart(start), filters, granularity, datasetName);
  }

  @Deprecated
  public MetricSlice withStart(long start) {
    return new MetricSlice(metricConfigDTO, interval.withStartMillis(start), filters, granularity, datasetName);
  }

  public MetricSlice withEnd(DateTime end) {
    return new MetricSlice(metricConfigDTO, interval.withEnd(end), filters, granularity, datasetName);
  }

  @Deprecated
  public MetricSlice withEnd(long end) {
    return new MetricSlice(metricConfigDTO, interval.withEndMillis(end), filters, granularity, datasetName);
  }

  public MetricSlice withFilters(Multimap<String, String> filters) {
    return new MetricSlice(metricConfigDTO, interval, filters, granularity, datasetName);
  }

  public MetricSlice withGranularity(TimeGranularity granularity) {
    return new MetricSlice(metricConfigDTO, interval, filters, granularity, datasetName);
  }

  /**
   * check if current metric slice contains another metric slice
   */
  public boolean containSlice(MetricSlice slice) {
    return Objects.equals(slice.metricConfigDTO, this.metricConfigDTO) && Objects.equals(slice.granularity, this.granularity) && slice
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
    return Objects.equals(metricConfigDTO, that.metricConfigDTO) && Objects.equals(interval, that.interval) && Objects
        .equals(filters, that.filters)
        && Objects.equals(granularity, that.granularity)
        && Objects.equals(datasetName, that.datasetName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricConfigDTO, interval, filters, granularity);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("metricId", metricConfigDTO.getId())
        .add("start", interval.getStart())
        .add("end", interval.getEnd())
        .add("filters", filters)
        .add("granularity", granularity)
        .add("datasetName", datasetName)
        .toString();
  }
}
