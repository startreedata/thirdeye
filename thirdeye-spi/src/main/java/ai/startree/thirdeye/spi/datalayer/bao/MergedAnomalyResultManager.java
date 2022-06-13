/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.List;
import java.util.Set;

public interface MergedAnomalyResultManager extends AbstractManager<MergedAnomalyResultDTO> {

  MergedAnomalyResultDTO findById(Long id);

  List<MergedAnomalyResultDTO> findOverlappingByFunctionId(long functionId,
      long conflictWindowStart,
      long conflictWindowEnd);

  List<MergedAnomalyResultDTO> findByMetricTime(String metric, long startTime, long endTime);

  List<MergedAnomalyResultDTO> findByFunctionId(Long functionId);

  List<MergedAnomalyResultDTO> findByDetectionConfigId(long detectionConfigId);

  List<MergedAnomalyResultDTO> findByStartEndTimeInRangeAndDetectionConfigId(long startTime,
      long endTime, long detectionConfigId);

  List<MergedAnomalyResultDTO> findByCreatedTimeInRangeAndDetectionConfigId(long startTime,
      long endTime, long detectionConfigId);

  List<MergedAnomalyResultDTO> findByTime(long startTime, long endTime);

  List<MergedAnomalyResultDTO> findAnomaliesByMetricIdAndTimeRange(Long metricId, long start,
      long end);

  MergedAnomalyResultDTO findParent(MergedAnomalyResultDTO entity);

  void updateAnomalyFeedback(MergedAnomalyResultDTO entity);

  MergedAnomalyResultDTO convertMergeAnomalyDTO2Bean(MergedAnomalyResultDTO entity);

  MergedAnomalyResultDTO convertMergedAnomalyBean2DTO(
      MergedAnomalyResultDTO mergedAnomalyResultDTO, Set<Long> visitedAnomalyIds);

  List<MergedAnomalyResultDTO> convertMergedAnomalyBean2DTO(
      List<MergedAnomalyResultDTO> mergedAnomalyResultDTOList);
}
