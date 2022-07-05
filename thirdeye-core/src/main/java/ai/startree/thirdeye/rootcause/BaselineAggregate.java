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
package ai.startree.thirdeye.rootcause;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Grouping;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.detection.Baseline;
import ai.startree.thirdeye.spi.detection.BaselineAggregateType;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * Synthetic baseline from a list of time offsets, aggregated with a user-specified function.
 *
 * @see BaselineAggregateType
 */
public class BaselineAggregate implements Baseline {

  private static final String COL_KEY = Grouping.GROUP_KEY;

  private final BaselineAggregateType type;
  private final List<Period> offsets;
  private final DateTimeZone timeZone;
  private final PeriodType periodType;

  private BaselineAggregate(BaselineAggregateType type, List<Period> offsets, DateTimeZone timezone,
      PeriodType periodType) {
    this.type = type;
    this.offsets = offsets;
    this.timeZone = timezone;
    this.periodType = periodType;
  }

  /**
   * Helper to eliminate duplicate timestamps via averaging of values
   *
   * @param df timeseries dataframe
   * @return de-duplicated dataframe
   */
  private static DataFrame eliminateDuplicates(DataFrame df) {
    List<String> aggExpressions = new ArrayList<>();
    for (String seriesName : df.getIndexNames()) {
      aggExpressions.add(String.format("%s:FIRST", seriesName));
    }
    aggExpressions.add(COL_VALUE + ":MEAN");

    DataFrame res = df.groupByValue(df.getIndexNames()).aggregate(aggExpressions)
        .dropSeries(COL_KEY);

    return res.setIndex(df.getIndexNames());
  }

  /**
   * Helper to apply map operation to partially incomplete row, i.e. a row
   * that contains null values.
   *
   * This is the important function that performs the function mean/sum/etc in eg "mean2w".
   *
   * @param df dataframe
   * @param f function
   * @param colNames column to apply function to
   * @return double series
   */
  // TODO move this into DataFrame API?
  private static DoubleSeries mapWithNull(DataFrame df, Series.DoubleFunction f,
      List<String> colNames) {
    double[] values = new double[df.size()];

    double[] row = new double[colNames.size()];
    for (int i = 0; i < df.size(); i++) {
      int offset = 0;
      for (int j = 0; j < colNames.size(); j++) {
        if (!df.isNull(colNames.get(j), i)) {
          row[offset++] = df.getDouble(colNames.get(j), i);
        }
      }

      if (offset <= 0) {
        values[i] = DoubleSeries.NULL;
      } else if (offset >= colNames.size()) {
        values[i] = f.apply(row);
      } else {
        values[i] = f.apply(Arrays.copyOf(row, offset));
      }
    }

    return DoubleSeries.buildFrom(values);
  }

  /**
   * Returns an instance of BaselineAggregate for the specified type and offsets
   *
   * @param type aggregation type
   * @param offsets time offsets
   * @return BaselineAggregate with given type and offsets
   * @see BaselineAggregateType
   */
  public static BaselineAggregate fromOffsets(BaselineAggregateType type, List<Period> offsets,
      DateTimeZone timeZone) {
    if (offsets.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one offset");
    }

    PeriodType periodType = offsets.get(0).getPeriodType();
    for (Period p : offsets) {
      if (!periodType.equals(p.getPeriodType())) {
        throw new IllegalArgumentException(String
            .format("Expected uniform period type but found '%s' and '%s'", periodType,
                p.getPeriodType()));
      }
    }

    return new BaselineAggregate(type, offsets, timeZone, periodType);
  }

  /**
   * Returns an instance of BaselineAggregate for the specified type and {@code numMonths} offsets
   * computed on a consecutive month-over-month basis starting with a lag of {@code offsetMonths}.
   * <br/><b>NOTE:</b> this will apply DST correction
   *
   * @param type aggregation type
   * @param numMonths number of consecutive months
   * @param offsetMonths lag for starting consecutive months
   * @param timeZone time zone
   * @return BaselineAggregate with given type and monthly offsets
   * @see BaselineAggregateType
   */
  public static BaselineAggregate fromMonthOverMonth(BaselineAggregateType type, int numMonths,
      int offsetMonths, DateTimeZone timeZone) {
    List<Period> offsets = new ArrayList<>();
    for (int i = 0; i < numMonths; i++) {
      offsets.add(new Period(0, -1 * (i + offsetMonths), 0, 0, 0, 0, 0, 0, PeriodType.months()));
    }
    return new BaselineAggregate(type, offsets, timeZone, PeriodType.months());
  }

