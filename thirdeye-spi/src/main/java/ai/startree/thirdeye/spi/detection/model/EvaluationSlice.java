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

import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;

/**
 * Selector for evaluations based on (optionally) start time and end time.
 */
public class EvaluationSlice {

  private final long start;
  private final long end;

  private EvaluationSlice(long start, long end) {
    this.start = start;
    this.end = end;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public boolean match(EvaluationDTO evaluationDTO) {
    if (this.start >= 0 && evaluationDTO.getEndTime() <= this.start) {
      return false;
    }
    return this.end < 0 || evaluationDTO.getStartTime() < this.end;
  }
}
