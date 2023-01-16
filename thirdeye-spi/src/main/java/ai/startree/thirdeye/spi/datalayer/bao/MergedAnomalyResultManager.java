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
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.List;

public interface MergedAnomalyResultManager extends AbstractManager<MergedAnomalyResultDTO> {

  MergedAnomalyResultDTO findById(Long id);

  List<MergedAnomalyResultDTO> findByFunctionId(Long functionId);

  /**
   * TODO spyne Refactor. Have a AnomalyFilter object to handle these. Else we'll keep adding params and methods.
   * @return filtered list of anomalies
   */
  List<MergedAnomalyResultDTO> findByStartEndTimeInRangeAndDetectionConfigId(long startTime,
      long endTime,
      long alertId,
      final Long enumerationItemId);

  List<MergedAnomalyResultDTO> findByCreatedTimeInRangeAndDetectionConfigId(long startTime,
      long endTime,
      long alertId);

  List<MergedAnomalyResultDTO> findByTime(long startTime, long endTime);

  MergedAnomalyResultDTO findParent(MergedAnomalyResultDTO entity);

  void updateAnomalyFeedback(MergedAnomalyResultDTO entity);

  MergedAnomalyResultDTO convertMergeAnomalyDTO2Bean(MergedAnomalyResultDTO entity);

  List<MergedAnomalyResultDTO> convertMergedAnomalyBean2DTO(
      List<MergedAnomalyResultDTO> mergedAnomalyResultDTOList);

  long countParentAnomalies(DaoFilter filter);

  List<MergedAnomalyResultDTO> findParentAnomaliesWithFeedback(DaoFilter filter);
}