  /**
   * Returns an instance of BaselineAggregate for the specified type and {@code numWeeks} offsets
   * computed on a consecutive week-over-week basis starting with a lag of {@code offsetWeeks}.
   * <br/><b>NOTE:</b> this will apply DST correction (modeled as 7 days)
   *
   * @param type aggregation type
   * @param numWeeks number of consecutive weeks
   * @param offsetWeeks lag for starting consecutive weeks
   * @param timeZone time zone
   * @return BaselineAggregate with given type and weekly offsets
   * @see BaselineAggregateType
   */
  public static BaselineAggregate fromWeekOverWeek(BaselineAggregateType type, int numWeeks,
      int offsetWeeks, DateTimeZone timeZone) {
    List<Period> offsets = new ArrayList<>();
    for (int i = 0; i < numWeeks; i++) {
      offsets.add(new Period(0, 0, 0, -1 * 7 * (i + offsetWeeks), 0, 0, 0, 0, PeriodType.days()));
    }
    return new BaselineAggregate(type, offsets, timeZone, PeriodType.days());
  }

  /**
   * Returns an instance of BaselineAggregate for the specified type and {@code numDays} offsets
   * computed on a consecutive day-over-day basis starting with a lag of {@code offsetDays}.
   * <br/><b>NOTE:</b> this will apply DST correction
   *
   * @param type aggregation type
   * @param numDays number of consecutive weeks
   * @param offsetDays lag for starting consecutive weeks
   * @param timeZone time zone
   * @return BaselineAggregate with given type and daily offsets
   * @see BaselineAggregateType
   */
  public static BaselineAggregate fromDayOverDay(BaselineAggregateType type, int numDays,
      int offsetDays, DateTimeZone timeZone) {
    List<Period> offsets = new ArrayList<>();
    for (int i = 0; i < numDays; i++) {
      offsets.add(new Period(0, 0, 0, -1 * (i + offsetDays), 0, 0, 0, 0, PeriodType.days()));
    }
    return new BaselineAggregate(type, offsets, timeZone, PeriodType.days());
  }

  /**
   * Returns an instance of BaselineAggregate for the specified type and {@code numDays} offsets
   * computed on a consecutive day-over-day basis starting with a lag of {@code offsetHours}.
   * <br/><b>NOTE:</b> this will <b>NOT</b> apply DST correction
   *
   * @param type aggregation type
   * @param numHours number of consecutive weeks
   * @param offsetHours lag for starting consecutive weeks
   * @param timeZone time zone
   * @return BaselineAggregate with given type and daily offsets
   * @see BaselineAggregateType
   */
  public static BaselineAggregate fromHourOverHour(BaselineAggregateType type, int numHours,
      int offsetHours, DateTimeZone timeZone) {
    List<Period> offsets = new ArrayList<>();
    for (int i = 0; i < numHours; i++) {
      offsets.add(new Period(0, 0, 0, 0, -1 * (i + offsetHours), 0, 0, 0, PeriodType.hours()));
    }
    return new BaselineAggregate(type, offsets, timeZone, PeriodType.hours());
  }

  public BaselineAggregate withType(BaselineAggregateType type) {
    return new BaselineAggregate(type, this.offsets, this.timeZone, this.periodType);
  }

  public BaselineAggregate withOffsets(List<Period> offsets) {
    return new BaselineAggregate(this.type, offsets, this.timeZone, this.periodType);
  }

  public BaselineAggregate withTimeZone(DateTimeZone timeZone) {
    return new BaselineAggregate(this.type, this.offsets, timeZone, this.periodType);
  }

  public BaselineAggregate withPeriodType(PeriodType periodType) {
    return new BaselineAggregate(this.type, this.offsets, this.timeZone, periodType);
  }

  @Override
  public List<MetricSlice> scatter(MetricSlice slice) {
    List<MetricSlice> slices = new ArrayList<>();
    for (Period offset : this.offsets) {
      slices.add(slice
          .withStart(new DateTime(slice.getStart(), this.timeZone).plus(offset))
          .withEnd(new DateTime(slice.getEnd(), this.timeZone).plus(offset)));
    }
    return slices;
  }

  private Map<MetricSlice, DataFrame> filter(MetricSlice slice, Map<MetricSlice, DataFrame> data) {
    Map<MetricSlice, DataFrame> output = new HashMap<>();
    Set<MetricSlice> patterns = new HashSet<>(scatter(slice));

    for (Map.Entry<MetricSlice, DataFrame> entry : data.entrySet()) {
      if (patterns.contains(entry.getKey())) {
        output.put(entry.getKey(), entry.getValue());
      }
    }

    return output;
  }

