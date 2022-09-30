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
package ai.startree.thirdeye.detectionpipeline.operator;

import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperatorResult.Builder;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class AnomalyDetectorOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_OUTPUT_KEY = "output_AnomalyDetectorResult";

  private AnomalyDetector<? extends AbstractSpec> detector;
  private Period monitoringGranularity;

  public AnomalyDetectorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final DetectionRegistry detectionRegistry = (DetectionRegistry) context.getProperties()
        .get(Constants.DETECTION_REGISTRY_REF_KEY);
    requireNonNull(detectionRegistry, "DetectionRegistry is not set");
    detector = createDetector(optional(planNode.getParams()).map(TemplatableMap::valueMap)
        .orElse(null), detectionRegistry);
  }

  private AnomalyDetector<? extends AbstractSpec> createDetector(
      final Map<String, Object> params,
      final DetectionRegistry detectionRegistry) {
    final String type = ensureExists(MapUtils.getString(params, PROP_TYPE),
        ERR_MISSING_CONFIGURATION_FIELD,
        "'type' in detector config");

    final Map<String, Object> componentSpec = getComponentSpec(params);
    // get generic detector info
    AbstractSpec genericDetectorSpec = AbstractSpec.fromProperties(componentSpec,
        GenericDetectorSpec.class);
    ensureExists(genericDetectorSpec.getMonitoringGranularity(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "'monitoringGranularity' in detector config");
    monitoringGranularity = isoPeriod(genericDetectorSpec.getMonitoringGranularity());

    return detectionRegistry
        .buildDetector(type, new AnomalyDetectorFactoryContext().setProperties(componentSpec));
  }

  @Override
  public void execute() throws Exception {
    final Map<String, DataTable> dataTableMap = DetectionUtils.getDataTableMap(inputMap);
    final AnomalyDetectorResult detectorResult = detector.runDetection(detectionInterval,
        dataTableMap);

    OperatorResult operatorResult = buildDetectionResult(detectorResult);

    addMetadata(operatorResult);

    setOutput(DEFAULT_OUTPUT_KEY, operatorResult);
  }

  private void addMetadata(final OperatorResult operatorResult) {
    final Optional<String> anomalyMetric = optional(planNode.getParams().get("anomaly.metric"))
        .map(Templatable::value)
        .map(Object::toString);
    final Optional<String> anomalyDataset = optional(planNode.getParams().get("anomaly.dataset"))
        .map(Templatable::value)
        .map(Object::toString);
    final Optional<String> anomalySource = optional(planNode.getParams().get("anomaly.source"))
        .map(Templatable::value)
        .map(Object::toString);

    // annotate each anomaly with the available metadata
    for (MergedAnomalyResultDTO anomaly : operatorResult.getAnomalies()) {
      anomalyMetric.ifPresent(anomaly::setMetric);
      anomalyDataset.ifPresent(anomaly::setCollection);
      anomalySource.ifPresent(anomaly::setSource);
    }
  }

  @Override
  public String getOperatorName() {
    return "AnomalyDetectorOperator";
  }

  private OperatorResult buildDetectionResult(
      final AnomalyDetectorResult detectorV2Result) {

    final List<MergedAnomalyResultDTO> anomalies = buildAnomaliesFromDetectorDf(
        detectorV2Result.getDataFrame());
    final TimeSeries timeSeries = TimeSeries.fromDataFrame(detectorV2Result.getDataFrame()
        .sortedBy(COL_TIME));
    return new Builder()
        .setAnomalies(anomalies)
        .setTimeseries(timeSeries)
        .build();
  }

  private List<MergedAnomalyResultDTO> buildAnomaliesFromDetectorDf(final DataFrame df) {
    if (df.isEmpty()) {
      return Collections.emptyList();
    }

    final List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    final LongSeries timeMillisSeries = df.getLongs(Constants.COL_TIME);
    final BooleanSeries isAnomalySeries = df.getBooleans(Constants.COL_ANOMALY);
    final DoubleSeries currentSeries = df.getDoubles(Constants.COL_CURRENT);
    final DoubleSeries baselineSeries = df.getDoubles(Constants.COL_VALUE);

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
      // last anomaly has not been closed - let's close it - compute end time of anomaly range
      final long lastTimestamp = timeMillisSeries.getLong(timeMillisSeries.size() - 1);
      // exact computation of end of period
      DateTime endTime = new DateTime(lastTimestamp, detectionInterval.getChronology())
          .plus(monitoringGranularity);
      anomalies.add(anomalyStatsAccumulator.buildAnomaly(lastStartMillis, endTime.getMillis()));
    }

    return anomalies;
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class GenericDetectorSpec extends AbstractSpec {

    public GenericDetectorSpec() {
    }
  }
}
