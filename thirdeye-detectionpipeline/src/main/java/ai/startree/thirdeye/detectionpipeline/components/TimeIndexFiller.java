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

import static ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller.TimeLimitInferenceStrategy.FROM_DATA;
import static ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller.TimeLimitInferenceStrategy.FROM_DETECTION_TIME;
import static ai.startree.thirdeye.spi.dataframe.Series.SeriesType.OBJECT;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MAX_TIME_MILLIS;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MIN_TIME_MILLIS;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detectionpipeline.spec.TimeIndexFillerSpec;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries.Builder;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
import ai.startree.thirdeye.spi.detection.IndexFiller;
import ai.startree.thirdeye.spi.detection.NullReplacer;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import ai.startree.thirdeye.spi.util.TimeUtils;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

public class TimeIndexFiller implements IndexFiller<TimeIndexFillerSpec> {

  private static final TimeLimitInferenceStrategy DEFAULT_MIN_TIME_INFERENCE_STRATEGY = FROM_DATA;
  private static final TimeLimitInferenceStrategy DEFAULT_MAX_TIME_INFERENCE_STRATEGY = FROM_DETECTION_TIME;
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

  private static boolean isValidInferenceConfig(final TimeLimitInferenceStrategy strategy,
      final Period lookback) {
    return strategy != TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK
        || !lookback.equals(Period.ZERO);
  }

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

    final boolean allTimeLimitsAreInProperties = properties.containsKey(MIN_TIME_MILLIS.toString())
        && properties.containsKey(MAX_TIME_MILLIS.toString());
    final boolean noTimeLimitIsCustom =
        spec.getLookback() == null && spec.getMinTimeInference() == null
            && spec.getMaxTimeInference() == null;
    if (allTimeLimitsAreInProperties && noTimeLimitIsCustom) {
      minTime = Long.parseLong(properties.get(MIN_TIME_MILLIS.toString()));
      maxTime = Long.parseLong(properties.get(MAX_TIME_MILLIS.toString()));
    } else {
      inferTimeLimits(detectionInterval, dataTable.getDataFrame());
    }
  }

  private void inferTimeLimits(final Interval detectionInterval, final DataFrame rawDataFrame) {
    final Period lookback = isoPeriod(spec.getLookback(), DEFAULT_LOOKBACK);
    final TimeLimitInferenceStrategy minTimeInference = spec.getMinTimeInference() != null ?
        TimeLimitInferenceStrategy.valueOf(spec.getMinTimeInference().toUpperCase()) :
        DEFAULT_MIN_TIME_INFERENCE_STRATEGY;
    checkArgument(isValidInferenceConfig(minTimeInference, lookback),
        "minTime inference based on lookback requires a valid `lookback` parameter");
    final TimeLimitInferenceStrategy maxTimeInference = spec.getMaxTimeInference() != null ?
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

  @Override
  public DataTable fillIndex(final Interval detectionInterval, final DataTable dataTable) {
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
    final DataFrame beforeDetectionStart = dataFrame
        .filter((LongConditional) values -> values[0] < start.getMillis(), timeColumn)
        .dropNull(timeColumn);
    final DataFrame afterDetectionStart = dataFrame
        .filter((LongConditional) values -> values[0] >= start.getMillis(), timeColumn)
        .dropNull(timeColumn);

    return DataFrame.concatenate(
        nullReplacer.replaceNulls(beforeDetectionStart),
        nullReplacer.replaceNulls(afterDetectionStart)
    );
  }

  private DataFrame generateCorrectIndex(final Chronology chronology) {

    final DateTime firstIndexValue = TimeUtils.getSmallestDatetime(
        new DateTime(minTime, chronology), granularity);
    final DateTime lastIndexValue = TimeUtils.getBiggestDatetime(
        new DateTime(maxTime, chronology), granularity);
    final Series correctIndexSeries = generateSeries(firstIndexValue, lastIndexValue, granularity);
    final DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries(timeColumn, correctIndexSeries);

    return dataFrame;
  }

  private DataFrame joinOnTimeIndex(final DataFrame correctIndex, final DataFrame rawData) {
    final DataFrame filledData = correctIndex.joinLeft(rawData, timeColumn, timeColumn);

    // some series can be of type Object if the rawData had no value before the join
    // fix: transform these Series of Objects into series of Doubles - incorrect if String series was expected
    for (final String seriesName : filledData.getSeriesNames()) {
      final Series series = filledData.get(seriesName);
      if (series.type() == OBJECT) {
        filledData.addSeries(seriesName, series.getDoubles());
      }
    }

    return filledData;
  }

  private long inferMinTime(final DateTime start, final Series timeColumnSeries,
      final TimeLimitInferenceStrategy minTimeInference, final Period lookback) {
    if (minTimeInference == FROM_DATA) {
      return timeColumnSeries.getLong(0);
    }
    return inferWithDetectionTime(minTimeInference, start, lookback);
  }

  private long inferMaxTime(final DateTime end, final Series timeColumnSeries,
      final TimeLimitInferenceStrategy maxTimeInference, final Period lookback) {
    if (maxTimeInference == FROM_DATA) {
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
    final Builder correctIndexSeries = LongSeries.builder();
    DateTime indexValue = new DateTime(firstValue);
    while (!indexValue.isAfter(lastValueIncluded)) {
      correctIndexSeries.addValues(indexValue.getMillis());
      indexValue = indexValue.plus(timePeriod);
    }
    return correctIndexSeries.build();
  }

  /**
   * Available strategies to compute minTime and maxTime constraints of the timeseries.
   */
  public enum TimeLimitInferenceStrategy {
    FROM_DATA,
    FROM_DETECTION_TIME,
    FROM_DETECTION_TIME_WITH_LOOKBACK,
  }

  private static class NullReplacerRegistry {

    // extract this class and build map of NullReplacer *factories* dynamically when NullReplacers are pluginized
    public static final ImmutableMap<String, NullReplacer> NULL_REPLACER_MAP = create();

    private static ImmutableMap<String, NullReplacer> create() {
      return ImmutableMap.<String, NullReplacer>builder()
          .put("KEEP_NULL", df -> df)
          .put("FILL_WITH_ZEROES", df -> df.fillNull(df.getSeriesNames()))
          .put("FILL_FORWARD", df -> df.fillNullForward(df.getSeriesNames()))
          .put("FILL_BACKWARD", df -> df.fillNullBackward(df.getSeriesNames()))
          .build();
    }

    public NullReplacer buildNullReplacer(final String fillNullMethod,
        final Map<String, Object> fillNullParams) {
      //fillNullParams to be used by factories when pluginized - eg method=SPLINE, params={order=3, kind="smooth"}
      checkArgument(NULL_REPLACER_MAP.containsKey(fillNullMethod),
          "fillNull Method not registered: %s. Available null replacers: %s",
          fillNullMethod,
          NULL_REPLACER_MAP.keySet());

      return NULL_REPLACER_MAP.get(fillNullMethod);
    }
  }
}
