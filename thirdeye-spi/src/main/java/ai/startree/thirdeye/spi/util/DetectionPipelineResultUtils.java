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
package ai.startree.thirdeye.spi.util;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Temporary utils - refactoring DetectionPipelineResult.
 *
 * todo cyril move back to Runners or delete
 * */
public class DetectionPipelineResultUtils {
  /**
   * Finds the max out of all observed timestamps,
   * in all detection results.
   *
   * @return the last timestamp observed in the data.
   */
  public static long lastTimestamp(final @NonNull DetectionPipelineResult detectionPipelineResult) {
    return detectionPipelineResult.getDetectionResults()
        .stream()
        .filter(Objects::nonNull)
        .map(DetectionResult::getLastTimestamp)
        .max(Long::compareTo)
        .orElse(-1L);
  }

  public static long numAnomalies(final @NonNull DetectionPipelineResult detectionPipelineResult) {
    return optional(detectionPipelineResult.getDetectionResults().stream().map(DetectionResult::getAnomalies)
        .map(List::size).mapToInt(e -> e).sum()).orElse(0);
  }




}
