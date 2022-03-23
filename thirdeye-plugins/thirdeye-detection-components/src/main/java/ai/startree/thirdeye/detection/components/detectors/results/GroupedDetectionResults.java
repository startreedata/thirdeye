/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
package ai.startree.thirdeye.detection.components.detectors.results;

import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import java.util.Collections;
import java.util.List;

/**
 * The detection result. Contains a list of DetectionResult.
 */
public class GroupedDetectionResults implements DetectionPipelineResult {

  private final List<DetectionResult> detectionResults;

  public GroupedDetectionResults(List<DetectionResult> detectionResults) {
    this.detectionResults = detectionResults;
  }

  /**
   * Create a empty detection result
   *
   * @return the empty detection result
   */
  public static GroupedDetectionResults empty() {
    return new GroupedDetectionResults(Collections.emptyList());
  }

  /**
   * Create a detection result from a list of DetectionResult
   *
   * @param detectionResults the list of DetectionResult generated
   * @return the detection result contains the list of DetectionResult
   */
  public static GroupedDetectionResults from(List<DetectionResult> detectionResults) {
    return new GroupedDetectionResults(detectionResults);
  }

  public List<DetectionResult> getDetectionResults() {
    return detectionResults;
  }

  @Override
  public String toString() {
    return "DetectionResult{" + "detectionResults=" + detectionResults + '}';
  }
}
