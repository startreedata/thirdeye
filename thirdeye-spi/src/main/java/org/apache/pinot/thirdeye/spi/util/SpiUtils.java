package org.apache.pinot.thirdeye.spi.util;

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.thirdeye.spi.common.time.TimeGranularity;
import org.apache.pinot.thirdeye.spi.common.time.TimeSpec;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.MetricConfigBean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Partial;
import org.joda.time.PeriodType;

public class SpiUtils {
  private SpiUtils() {}

  public static String constructMetricAlias(String datasetName, String metricName) {
    return datasetName + MetricConfigBean.ALIAS_JOINER + metricName;
  }

  public static String getTimeFormatString(DatasetConfigDTO datasetConfig) {
    String timeFormat = datasetConfig.getTimeFormat();
    if (timeFormat.startsWith(DateTimeFieldSpec.TimeFormat.SIMPLE_DATE_FORMAT.toString())) {
      timeFormat = getSDFPatternFromTimeFormat(timeFormat);
    }
    return timeFormat;
  }

  private static String getSDFPatternFromTimeFormat(String timeFormat) {
    String pattern = timeFormat;
    String[] tokens = timeFormat.split(":", 2);
    if (tokens.length == 2) {
      pattern = tokens[1];
    }
    return pattern;
  }

  public static List<Range<DateTime>> computeTimeRanges(TimeGranularity granularity, DateTime start,
      DateTime end) {
    List<Range<DateTime>> timeranges = new ArrayList<>();
    if (granularity == null) {
      timeranges.add(Range.closedOpen(start, end));
      return timeranges;
    }
    DateTime current = start;
    DateTime newCurrent = null;
    while (current.isBefore(end)) {
      newCurrent = increment(current, granularity);
      timeranges.add(Range.closedOpen(current, newCurrent));
      current = newCurrent;
    }
    return timeranges;
  }

  public static DateTime increment(DateTime input, TimeGranularity granularity) {
    DateTime output;
    switch (granularity.getUnit()) {
      case DAYS:
        output = input.plusDays(granularity.getSize());
        break;
      case HOURS:
        output = input.plusHours(granularity.getSize());
        break;
      case MILLISECONDS:
        output = input.plusMillis(granularity.getSize());
        break;
      case MINUTES:
        output = input.plusMinutes(granularity.getSize());
        break;
      case SECONDS:
        output = input.plusSeconds(granularity.getSize());
        break;
      default:
        throw new IllegalArgumentException("Timegranularity:" + granularity + " not supported");
    }
    return output;
  }

  /**
   * Returns partial to zero out date fields based on period type
   *
   * @return partial
   */
  public static Partial makeOrigin(PeriodType type) {
    List<DateTimeFieldType> fields = new ArrayList<>();

    if (PeriodType.millis().equals(type)) {
      // left blank

    } else if (PeriodType.seconds().equals(type)) {
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.minutes().equals(type)) {
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.hours().equals(type)) {
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.days().equals(type)) {
      fields.add(DateTimeFieldType.hourOfDay());
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.months().equals(type)) {
      fields.add(DateTimeFieldType.dayOfMonth());
      fields.add(DateTimeFieldType.hourOfDay());
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else {
      throw new IllegalArgumentException(String.format("Unsupported PeriodType '%s'", type));
    }

    int[] zeros = new int[fields.size()];
    Arrays.fill(zeros, 0);

    // workaround for dayOfMonth > 0 constraint
    if (PeriodType.months().equals(type)) {
      zeros[0] = 1;
    }

    return new Partial(fields.toArray(new DateTimeFieldType[fields.size()]), zeros);
  }

  public static DateTimeZone getDateTimeZone(final DatasetConfigDTO datasetConfig) {
    final String timezone = datasetConfig != null
        ? datasetConfig.getTimezone()
        : TimeSpec.DEFAULT_TIMEZONE;
    return DateTimeZone.forID(timezone);
  }

  /**
   * Given time granularity and start time (with local time zone information), returns the bucket
   * index of the current time (with local time zone information).
   *
   * The reason to use this method to calculate the bucket index is to align the shifted data point
   * due to daylight saving time to the correct bucket index. Note that this method have no effect
   * if the input time use UTC timezone.
   *
   * For instance, considering March 13th 2016, the day DST takes effect. Assume that our daily
   * data whose timestamp is aligned at 0 am at each day, then the data point on March 14th would
   * be actually aligned to 13th's bucket. Because the two data point only has 23 hours difference.
   * Therefore, we cannot calculate the bucket index simply divide the difference between timestamps
   * by millis of 24 hours.
   *
   * We don't need to consider the case of HOURS because the size of a bucket does not change when
   * the time granularity is smaller than DAYS. In DAYS, the bucket size could be 23, 24, or 25
   * hours due to DST. In HOURS or anything smaller, the bucket size does not change. Hence, we
   * simply compute the bucket index using one fixed bucket size (i.e., interval).
   *
   * @param granularity the time granularity of the bucket
   * @param start the start time of the first bucket
   * @param current the current time
   * @return the bucket index of current time
   */
  public static int computeBucketIndex(TimeGranularity granularity, DateTime start,
      DateTime current) {
    int index = -1;
    switch (granularity.getUnit()) {
      case DAYS:
        Days d = Days.daysBetween(start, current);
        index = d.getDays() / granularity.getSize();
        break;
      default:
        long interval = granularity.toMillis();
        index = (int) ((current.getMillis() - start.getMillis()) / interval);
    }
    return index;
  }
}
