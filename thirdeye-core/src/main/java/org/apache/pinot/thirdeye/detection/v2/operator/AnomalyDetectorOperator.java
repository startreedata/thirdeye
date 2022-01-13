package org.apache.pinot.thirdeye.detection.v2.operator;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.detection.DetectionUtils.buildDetectionResult;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryV2Context;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.joda.time.Interval;

public class AnomalyDetectorOperator extends DetectionPipelineOperator {

  private static final String DEFAULT_OUTPUT_KEY = "output_AnomalyDetectorResult";

  private AnomalyDetectorV2<? extends AbstractSpec> detector;

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
}
