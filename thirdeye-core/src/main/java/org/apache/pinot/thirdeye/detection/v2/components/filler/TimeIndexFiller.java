package org.apache.pinot.thirdeye.detection.v2.components.filler;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.SeriesType.OBJECT;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import org.apache.pinot.thirdeye.detection.v2.spec.TimeIndexFillerSpec;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries.Builder;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.detection.IndexFiller;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

public class TimeIndexFiller implements IndexFiller<TimeIndexFillerSpec> {

  /**
   * Available strategies to compute minTime and maxTime constraints of the timeseries.
   */
  public enum TimeLimitInferenceStrategy {
    FROM_DATA,
    FROM_DETECTION_TIME,
    FROM_DETECTION_TIME_WITH_LOOKBACK,
  }

  /**
   * Available methods to fill null values generated in other columns when the index is completed.
   */
  public enum FillNullMethod {
    KEEP_NULL,
    FILL_WITH_ZEROES
  }

  private TimeGranularity timeGranularity;
  private String timeColumn;
  /**
   * Inference strategy for the min time constraint.
   */
  private TimeLimitInferenceStrategy minTimeInference;
  /**
   * Inference strategy for the max time constraint.
   */
  private TimeLimitInferenceStrategy maxTimeInference;
  /**
   * Period in milliseconds. Used when time limit inference uses a lookback time.
   */
  private Period lookback;
  /**
   * Method to fill null values.
   */
  private FillNullMethod fillNullMethod;

  @Override
  public void init(final TimeIndexFillerSpec spec) {
    timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    // TODO CYRIL if granularity not set, infer it - it is min(ts[n]-ts[n-1])
    checkArgument(!MetricSlice.NATIVE_GRANULARITY.equals(timeGranularity),
        "NATIVE_GRANULARITY not supported in v2 interface - set `monitoringGranularity` parameter");

    lookback = spec.getLookback().equals("") ? Period.ZERO
        : Period.parse(spec.getLookback(), ISOPeriodFormat.standard());
    checkArgument(isSingleUnitOrZero(lookback), "lookback period cannot use multiple units");
    minTimeInference = TimeLimitInferenceStrategy.valueOf(spec.getMinTimeInference().toUpperCase());
    checkArgument(isValidInferenceConfig(maxTimeInference, lookback),
        "minTime inference based on lookback requires a valid `lookback` parameter");
    maxTimeInference = TimeLimitInferenceStrategy.valueOf(spec.getMaxTimeInference().toUpperCase());
    checkArgument(isValidInferenceConfig(maxTimeInference, lookback),
        "maxTime inference based on lookback requires a valid `lookback` parameter");

    timeColumn = spec.getTimestamp();
    fillNullMethod = FillNullMethod.valueOf(spec.getFillNullMethod().toUpperCase());
  }

  private static boolean isValidInferenceConfig(TimeLimitInferenceStrategy strategy,
      Period lookback) {
    return strategy != TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK
        || !lookback.equals(Period.ZERO);
  }

  @Override
  public DataTable fillIndex(Interval interval, final DataTable dataTable) throws Exception {
    DataFrame rawData = dataTable.getDataFrame();
    checkArgument(rawData.contains(timeColumn),
        "'" + timeColumn + "' column not found in DataFrame");
    DataFrame correctIndex = generateCorrectIndex(interval, rawData);
    DataFrame filledData = joinOnTimeIndex(correctIndex, rawData);

    if (fillNullMethod == FillNullMethod.FILL_WITH_ZEROES) {
      filledData = filledData.fillNull(filledData.getSeriesNames());
    }

    return SimpleDataTable.fromDataFrame(filledData);
  }

  private DataFrame generateCorrectIndex(final Interval interval,
      final DataFrame rawDataFrame) {
    long inferredMinTime = inferMinTime(interval.getStart(), rawDataFrame);
    long inferredMaxTime = inferMaxTime(interval.getEnd(), rawDataFrame);

    Period timePeriod = timeGranularity.toPeriod();
    DateTime firstIndexValue = getFirstIndexValue(inferredMinTime, timePeriod);
    DateTime lastIndexValue = getLastIndexValue(inferredMaxTime, timePeriod);
    Series correctIndexSeries = generateSeries(firstIndexValue, lastIndexValue, timePeriod);
    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries(timeColumn, correctIndexSeries);

    return dataFrame;
  }

  private DataFrame joinOnTimeIndex(DataFrame correctIndex, DataFrame rawData) {
    DataFrame filledData = correctIndex.joinLeft(rawData, timeColumn, timeColumn);

    // some series can be of type Object if the rawData had no value before the join
    // fix: transform these Series of Objects into series of Doubles - incorrect if String series was expected
    for (String seriesName : filledData.getSeriesNames()) {
      Series series = filledData.get(seriesName);
      if (series.type() == OBJECT) {
        filledData.addSeries(seriesName, series.getDoubles());
      }
    }

    return filledData;
  }

  private long inferMinTime(final DateTime start, final DataFrame rawDataFrame) {
    if (minTimeInference == TimeLimitInferenceStrategy.FROM_DATA) {
      return rawDataFrame.get(timeColumn).getLong(0);
    }
    return inferWithDetectionTime(minTimeInference, start);
  }

