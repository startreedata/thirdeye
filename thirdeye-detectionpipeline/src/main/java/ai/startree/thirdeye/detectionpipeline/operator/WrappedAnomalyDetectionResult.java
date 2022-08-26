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

package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.model.AnomalyDetectionResult;

// todo cyril suvodeep - this class does not look necessary AnomalyDetectionResult with enumerationItem setter seems enough and would avoid instanceOf checks
public class WrappedAnomalyDetectionResult extends AnomalyDetectionResult {

  private final EnumerationItemDTO enumerationItem;

  public WrappedAnomalyDetectionResult(final EnumerationItemDTO enumerationItem,
      final AnomalyDetectionResult delegate) {
    super(delegate);
    this.enumerationItem = enumerationItem;
  }

  @Override
  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }
}
