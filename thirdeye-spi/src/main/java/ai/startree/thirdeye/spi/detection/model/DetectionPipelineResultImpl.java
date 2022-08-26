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
package ai.startree.thirdeye.spi.detection.model;

import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DetectionPipelineResultImpl implements DetectionPipelineResult {

  private final List<DetectionResult> detectionResults;

  private DetectionPipelineResultImpl(final DetectionResult... detectionResults) {
    this.detectionResults = Arrays.asList(detectionResults);
  }

  public static DetectionPipelineResultImpl of(final DetectionResult detectionResult) {
    return new DetectionPipelineResultImpl(detectionResult);
  }

  public static DetectionPipelineResultImpl of(final DetectionResult... detectionResults) {
    return new DetectionPipelineResultImpl(detectionResults);
  }

  public static DetectionPipelineResultImpl of(final List<DetectionResult> detectionResults) {
    return new DetectionPipelineResultImpl(detectionResults.toArray(new DetectionResult[0]));
  }

  @Override
  public @NonNull List<DetectionResult> getDetectionResults() {
    return detectionResults;
  }
}
