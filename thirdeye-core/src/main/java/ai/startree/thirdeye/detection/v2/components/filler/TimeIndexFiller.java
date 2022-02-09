package ai.startree.thirdeye.detection.v2.components.filler;

import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.OBJECT;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MAX_TIME_MILLIS;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MIN_TIME_MILLIS;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.TIME_COLUMN;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_TIMESTAMP;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detection.v2.spec.TimeIndexFillerSpec;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries.Builder;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
import ai.startree.thirdeye.spi.detection.IndexFiller;
import ai.startree.thirdeye.spi.detection.NullReplacer;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

  private static final NullReplacer WITH_ZERO_NULL_REPLACER = new NullReplacerRegistry().buildNullReplacer(
      "FILL_WITH_ZEROES",
      new HashMap<>());

  private static final TimeLimitInferenceStrategy DEFAULT_MIN_TIME_INFERENCE_STRATEGY = TimeLimitInferenceStrategy.FROM_DATA;
  private static final TimeLimitInferenceStrategy DEFAULT_MAX_TIME_INFERENCE_STRATEGY = TimeLimitInferenceStrategy.FROM_DETECTION_TIME;
  private static final Period DEFAULT_LOOKBACK = Period.ZERO;

  private Period granularity;
  private String timeColumn;

  /**
   * Replacer of null values in metric/dimensions columns.
   */
  private NullReplacer nullReplacer;

  private TimeIndexFillerSpec spec;

  private long minTime;
  private long maxTime;

  @Override
  public void init(final TimeIndexFillerSpec spec) {
    // save the spec - resolve at runtime, once DataTable properties are available
    this.spec = spec;
  }

  private void initWithRuntimeInfo(final Interval detectionInterval, final DataTable dataTable) {
    // principle: custom config takes precedence over dataTable properties. If both config and properties are not set: use default config.
    Map<String, String> properties = dataTable.getProperties();
    if (spec.getTimestamp() != null && !spec.getTimestamp().equals(DEFAULT_TIMESTAMP)) {
      timeColumn = spec.getTimestamp();
    } else {
      timeColumn = properties.getOrDefault(TIME_COLUMN.toString(), DEFAULT_TIMESTAMP);
    }


    String granularitySpec = Optional.ofNullable(spec.getMonitoringGranularity())
        .orElseGet(() -> Optional.ofNullable(properties.get(GRANULARITY.toString()))
            .orElseThrow(() -> new IllegalArgumentException(
                "monitoringGranularity is missing from spec and DataTable properties")));
    granularity = Period.parse(granularitySpec, ISOPeriodFormat.standard());

    nullReplacer = new NullReplacerRegistry().buildNullReplacer(
        spec.getFillNullMethod().toUpperCase(), spec.getFillNullParams());

    boolean allTimeLimitsAreInProperties = properties.containsKey(MIN_TIME_MILLIS.toString())
        && properties.containsKey(MAX_TIME_MILLIS.toString());
    boolean noTimeLimitIsCustom = spec.getLookback() == null && spec.getMinTimeInference() == null
        && spec.getMaxTimeInference() == null;
    if (allTimeLimitsAreInProperties && noTimeLimitIsCustom) {
      minTime = Long.parseLong(properties.get(MIN_TIME_MILLIS.toString()));
      maxTime = Long.parseLong(properties.get(MAX_TIME_MILLIS.toString()));
    } else {
      inferTimeLimits(detectionInterval, dataTable.getDataFrame());
    }
  }

  private void inferTimeLimits(final Interval detectionInterval, final DataFrame rawDataFrame) {
    Period lookback = spec.getLookback() != null ?
        Period.parse(spec.getLookback(), ISOPeriodFormat.standard()) :
        DEFAULT_LOOKBACK;
    TimeLimitInferenceStrategy minTimeInference = spec.getMinTimeInference() != null ?
        TimeLimitInferenceStrategy.valueOf(spec.getMinTimeInference().toUpperCase()) :
        DEFAULT_MIN_TIME_INFERENCE_STRATEGY;
    checkArgument(isValidInferenceConfig(minTimeInference, lookback),
        "minTime inference based on lookback requires a valid `lookback` parameter");
    TimeLimitInferenceStrategy maxTimeInference = spec.getMaxTimeInference() != null ?
        TimeLimitInferenceStrategy.valueOf(spec.getMaxTimeInference().toUpperCase()) :
        DEFAULT_MAX_TIME_INFERENCE_STRATEGY;
    checkArgument(isValidInferenceConfig(maxTimeInference, lookback),
        "maxTime inference based on lookback requires a valid `lookback` parameter");

    minTime = inferMinTime(detectionInterval.getStart(),
        rawDataFrame.get(timeColumn),
        minTimeInference,
        lookback);
    maxTime = inferMaxTime(detectionInterval.getEnd(),
        rawDataFrame.get(timeColumn),
        maxTimeInference,
        lookback);
  }

  private static boolean isValidInferenceConfig(TimeLimitInferenceStrategy strategy,
      Period lookback) {
    return strategy != TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK
        || !lookback.equals(Period.ZERO);
  }

  @Override
  public DataTable fillIndex(Interval detectionInterval, final DataTable dataTable)
      throws Exception {
    initWithRuntimeInfo(detectionInterval, dataTable);

    DataFrame rawData = dataTable.getDataFrame();
    checkArgument(rawData.contains(timeColumn),
        "'" + timeColumn + "' column not found in DataFrame");
    DataFrame correctIndex = generateCorrectIndex();
    DataFrame filledData = joinOnTimeIndex(correctIndex, rawData);
    DataFrame nullReplacedData = replaceNullData(detectionInterval.getStart(), filledData);

    return SimpleDataTable.fromDataFrame(nullReplacedData);
  }

  private DataFrame replaceNullData(final DateTime start, final DataFrame dataFrame) {
    // only apply replacer *before* the detection period - on detection period, replace nulls by zeroes
    DataFrame beforeDetectionStart = dataFrame.filter((LongConditional) values ->
        values[0] < start.getMillis(), timeColumn).dropNull(timeColumn);
    DataFrame afterDetectionStart = dataFrame.filter((LongConditional) values ->
        values[0] >= start.getMillis(), timeColumn).dropNull(timeColumn);

    return DataFrame.concatenate(
        nullReplacer.replaceNulls(beforeDetectionStart),
        WITH_ZERO_NULL_REPLACER.replaceNulls(afterDetectionStart)
    );
  }

  private DataFrame generateCorrectIndex() {

    DateTime firstIndexValue = getFirstIndexValue(minTime, granularity);
    DateTime lastIndexValue = getLastIndexValue(maxTime, granularity);
    Series correctIndexSeries = generateSeries(firstIndexValue, lastIndexValue, granularity);
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

  private long inferMinTime(final DateTime start, final Series timeColumnSeries,
      final TimeLimitInferenceStrategy minTimeInference, final Period lookback) {
    if (minTimeInference == TimeLimitInferenceStrategy.FROM_DATA) {
      return timeColumnSeries.getLong(0);
    }
    return inferWithDetectionTime(minTimeInference, start, lookback);
  }

  private long inferMaxTime(final DateTime end, final Series timeColumnSeries,
      final TimeLimitInferenceStrategy maxTimeInference, final Period lookback) {
    if (maxTimeInference == TimeLimitInferenceStrategy.FROM_DATA) {
      // +1 to have an exclusive limit, like with other strategies
      return timeColumnSeries.getLong(timeColumnSeries.size() - 1) + 1;
    }
    return inferWithDetectionTime(maxTimeInference, end, lookback);
  }

  private long inferWithDetectionTime(final TimeLimitInferenceStrategy inferenceStrategy,
      final DateTime time, final Period lookback) {
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

  private static class NullReplacerRegistry {

    // extract this class and build map of NullReplacer *factories* dynamically when NullReplacers are pluginized
    public static Map<String, NullReplacer> nullReplacerMap = new HashMap<>();

    static {
      nullReplacerMap.put("KEEP_NULL", df -> df);
      nullReplacerMap.put("FILL_WITH_ZEROES", df -> df.fillNull(df.getSeriesNames()));
    }

    public NullReplacer buildNullReplacer(String fillNullMethod,
        Map<String, Object> fillNullParams) {
      //fillNullParams to be used by factories when pluginized - eg method=SPLINE, params={order=3, kind="smooth"}
      checkArgument(nullReplacerMap.containsKey(fillNullMethod),
          String.format("fillNull Method not registered: %s. Available null replacers: %s",
              fillNullMethod,
              nullReplacerMap.keySet()));

      return nullReplacerMap.get(fillNullMethod);
    }
  }
}