  @Override
  // todo cyril gather should not require the reference slice imo
  public DataFrame gather(final MetricSlice referenceSlice, Map<MetricSlice, DataFrame> data) {
    // prepare the slices data
    Map<MetricSlice, DataFrame> filtered = this.filter(referenceSlice, data);
    Map<String, DataFrame> preparedSlices = new HashMap<>();
    for (Map.Entry<MetricSlice, DataFrame> entry : filtered.entrySet()) {
      optional(prepareSliceData(entry.getKey(), entry.getValue(), referenceSlice))
          .ifPresent(e -> preparedSlices.put(e.getKey(), e.getValue()));
    }

    // put all prepared slice data in the same dataframe
    DataFrame output = new DataFrame(COL_TIME, LongSeries.empty());
    for (DataFrame preparedSlice : preparedSlices.values()) {
      if (output.isEmpty()) {
        output = preparedSlice;
      } else {
        output = output.joinOuter(preparedSlice);
      }
    }

    // aggregate and clean
    List<String> sliceColumnNames = new ArrayList<>(preparedSlices.keySet());
    return output
        // aggregate slices
        .addSeries(COL_VALUE, mapWithNull(output, this.type.getFunction(), sliceColumnNames))
        // align times
        .addSeries(COL_TIME,
            this.toTimestampSeries(referenceSlice.getStartMillis(), output.getLongs(COL_TIME)))
        // drop slice columns
        .dropSeries(sliceColumnNames)
        // filter by original time range - (not an in-place operation)
        .filter(output.getLongs(COL_TIME).gte(referenceSlice.getStartMillis())
            .and(output.getLongs(COL_TIME).lt(referenceSlice.getEndMillis())))
        .dropNull(output.getIndexNames());
  }

  private Map.Entry<String, DataFrame> prepareSliceData(final MetricSlice slice,
      final DataFrame data,
      final MetricSlice referenceSlice) {
    // check if offset corresponds to configuration
    Period period = new Period(
        new DateTime(referenceSlice.getStartMillis(), this.timeZone),
        new DateTime(slice.getStartMillis(), this.timeZone),
        this.periodType);
    if (!offsets.contains(period)) {
      return null;
    }

    String sliceColName = String.valueOf(slice.getStartMillis());
    DataFrame dfTransform = new DataFrame(data);
    dfTransform.addSeries(COL_TIME,
        this.toVirtualSeries(slice.getStartMillis(), dfTransform.getLongs(COL_TIME)));
    dfTransform = eliminateDuplicates(dfTransform);
    dfTransform.renameSeries(COL_VALUE, sliceColName);

    return Map.entry(sliceColName, dfTransform);
  }

  /**
   * Transform UTC timestamps into relative day-time-of-day timestamps
   *
   * @param origin origin timestamp
   * @param timestampSeries timestamp series
   * @return day-time-of-day series
   */
  private LongSeries toVirtualSeries(long origin, LongSeries timestampSeries) {
    final DateTime dateOrigin = new DateTime(origin, this.timeZone)
        .withFields(SpiUtils.makeOrigin(this.periodType));
    return timestampSeries.map(this.makeTimestampToVirtualFunction(dateOrigin));
  }

  /**
   * Transform day-time-of-day timestamps into UTC timestamps
   *
   * @param origin origin timestamp
   * @param virtualSeries day-time-of-day series
   * @return utc timestamp series
   */
  private LongSeries toTimestampSeries(long origin, LongSeries virtualSeries) {
    final DateTime dateOrigin = new DateTime(origin, this.timeZone)
        .withFields(SpiUtils.makeOrigin(this.periodType));
    return virtualSeries.map(this.makeVirtualToTimestampFunction(dateOrigin));
  }

