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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.ArrayList;
import java.util.List;

public class LabelUtils {

  public static void addLabel(final MergedAnomalyResultDTO anomalyResultDTO,
      final AnomalyLabelDTO newLabel) {
    final List<AnomalyLabelDTO> labels = optional(anomalyResultDTO.getAnomalyLabels()).orElse(
        new ArrayList<>());
    labels.add(newLabel);
    anomalyResultDTO.setAnomalyLabels(labels);
  }
}
