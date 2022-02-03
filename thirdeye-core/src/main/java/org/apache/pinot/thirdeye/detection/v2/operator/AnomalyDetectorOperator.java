package org.apache.pinot.thirdeye.detection.v2.operator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryV2Context;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public class AnomalyDetectorOperator extends DetectionPipelineOperator {

  private static final String DEFAULT_OUTPUT_KEY = "output_AnomalyDetectorResult";

  private AnomalyDetectorV2<? extends AbstractSpec> detector;
  private AbstractSpec genericDetectorSpec;

  public AnomalyDetectorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    detector = createDetector(planNode.getParams());
  }

  private AnomalyDetectorV2<? extends AbstractSpec> createDetector(
      final Map<String, Object> params) {
    final String type = requireNonNull(MapUtils.getString(params, PROP_TYPE),
        "Must have 'type' in detector config");

    final Map<String, Object> componentSpec = getComponentSpec(params);
    // get generic detector info
    genericDetectorSpec = AbstractSpec.fromProperties(componentSpec, GenericDetectorSpec.class);
    requireNonNull(genericDetectorSpec.getMonitoringGranularity(),
        "monitoringGranularity is mandatory in v2 interface");
    checkArgument(!MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString().equals(
            genericDetectorSpec.getMonitoringGranularity()),
        "NATIVE_GRANULARITY not supported in v2 interface");

    return new DetectionRegistry()
        .buildDetectorV2(type, new AnomalyDetectorFactoryV2Context().setProperties(componentSpec));
  }

  @Override
  public void execute() throws Exception {
    for (final Interval interval : getMonitoringWindows()) {
      final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
      final AnomalyDetectorV2Result detectorResult = detector
          .runDetection(interval, timeSeriesMap);

      DetectionPipelineResult detectionResult = buildDetectionResult(detectorResult);

      addMetadata(detectionResult);

      setOutput(DEFAULT_OUTPUT_KEY, detectionResult);
    }
  }

  private void addMetadata(DetectionPipelineResult detectionPipelineResult) {
    // Annotate each anomaly with a metric name
    optional(planNode.getParams().get("anomaly.metric"))
        .map(Object::toString)
        .ifPresent(anomalyMetric -> detectionPipelineResult.getDetectionResults().stream()
            .map(DetectionResult::getAnomalies)
            .flatMap(Collection::stream)
            .forEach(anomaly -> anomaly.setMetric(anomalyMetric)));

    // Annotate each anomaly with source info
    optional(planNode.getParams().get("anomaly.source"))
        .map(Object::toString)
        .ifPresent(anomalySource -> detectionPipelineResult.getDetectionResults().stream()
            .map(DetectionResult::getAnomalies)
            .flatMap(Collection::stream)
            .forEach(anomaly -> anomaly.setSource(anomalySource)));
  }

  private List<Interval> getMonitoringWindows() {
    return singletonList(new Interval(startTime, endTime));
  }

  @Override
  public String getOperatorName() {
    return "AnomalyDetectorOperator";
  }

  private DetectionResult buildDetectionResult(
      final AnomalyDetectorV2Result detectorV2Result) {

    final List<MergedAnomalyResultDTO> anomalies = buildAnomaliesFromDetectorDf(
        detectorV2Result.getDataFrame(),
        genericDetectorSpec.getTimezone(),
        getMonitoringGranularityPeriod(genericDetectorSpec.getMonitoringGranularity()));

    return DetectionResult.from(anomalies,
        TimeSeries.fromDataFrame(detectorV2Result.getDataFrame().sortedBy(COL_TIME)));
  }

  private static List<MergedAnomalyResultDTO> buildAnomaliesFromDetectorDf(final DataFrame df,
      final String datasetTimezone,
      final Period monitoringGranularityPeriod) {
    if (df.isEmpty()) {
      return Collections.emptyList();
    }

    final List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    final LongSeries timeMillisSeries = df.getLongs(DataFrame.COL_TIME);
    final BooleanSeries isAnomalySeries = df.getBooleans(DataFrame.COL_ANOMALY);
    final DoubleSeries currentSeries = df.getDoubles(DataFrame.COL_CURRENT);
    final DoubleSeries baselineSeries = df.getDoubles(DataFrame.COL_VALUE);

    long lastStartMillis = -1;
    AnomalyStatsAccumulator anomalyStatsAccumulator = new AnomalyStatsAccumulator();

    for (int i = 0; i < df.size(); i++) {
      if (!isAnomalySeries.isNull(i) && BooleanSeries.booleanValueOf(isAnomalySeries.get(i))) {
        // inside an anomaly range
        if (lastStartMillis < 0) {
          // start of an anomaly range
          lastStartMillis = timeMillisSeries.get(i);
        }
        if (!currentSeries.isNull(i)) {
          anomalyStatsAccumulator.addCurrentValue(currentSeries.getDouble(i));
        }
        if (!baselineSeries.isNull(i)) {
          anomalyStatsAccumulator.addBaselineValue(baselineSeries.getDouble(i));
        }
      } else if (lastStartMillis >= 0) {
        // anomaly range opened - let's close the anomaly
        long endMillis = timeMillisSeries.get(i);
        anomalies.add(anomalyStatsAccumulator.buildAnomaly(lastStartMillis, endMillis));

        // reset variables for next anomaly
        anomalyStatsAccumulator.reset();
        lastStartMillis = -1;
      }
    }

    if (lastStartMillis >= 0) {
      // last anomaly has not been closed - let's close it
      // estimate end time of anomaly range
      final long lastTimestamp = timeMillisSeries.getLong(timeMillisSeries.size() - 1);
      // default: add 1 to lastTimestamp
      long endMillis = lastTimestamp + 1;
      if (datasetTimezone != null && monitoringGranularityPeriod != null) {
        // exact computation of end of period
        final DateTimeZone timezone = DateTimeZone.forID(datasetTimezone);
        endMillis = new DateTime(lastTimestamp, timezone)
            .plus(monitoringGranularityPeriod)
            .getMillis();
      }
      anomalies.add(anomalyStatsAccumulator.buildAnomaly(lastStartMillis, endMillis));
    }

    return anomalies;
  }

  /**
   * Get the joda period for a monitoring granularity
   */
  private static Period getMonitoringGranularityPeriod(final String monitoringGranularity) {
    final String[] split = monitoringGranularity.split("_");
    if (split[1].equals("MONTHS")) {
      return new Period(0, Integer.parseInt(split[0]), 0, 0, 0, 0, 0, 0, PeriodType.months());
    }
    if (split[1].equals("WEEKS")) {
      return new Period(0, 0, Integer.parseInt(split[0]), 0, 0, 0, 0, 0, PeriodType.weeks());
    }
    return TimeGranularity.fromString(monitoringGranularity).toPeriod();
  }

  private static class AnomalyStatsAccumulator {

    private double currentSum = 0;
    private int currentCount = 0;
    private double baselineSum = 0;
    private int baselineCount = 0;

    public AnomalyStatsAccumulator() {
    }

    public MergedAnomalyResultDTO buildAnomaly(long startMillis, long endMillis) {
      final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
      anomaly.setStartTime(startMillis);
      anomaly.setEndTime(endMillis);
      if (currentCount > 0) {
        anomaly.setAvgCurrentVal(currentSum / currentCount);
      }
      if (baselineCount > 0) {
        anomaly.setAvgBaselineVal(baselineSum / baselineCount);
      }
      return anomaly;
    }

    public void addCurrentValue(double currentValue) {
      currentSum += currentValue;
      ++currentCount;
    }

    public void addBaselineValue(double baselineValue) {
      baselineSum += baselineValue;
      ++baselineCount;
    }

    public void reset() {
      currentSum = 0;
      currentCount = 0;
      baselineSum = 0;
      baselineCount = 0;
    }
  }

  /**
   * Used to parse parameters common to all detectors that use AbstractSpec
   * Makes the AnomalyDetectorOperator more aware of what's happening.
   * Temporary solution. Maybe introduce a DetectorSpec extends AbstractSpec in public
   */
  private static class GenericDetectorSpec extends AbstractSpec {

    public GenericDetectorSpec() {
    }
  }
}
