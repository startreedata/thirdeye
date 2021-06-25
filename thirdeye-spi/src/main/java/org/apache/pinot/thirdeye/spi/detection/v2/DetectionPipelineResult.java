package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.List;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;

public interface DetectionPipelineResult {

  List<DetectionResult> getDetectionResults();
}
