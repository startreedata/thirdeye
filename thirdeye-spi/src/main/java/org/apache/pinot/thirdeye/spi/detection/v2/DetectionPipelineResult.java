package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.List;

public interface DetectionPipelineResult {

  List<DetectionResult> getDetectionResults();
}
