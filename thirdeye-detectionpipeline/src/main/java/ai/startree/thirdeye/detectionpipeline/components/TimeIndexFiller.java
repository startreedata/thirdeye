/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.detectionpipeline.components;

import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.BOOLEAN;
import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.DOUBLE;
import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.LONG;
import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.OBJECT;
import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.STRING;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MAX_TIME_MILLIS;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MIN_TIME_MILLIS;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detectionpipeline.spec.TimeIndexFillerSpec;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries.Builder;
import ai.startree.thirdeye.spi.dataframe.ObjectSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.detection.IndexFiller;
import ai.startree.thirdeye.spi.detection.NullReplacer;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import ai.startree.thirdeye.spi.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeIndexFiller implements IndexFiller<TimeIndexFillerSpec> {

  private static final Logger LOG = LoggerFactory.getLogger(TimeIndexFiller.class);

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
    final Map<String, String> properties = dataTable.getProperties();
    timeColumn = Objects.requireNonNull(spec.getTimestamp());

    final String granularitySpec = optional(spec.getMonitoringGranularity())
        .orElseGet(() -> optional(properties.get(GRANULARITY.toString()))
            .orElseThrow(() -> new IllegalArgumentException(
                "monitoringGranularity is missing from spec and DataTable properties")));
    granularity = isoPeriod(granularitySpec);

    nullReplacer = new NullReplacerRegistry().buildNullReplacer(
        spec.getFillNullMethod().toUpperCase(), spec.getFillNullParams());

    boolean allTimeLimitsAreInProperties = properties.containsKey(MIN_TIME_MILLIS.toString())
        && properties.containsKey(MAX_TIME_MILLIS.toString());
    boolean noTimeLimitIsCustom = spec.getLookback() == null && spec.getMinTimeInference() == null
        && spec.getMaxTimeInference() == null;
    if (allTimeLimitsAreInProperties && noTimeLimitIsCustom) {
      minTime = Long.parseLong(properties.get(MIN_TIME_MILLIS.toString()));
      maxTime = Long.parseLong(properties.get(MAX_TIME_MILLIS.toString()));
      LOG.info("Using min time and max time provided by the DataTable properties.");
    } else {
      inferTimeLimits(detectionInterval, dataTable.getDataFrame());
    }
  }

  private void inferTimeLimits(final Interval detectionInterval, final DataFrame rawDataFrame) {
    final Period lookback = isoPeriod(spec.getLookback(), DEFAULT_LOOKBACK);
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
    Objects.requireNonNull(detectionInterval);
    initWithRuntimeInfo(detectionInterval, dataTable);

    final DataFrame rawData = dataTable.getDataFrame();
    checkArgument(rawData.contains(timeColumn),
        "'" + timeColumn + "' column not found in DataFrame");
    final DataFrame correctIndex = generateCorrectIndex(detectionInterval.getChronology());
    final DataFrame filledData = joinOnTimeIndex(correctIndex, rawData);
    final DataFrame nullReplacedData = replaceNullData(detectionInterval.getStart(), filledData);

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

  private DataFrame generateCorrectIndex(Chronology chronology) {
    final DateTime firstIndexValue = TimeUtils.getSmallestDatetime(new DateTime(minTime, chronology), granularity);
    final DateTime lastIndexValue = TimeUtils.getBiggestDatetime(new DateTime(maxTime, chronology), granularity);
    LOG.info(
        "Generating time index for minTime: {}, maxTime: {}. Computed first value: {}, last value: {}",
        minTime, maxTime, firstIndexValue.getMillis(), lastIndexValue.getMillis());
    final Series correctIndexSeries = generateSeries(firstIndexValue, lastIndexValue, granularity);
    final DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries(timeColumn, correctIndexSeries);

    return dataFrame;
  }

  private DataFrame joinOnTimeIndex(final DataFrame correctIndex, final DataFrame rawData) {
    leftFill(correctIndex, rawData, timeColumn);

    // some series can be of type Object if the rawData had no value before the join
    // fix: transform these Series of Objects into series of Doubles - incorrect if String series was expected
    for (final String seriesName : correctIndex.getSeriesNames()) {
      final Series series = correctIndex.get(seriesName);
      if (series.type() == OBJECT) {
        correctIndex.addSeries(seriesName, series.getDoubles());
      }
    }

    return correctIndex;
  }

  /**
   * Low level filling implementation - for performance.
   * Pre-conditions:
   * - the timeColumn Series of both dataframes are sorted in ascending order - this is not checked
   *
   * Behaviour:
   * join raw data into the correctIndex dataframe. Modifies correctIndex directly (no copy).
   * Equivalent to correctIndex.joinLeft(rawData, timeColumn, timeColumn); but is O(n),
   * whereas join is O(n*n).
   */
  @VisibleForTesting
  protected static void leftFill(final DataFrame correctIndex, final DataFrame rawData,
      final String timeColumn) {
    final long[] correctTimeIndex = correctIndex.getLongs(timeColumn).values();
    final int correctSize = correctTimeIndex.length;
    final long[] rawTimeIndex = rawData.getLongs(timeColumn).values();
    if (rawTimeIndex[rawTimeIndex.length - 1] > correctTimeIndex[correctTimeIndex.length - 1]) {
      LOG.error(
          "The last value of the time index of the raw data is bigger than the last value of the expected time index. This should never happen. {} > {}",
          rawTimeIndex[rawTimeIndex.length - 1], correctTimeIndex[correctTimeIndex.length
              - 1]); // TODO CYRIL can be removed once this is fixed - or add metric
    }

    for (final Entry<String, Series> entry : rawData.getSeries().entrySet()) {
      final String seriesName = entry.getKey();
      if (seriesName.equals(timeColumn)) {
        // skip the rawData timeColumn
        continue;
      }

      final Series series = entry.getValue();
      if (series.type().equals(BOOLEAN)) {
        final byte[] filledValues = new byte[correctSize];
        Arrays.fill(filledValues, BooleanSeries.NULL);
        final byte[] rawValues = series.getBooleans().values();
        int rawIdx = 0;
        int correctIdx = 0;
        while (correctIdx < correctSize && rawIdx < rawTimeIndex.length) {
          final int timeComparison = Long.compare(correctTimeIndex[correctIdx], rawTimeIndex[rawIdx]);
          if (timeComparison == 0) {
            filledValues[correctIdx++] = rawValues[rawIdx++];
          } else if (timeComparison > 0) {
            rawIdx++;
          } else {
            correctIdx++;
          }
        }
        correctIndex.addSeries(seriesName, filledValues);
      } else if (series.type().equals(DOUBLE)) {
        final double[] filledValues = new double[correctSize];
        Arrays.fill(filledValues, DoubleSeries.NULL);
        final double[] rawValues = series.getDoubles().values();
        int rawIdx = 0;
        int correctIdx = 0;
        while (correctIdx < correctSize && rawIdx < rawTimeIndex.length) {
          final int timeComparison = Long.compare(correctTimeIndex[correctIdx],
              rawTimeIndex[rawIdx]);
          if (timeComparison == 0) {
            filledValues[correctIdx++] = rawValues[rawIdx++];
          } else if (timeComparison > 0) {
            rawIdx++;
          } else {
            correctIdx++;
          }
        }
        correctIndex.addSeries(seriesName, filledValues);
      } else if (series.type().equals(LONG)) {
        final long[] filledValues = new long[correctSize];
        Arrays.fill(filledValues, LongSeries.NULL);
        final long[] rawValues = series.getLongs().values();
        int rawIdx = 0;
        int correctIdx = 0;
        while (correctIdx < correctSize && rawIdx < rawTimeIndex.length) {
          final int timeComparison = Long.compare(correctTimeIndex[correctIdx],
              rawTimeIndex[rawIdx]);
          if (timeComparison == 0) {
            filledValues[correctIdx++] = rawValues[rawIdx++];
          } else if (timeComparison > 0) {
            rawIdx++;
          } else {
            correctIdx++;
          }
        }
        correctIndex.addSeries(seriesName, filledValues);
      } else if (series.type().equals(STRING)) {
        final String[] filledValues = new String[correctSize];
        Arrays.fill(filledValues, StringSeries.NULL);
        final String[] rawValues = series.getStrings().values();
        int rawIdx = 0;
        int correctIdx = 0;
        while (correctIdx < correctSize && rawIdx < rawTimeIndex.length) {
          final int timeComparison = Long.compare(correctTimeIndex[correctIdx],
              rawTimeIndex[rawIdx]);
          if (timeComparison == 0) {
            filledValues[correctIdx++] = rawValues[rawIdx++];
          } else if (timeComparison > 0) {
            rawIdx++;
          } else {
            correctIdx++;
          }
        }
        correctIndex.addSeries(seriesName, filledValues);
      } else if (series.type().equals(OBJECT)) {
        final Object[] filledValues = new Object[correctSize];
        Arrays.fill(filledValues, ObjectSeries.NULL);
        final Object[] rawValues = series.getObjects().values();
        int rawIdx = 0;
        int correctIdx = 0;
        while (correctIdx < correctSize && rawIdx < rawTimeIndex.length) {
          final int timeComparison = Long.compare(correctTimeIndex[correctIdx],
              rawTimeIndex[rawIdx]);
          if (timeComparison == 0) {
            filledValues[correctIdx++] = rawValues[rawIdx++];
          } else if (timeComparison > 0) {
            rawIdx++;
          } else {
            correctIdx++;
          }
        }
        correctIndex.addSeriesObjects(seriesName, filledValues);
      } else {
        throw new UnsupportedOperationException(
            String.format("Unsupported Series type: %s", series.type()));
      }
    }
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
      final Period timePeriod) {
    final Builder correctIndexBuilder = LongSeries.builder();
    DateTime indexValue = new DateTime(firstValue);
    while (!indexValue.isAfter(lastValueIncluded)) {
      correctIndexBuilder.addValues(indexValue.getMillis());
      indexValue = indexValue.plus(timePeriod);
    }
    final LongSeries correctIndex = correctIndexBuilder.build();
    if (correctIndex.get(correctIndex.size() - 1) != lastValueIncluded.getMillis()) {
      LOG.error(
          "Error when creating time index. Mismatch between the input lastValue: {} and the generated last value: {}. This should never happen.",
          lastValueIncluded, correctIndex.get(correctIndex.size()
              - 1)); // TODO CYRIL can be removed once debugging session is done or throw
    }
    return correctIndex;
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
          "fillNull Method not registered: %s. Available null replacers: %s",
          fillNullMethod,
          nullReplacerMap.keySet());

      return nullReplacerMap.get(fillNullMethod);
    }
  }
}
