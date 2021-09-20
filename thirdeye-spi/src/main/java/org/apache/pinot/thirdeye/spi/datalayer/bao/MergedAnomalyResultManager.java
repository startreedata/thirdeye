/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.datalayer.bao;

import java.util.List;
import java.util.Set;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;

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
