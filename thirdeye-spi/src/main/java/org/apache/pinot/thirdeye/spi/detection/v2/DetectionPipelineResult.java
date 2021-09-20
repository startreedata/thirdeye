package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.List;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;

public interface DetectionPipelineResult {

  List<DetectionResult> getDetectionResults();

  default long getLastTimestamp() {
    return -1;
  }

  default List<MergedAnomalyResultDTO> getAnomalies() {
    throw new UnsupportedOperationException();
  }

  default List<EvaluationDTO> getEvaluations() {
    throw new UnsupportedOperationException();
  }
}
