package org.apache.pinot.thirdeye.detection.v2.operator;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.joda.time.Interval;

public class AnomalyDetectorOperator extends DetectionPipelineOperator<DataTable> {

  private static final String DEFAULT_OUTPUT_KEY = "AnomalyDetectorResult";

  public AnomalyDetectorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  protected BaseComponent createComponent(final Map<String, Object> componentSpec) {
    final String type = requireNonNull(MapUtils.getString(componentSpec, PROP_TYPE),
        "Must have 'type' in detector config");
    return new DetectionRegistry()
        .buildDetectorV2(type, new AnomalyDetectorFactoryContext().setProperties(componentSpec));
  }

  @SuppressWarnings({"SuspiciousMethodCalls", "rawtypes"})
  @Override
  public void execute() throws Exception {
    // The last exception of the detection windows. It will be thrown out to upper level.
    for (final Object key : getComponents().keySet()) {
      final BaseComponent component = getComponents().get(key);
      if (component instanceof AnomalyDetectorV2) {
        runDetection(key, (AnomalyDetectorV2) component);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void runDetection(final Object key, final AnomalyDetectorV2 anomalyDetectorV2)
      throws DetectorException {
    anomalyDetectorV2.setTimeConverter(timeConverter);
    for (final Interval interval : getMonitoringWindows()) {
      final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
      final DetectionPipelineResult detectionResult = anomalyDetectorV2
          .runDetection(interval, timeSeriesMap);

      // Annotate each anomaly with a metric name
      optional(planNode.getParams().get("anomaly.metric"))
          .map(Object::toString)
          .ifPresent(anomalyMetric -> detectionResult.getDetectionResults().stream()
              .map(DetectionResult::getAnomalies)
              .flatMap(Collection::stream)
              .forEach(anomaly -> anomaly.setMetric(anomalyMetric)));

      final String outputKey = key + "_" + DEFAULT_OUTPUT_KEY;
      setOutput(outputKey, detectionResult);
    }
  }

  private List<Interval> getMonitoringWindows() {
    return Collections.singletonList(new Interval(startTime, endTime));
  }

  @Override
  public String getOperatorName() {
    return "AnomalyDetectorOperator";
  }
}
