/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AlertTemplateManager extends AbstractManager<AlertTemplateDTO> {

  /**
   * Look for a template that matches the alertTemplateDto.
   * If id is set:
   * --> findById. 
   * else if the name is set:
   * --> findByName.
   * If found: return
   * If not found:
   *    if alertTemplateDto nodes are set:
   *      return the alertTemplateDto
   *    else:
   *      throw an error
   */
  AlertTemplateDTO findMatch(final @NonNull AlertTemplateDTO alertTemplateDTO, final @Nullable String namespace);
}
