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
package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RcaDimensionFilterHelper {

  /**
   * Apply inclusion and exclusion lists
   */
  @NonNull
  public static List<String> getRcaDimensions(final @NonNull List<String> includedDimensions,
      final @NonNull List<String> excludedDimensions,
      final DatasetConfigDTO datasetConfigDTO) {
    final boolean oneListIsEmpty = includedDimensions.isEmpty() || excludedDimensions.isEmpty();
    if (!oneListIsEmpty) {
      throw new IllegalArgumentException(
          "includedDimensions and excludedDimensions are both not empty. Cannot use an inclusion list and an exclusion list at the same time");
    } else if (!includedDimensions.isEmpty()) {
      // use inclusion list - takes precedence other every other lists
      return cleanDimensionStrings(includedDimensions);
    } else {
      final Templatable<List<String>> datasetDimensions = datasetConfigDTO.getDimensions();
      if (datasetDimensions == null || datasetDimensions.value() == null) {
        // no known dimensions - no need to apply exclusion list
        return List.of();
      } else {
        final List<String> excludedDimensionsToUse;
        if (!excludedDimensions.isEmpty()) {
          // use argument exclusion list
          excludedDimensionsToUse = excludedDimensions;
        } else {
          // use default exclusion list
          excludedDimensionsToUse = optional(datasetConfigDTO.getRcaExcludedDimensions()).map(
              Templatable::value).orElse(List.of());
        }
        final List<String> rcaDimensions = new ArrayList<>(optional(datasetDimensions).map(
            Templatable::value).orElse(List.of()));
        rcaDimensions.removeAll(cleanDimensionStrings(excludedDimensionsToUse));
        return rcaDimensions;
      }
    }
  }

  public static @NonNull List<String> cleanDimensionStrings(
      @NonNull final List<String> dimensions) {
    return dimensions.stream().map(String::trim).collect(Collectors.toList());
  }
}
