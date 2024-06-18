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

import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface AnomalyManager extends AbstractManager<AnomalyDTO> {

  AnomalyDTO findById(Long id);

  AnomalyDTO findParent(AnomalyDTO entity);

  void updateAnomalyFeedback(AnomalyDTO entity);

  AnomalyDTO convertMergeAnomalyDTO2Bean(AnomalyDTO entity);

  List<AnomalyDTO> decorate(List<AnomalyDTO> anomalyDTOList);

  long count(final @NonNull AnomalyFilter filter);

  List<AnomalyDTO> filter(@NonNull AnomalyFilter anomalyFilter);
}
