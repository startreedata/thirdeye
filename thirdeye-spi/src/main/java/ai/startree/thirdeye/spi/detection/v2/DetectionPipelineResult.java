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
package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import java.util.List;

public interface DetectionPipelineResult {

  default List<DetectionResult> getDetectionResults() {
    return null;
  }

  /**
   * If implemented, returns the last timestamp observed in the data. Can be different from the last
   * processed timestamp.
   */
  default long getLastTimestamp() {
    return -1;
  }

  default List<MergedAnomalyResultDTO> getAnomalies() {
    throw new UnsupportedOperationException();
  }

  default EnumerationItemDTO getEnumerationItem() {
    return null;
  }
}
