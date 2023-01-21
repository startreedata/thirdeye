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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import java.util.List;

public interface MergedAnomalyResultManager extends AbstractManager<AnomalyDTO> {

  AnomalyDTO findById(Long id);

  List<AnomalyDTO> findByFunctionId(Long functionId);

  /**
   * TODO spyne Refactor. Have a AnomalyFilter object to handle these. Else we'll keep adding params and methods.
   * @return filtered list of anomalies
   */
  List<AnomalyDTO> findByStartEndTimeInRangeAndDetectionConfigId(long startTime,
      long endTime,
      long alertId,
      final Long enumerationItemId);

  List<AnomalyDTO> findByCreatedTimeInRangeAndDetectionConfigId(long startTime,
      long endTime,
      long alertId);

  List<AnomalyDTO> findByTime(long startTime, long endTime);

  AnomalyDTO findParent(AnomalyDTO entity);

  void updateAnomalyFeedback(AnomalyDTO entity);

  AnomalyDTO convertMergeAnomalyDTO2Bean(AnomalyDTO entity);

  List<AnomalyDTO> convertMergedAnomalyBean2DTO(
      List<AnomalyDTO> anomalyDTOList);

  long countParentAnomalies(DaoFilter filter);

  List<AnomalyDTO> findParentAnomaliesWithFeedback(DaoFilter filter);
}
