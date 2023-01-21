/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AnomalyUtils {

  public static boolean isIgnore(final @NonNull AnomalyDTO parent) {
    return optional(parent.getAnomalyLabels())
        .map(labels -> labels.stream().anyMatch(AnomalyLabelDTO::isIgnore))
        .orElse(false);
  }

  public static void addLabel(final AnomalyDTO anomalyResultDTO,
      final AnomalyLabelDTO newLabel) {
    final List<AnomalyLabelDTO> labels = optional(anomalyResultDTO.getAnomalyLabels()).orElse(
        new ArrayList<>());
    labels.add(newLabel);
    anomalyResultDTO.setAnomalyLabels(labels);
  }
}
