package org.apache.pinot.thirdeye.task;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;

public class DetectionPipelineResultWrapper implements DetectionPipelineResult {

  private final AlertDTO alert;
  private final DetectionPipelineResult delegate;

  public DetectionPipelineResultWrapper(
      final AlertDTO alert,
      final DetectionPipelineResult delegate) {
    this.alert = alert;
    this.delegate = delegate;
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    return delegate.getDetectionResults();
  }

  /**
   * Choose the max out of all the timestamps
   *
   * @return the last processed timestamp
   *    NOTE! This is different from the legacy implementation which guesses the lastTimestamp.
   */
  @Override
  public long getLastTimestamp() {
    return delegate
        .getDetectionResults()
        .stream()
        .map(this::getLastTimestamp)
        .max(Long::compareTo)
        .orElse(-1L);
  }

  private Long getLastTimestamp(final DetectionResult detectionResult) {
    final LongSeries timeSeries = detectionResult.getTimeseries().getTime();
    return timeSeries.get(timeSeries.size() - 1);
  }

  /**
   * Collect anomalies from the results.
   *
   * @return list of anomalies found.
   */
  @Override
  public List<MergedAnomalyResultDTO> getAnomalies() {
    return delegate
        .getDetectionResults()
        .stream()
        .map(DetectionResult::getAnomalies)
        .flatMap(Collection::stream)
        .peek(anomaly -> anomaly.setDetectionConfigId(alert.getId()))
        .collect(Collectors.toList());
  }

  /**
   * TODO spyne to be implemented later.
   * @return empty list.
   */
  @Override
  public List<EvaluationDTO> getEvaluations() {
    return Collections.emptyList();
  }
}
