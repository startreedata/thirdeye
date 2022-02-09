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
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.GroupedAnomalyResultsManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;

@Singleton
public class GroupedAnomalyResultsManagerImpl extends AbstractManagerImpl<GroupedAnomalyResultsDTO>
    implements GroupedAnomalyResultsManager {

  protected static final ModelMapper MODEL_MAPPER = new ModelMapper();
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  public GroupedAnomalyResultsManagerImpl(GenericPojoDao genericPojoDao,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(GroupedAnomalyResultsDTO.class, genericPojoDao);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  @Override
  public Long save(GroupedAnomalyResultsDTO groupedAnomalyResultDTO) {
    if (groupedAnomalyResultDTO.getId() != null) {
      update(groupedAnomalyResultDTO);
      return groupedAnomalyResultDTO.getId();
    } else {
      GroupedAnomalyResultsDTO bean = convertGroupedAnomalyDTO2Bean(groupedAnomalyResultDTO);
      Long id = genericPojoDao.put(bean);
      groupedAnomalyResultDTO.setId(id);
      return id;
    }
  }

  @Override
  public int update(GroupedAnomalyResultsDTO groupedAnomalyResultDTO) {
    if (groupedAnomalyResultDTO.getId() == null) {
      Long id = save(groupedAnomalyResultDTO);
      if (id > 0) {
        return 1;
      } else {
        return 0;
      }
    } else {
      GroupedAnomalyResultsDTO GroupedAnomalyResultsDTO = convertGroupedAnomalyDTO2Bean(
          groupedAnomalyResultDTO);
      return genericPojoDao.update(GroupedAnomalyResultsDTO);
    }
  }

  @Override
  public GroupedAnomalyResultsDTO findById(Long id) {
    GroupedAnomalyResultsDTO bean = genericPojoDao.get(id, GroupedAnomalyResultsDTO.class);
    if (bean != null) {
      return convertGroupedAnomalyBean2DTO(bean);
    } else {
      return null;
    }
  }

  @Override
  public GroupedAnomalyResultsDTO findMostRecentInTimeWindow(long alertConfigId, String dimensions,
      long windowStart,
      long windowEnd) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("alertConfigId", alertConfigId), Predicate.EQ("dimensions", dimensions),
            Predicate.GT("endTime", windowStart), Predicate.LE("endTime", windowEnd));

    List<GroupedAnomalyResultsDTO> GroupedAnomalyResultsDTOs =
        genericPojoDao.get(predicate, GroupedAnomalyResultsDTO.class);
    if (CollectionUtils.isNotEmpty(GroupedAnomalyResultsDTOs)) {
      // Sort grouped anomaly results bean in the natural order of their end time.
      Collections.sort(GroupedAnomalyResultsDTOs, (o1, o2) -> {
        int endTimeCompare = (int) (o1.getEndTime() - o2.getEndTime());
        if (endTimeCompare != 0) {
          return endTimeCompare;
        } else {
          return (int) (o1.getId() - o2.getId());
        }
      });
      return convertGroupedAnomalyBean2DTO(
          GroupedAnomalyResultsDTOs.get(GroupedAnomalyResultsDTOs.size() - 1));
    } else {
      return null;
    }
  }

  protected GroupedAnomalyResultsDTO convertGroupedAnomalyDTO2Bean(
      GroupedAnomalyResultsDTO entity) {
    GroupedAnomalyResultsDTO bean = entity;
    if (CollectionUtils.isNotEmpty(entity.getAnomalyResults())) {
      List<Long> mergedAnomalyId = new ArrayList<>();
      for (MergedAnomalyResultDTO mergedAnomalyResultDTO : entity.getAnomalyResults()) {
        mergedAnomalyId.add(mergedAnomalyResultDTO.getId());
      }
      bean.setAnomalyResultsId(mergedAnomalyId);
    }
    return bean;
  }

  /**
   * Convert grouped anomaly bean to DTO. The merged anomaly results in this group are also
   * converted to their
   * corresponding DTO class; however, the raw anomalies of those merged results are not converted.
   *
   * @param GroupedAnomalyResultsDTO the bean class to be converted
   * @return the DTO class that consists of the DTO of merged anomalies whose raw anomalies are not
   *     converted from bean.
   */
  protected GroupedAnomalyResultsDTO convertGroupedAnomalyBean2DTO(
      GroupedAnomalyResultsDTO GroupedAnomalyResultsDTO) {
    GroupedAnomalyResultsDTO groupedAnomalyResultsDTO =
        MODEL_MAPPER.map(GroupedAnomalyResultsDTO, GroupedAnomalyResultsDTO.class);

    if (CollectionUtils.isNotEmpty(GroupedAnomalyResultsDTO.getAnomalyResultsId())) {
      List<MergedAnomalyResultDTO> list =
          genericPojoDao
              .get(GroupedAnomalyResultsDTO.getAnomalyResultsId(), MergedAnomalyResultDTO.class);
      List<MergedAnomalyResultDTO> mergedAnomalyResults = mergedAnomalyResultManager
          .convertMergedAnomalyBean2DTO(list);
      groupedAnomalyResultsDTO.setAnomalyResults(mergedAnomalyResults);
    }

    return groupedAnomalyResultsDTO;
  }
}