  private long inferMaxTime(final DateTime end, final DataFrame rawDataFrame) {
    if (maxTimeInference == TimeLimitInferenceStrategy.FROM_DATA) {
      // +1 to have an exclusive limit, like with other strategies
      return rawDataFrame.get(timeColumn).getLong(rawDataFrame.size() - 1) + 1;
    }
    return inferWithDetectionTime(maxTimeInference, end);
  }

  private long inferWithDetectionTime(final TimeLimitInferenceStrategy inferenceStrategy,
      final DateTime time) {
    switch (inferenceStrategy) {
      case FROM_DETECTION_TIME:
        return time.getMillis();
      case FROM_DETECTION_TIME_WITH_LOOKBACK:
        return time.minus(lookback).getMillis();
      default:
        throw new UnsupportedOperationException(
            "Unsupported time inference strategy " + inferenceStrategy);
    }
  }

  private Series generateSeries(final DateTime firstValue, final DateTime lastValueIncluded,
      Period timePeriod) {
    Builder correctIndexSeries = LongSeries.builder();
    DateTime indexValue = new DateTime(firstValue);
    while (!indexValue.isAfter(lastValueIncluded)) {
      correctIndexSeries.addValues(indexValue.getMillis());
      indexValue = indexValue.plus(timePeriod);
    }
    return correctIndexSeries.build();
  }

  /**
   * Returns the first index of a timeseries grouped by period, with time index >=minTimeConstraint.
   * eg: minTimeConstraint is >= Thursday 3 am - period is 1 DAY --> returns Friday.
   * eg: inclusion: minTimeConstraint is >= Thursday 0 am - period is 1 DAY --> returns Thursday.
   */
  @VisibleForTesting
  protected static DateTime getFirstIndexValue(long minTimeConstraint, Period timePeriod) {
    DateTime dateTimeConstraint = new DateTime(minTimeConstraint, DateTimeZone.UTC);
    DateTime dateTimeFloored = floorByPeriod(dateTimeConstraint, timePeriod.toPeriod());
    if (dateTimeConstraint.equals(dateTimeFloored)) {
      return dateTimeFloored;
    }
    // dateTimeConstraint bigger than floor --> add 1 period
    return dateTimeFloored.plus(timePeriod);
  }

  /**
   * Returns the last index of a timeseries grouped by period, with time index <maxTimeConstraint.
   * eg: maxTimeConstraint is < Monday 4 am - period is 1 DAY --> returns Monday.
   * eg: exclusion: maxTimeConstraint is < Monday 0 am - period is 1 DAY --> returns Sunday.
   */
  @VisibleForTesting
  protected static DateTime getLastIndexValue(long maxTimeConstraint, Period timePeriod) {
    DateTime dateTimeConstraint = new DateTime(maxTimeConstraint, DateTimeZone.UTC);
    DateTime dateTimeFloored = floorByPeriod(dateTimeConstraint, timePeriod.toPeriod());

    if (dateTimeConstraint.equals(dateTimeFloored)) {
      // dateTimeConstraint equals floor --> should be excluded
      return dateTimeFloored.minus(timePeriod);
    }
    return dateTimeFloored;
  }

  /**
   * See https://stackoverflow.com/questions/8933158/how-do-i-round-a-datetime-to-the-nearest-period
   * Floors correctly only if 1 Time unit is used in the Period.
   */
  @VisibleForTesting
  protected static DateTime floorByPeriod(DateTime dt, Period period) {
    if (period.getYears() != 0) {
      return dt.yearOfEra().roundFloorCopy().minusYears(dt.getYearOfEra() % period.getYears());
    } else if (period.getMonths() != 0) {
      return dt.monthOfYear()
          .roundFloorCopy()
          .minusMonths((dt.getMonthOfYear() - 1) % period.getMonths());
    } else if (period.getWeeks() != 0) {
      return dt.weekOfWeekyear()
          .roundFloorCopy()
          .minusWeeks((dt.getWeekOfWeekyear() - 1) % period.getWeeks());
    } else if (period.getDays() != 0) {
      return dt.dayOfMonth()
          .roundFloorCopy()
          .minusDays((dt.getDayOfMonth() - 1) % period.getDays());
    } else if (period.getHours() != 0) {
      return dt.hourOfDay().roundFloorCopy().minusHours(dt.getHourOfDay() % period.getHours());
    } else if (period.getMinutes() != 0) {
      return dt.minuteOfHour()
          .roundFloorCopy()
          .minusMinutes(dt.getMinuteOfHour() % period.getMinutes());
    } else if (period.getSeconds() != 0) {
      return dt.secondOfMinute()
          .roundFloorCopy()
          .minusSeconds(dt.getSecondOfMinute() % period.getSeconds());
    }
    return dt.millisOfSecond()
        .roundCeilingCopy()
        .minusMillis(dt.getMillisOfSecond() % period.getMillis());
  }

  private static boolean isSingleUnitOrZero(final Period lookback) {
    // TODO CYRIL refactor to a IsoDateUtils class
    return lookback.equals(Period.ZERO)
        || Arrays.stream(lookback.getValues()).filter(v -> v == 0).count() == 7;
  }
}
