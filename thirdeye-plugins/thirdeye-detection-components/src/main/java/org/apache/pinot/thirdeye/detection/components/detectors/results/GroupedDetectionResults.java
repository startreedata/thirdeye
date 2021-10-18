/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */
package org.apache.pinot.thirdeye.detection.components.detectors.results;

import java.util.Collections;
import java.util.List;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;

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