  /**
   * Returns a conversion function from utc timestamps to virtual, relative timestamps based
   * on period type and an origin
   *
   * @param origin origin to base relative timestamp on
   * @return LongFunction for converting to relative timestamps
   */
  private Series.LongFunction makeTimestampToVirtualFunction(final DateTime origin) {
    if (PeriodType.millis().equals(this.periodType)) {
      return values -> values[0] - origin.getMillis();
    } else if (PeriodType.seconds().equals(this.periodType)) {
      return values -> {
        DateTime dateTime = new DateTime(values[0], BaselineAggregate.this.timeZone);
        long seconds = new Period(origin, dateTime, BaselineAggregate.this.periodType)
            .getSeconds();
        long millis = dateTime.getMillisOfSecond();
        return seconds * 1000L + millis;
      };
    } else if (PeriodType.minutes().equals(this.periodType)) {
      return values -> {
        DateTime dateTime = new DateTime(values[0], BaselineAggregate.this.timeZone);
        long minutes = new Period(origin, dateTime, BaselineAggregate.this.periodType)
            .getMinutes();
        long seconds = dateTime.getSecondOfMinute();
        long millis = dateTime.getMillisOfSecond();
        return minutes * 100000L + seconds * 1000L + millis;
      };
    } else if (PeriodType.hours().equals(this.periodType)) {
      return values -> {
        DateTime dateTime = new DateTime(values[0], BaselineAggregate.this.timeZone);
        long hours = new Period(origin, dateTime, BaselineAggregate.this.periodType).getHours();
        long minutes = dateTime.getMinuteOfHour();
        long seconds = dateTime.getSecondOfMinute();
        long millis = dateTime.getMillisOfSecond();
        return hours * 10000000L + minutes * 100000L + seconds * 1000L + millis;
      };
    } else if (PeriodType.days().equals(this.periodType)) {
      return values -> {
        DateTime dateTime = new DateTime(values[0], BaselineAggregate.this.timeZone);
        long days = new Period(origin, dateTime, BaselineAggregate.this.periodType).getDays();
        long hours = dateTime.getHourOfDay();
        long minutes = dateTime.getMinuteOfHour();
        long seconds = dateTime.getSecondOfMinute();
        long millis = dateTime.getMillisOfSecond();
        return days * 1000000000L + hours * 10000000L + minutes * 100000L + seconds * 1000L
            + millis;
      };
    } else if (PeriodType.months().equals(this.periodType)) {
      return values -> {
        DateTime dateTime = new DateTime(values[0], BaselineAggregate.this.timeZone);
        long months = new Period(origin, dateTime, BaselineAggregate.this.periodType).getMonths();
        long days = dateTime.getDayOfMonth() - 1; // workaround for dayOfMonth > 0 constraint
        if (days == dateTime.dayOfMonth().getMaximumValue() - 1) {
          days = 99;
        }

        long hours = dateTime.getHourOfDay();
        long minutes = dateTime.getMinuteOfHour();
        long seconds = dateTime.getSecondOfMinute();
        long millis = dateTime.getMillisOfSecond();
        return months * 100000000000L + days * 1000000000L + hours * 10000000L + minutes * 100000L
            + seconds * 1000L + millis;
      };
    } else {
      throw new IllegalArgumentException(
          String.format("Unsupported PeriodType '%s'", this.periodType));
    }
  }

  /**
   * Returns a conversion function from virtual, relative timestamps to UTC timestamps given
   * a period type and an origin
   *
   * @param origin origin to base absolute timestamps on
   * @return LongFunction for converting to UTC timestamps
   */
  private Series.LongFunction makeVirtualToTimestampFunction(final DateTime origin) {
    if (PeriodType.millis().equals(this.periodType)) {
      return values -> values[0] + origin.getMillis();
    } else if (PeriodType.seconds().equals(this.periodType)) {
      return values -> {
        int seconds = (int) (values[0] / 1000L);
        int millis = (int) (values[0] % 1000L);
        return origin
            .plusSeconds(seconds)
            .plusMillis(millis)
            .getMillis();
      };
    } else if (PeriodType.minutes().equals(this.periodType)) {
      return values -> {
        int minutes = (int) (values[0] / 100000L);
        int seconds = (int) ((values[0] / 1000L) % 100L);
        int millis = (int) (values[0] % 1000L);
        return origin
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusMillis(millis)
            .getMillis();
      };
    } else if (PeriodType.hours().equals(this.periodType)) {
      return values -> {
        int hours = (int) (values[0] / 10000000L);
        int minutes = (int) ((values[0] / 100000L) % 100L);
        int seconds = (int) ((values[0] / 1000L) % 100L);
        int millis = (int) (values[0] % 1000L);
        return origin
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusMillis(millis)
            .getMillis();
      };
    } else if (PeriodType.days().equals(this.periodType)) {
      return values -> {
        int days = (int) (values[0] / 1000000000L);
        int hours = (int) ((values[0] / 10000000L) % 100L);
        int minutes = (int) ((values[0] / 100000L) % 100L);
        int seconds = (int) ((values[0] / 1000L) % 100L);
        int millis = (int) (values[0] % 1000L);
        return origin
            .plusDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusMillis(millis)
            .getMillis();
      };
    } else if (PeriodType.months().equals(this.periodType)) {
      return values -> {
        int months = (int) (values[0] / 100000000000L);
        int days = (int) ((values[0] / 1000000000L) % 100L);
        int hours = (int) ((values[0] / 10000000L) % 100L);
        int minutes = (int) ((values[0] / 100000L) % 100L);
        int seconds = (int) ((values[0] / 1000L) % 100L);
        int millis = (int) (values[0] % 1000L);

        DateTime originPlusMonth = origin.plusMonths(months);

        // last day of source month
        if (days >= 99) {
          days = originPlusMonth.dayOfMonth().getMaximumValue() - 1;
        }

        // unsupported destination day (e.g. 31st of Feb)
        if (originPlusMonth.dayOfMonth().getMaximumValue()
            < originPlusMonth.getDayOfMonth() + days) {
          return LongSeries.NULL;
        }

        DateTime target = originPlusMonth
            .plusDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusMillis(millis);

        return target.getMillis();
      };
    } else {
      throw new IllegalArgumentException(
          String.format("Unsupported PeriodType '%s'", this.periodType));
    }
  }
}
