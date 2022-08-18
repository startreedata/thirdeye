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
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import java.util.Map;

public class ForkJoinResultItem {

  private final EnumerationItemDTO enumerationItem;
  private final Map<String, DetectionPipelineResult> results;

  public ForkJoinResultItem(final EnumerationItemDTO enumerationItem,
      final Map<String, DetectionPipelineResult> results) {
    this.enumerationItem = enumerationItem;
    this.results = results;
  }

  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  public Map<String, DetectionPipelineResult> getResults() {
    return results;
  }
}
