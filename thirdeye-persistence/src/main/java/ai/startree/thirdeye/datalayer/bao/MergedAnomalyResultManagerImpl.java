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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MergedAnomalyResultManagerImpl extends AbstractManagerImpl<MergedAnomalyResultDTO>
    implements MergedAnomalyResultManager {

  private static final Logger LOG = LoggerFactory.getLogger(MergedAnomalyResultManagerImpl.class);

  private static final String FIND_BY_TIME =
      "where (startTime < :endTime and endTime > :startTime) "
          + "order by endTime desc";

  private static final String FIND_BY_FUNCTION_ID = "where functionId=:functionId";

  // TODO inject as dependency
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10,
      new ThreadFactoryBuilder().setNameFormat("anomaly-manager-%d").build());

  @Inject
  public MergedAnomalyResultManagerImpl(final GenericPojoDao genericPojoDao,
      final MetricRegistry metricRegistry) {
    super(MergedAnomalyResultDTO.class, genericPojoDao);
    metricRegistry.register("anomalyCountTotal", new CachedGauge<Long>(1, TimeUnit.MINUTES) {
      @Override
      protected Long loadValue() {
        return count();
      }
    });
    metricRegistry.register("parentAnomalyCount", new CachedGauge<Long>(1, TimeUnit.MINUTES) {
      @Override
      protected Long loadValue() {
        return countParentAnomalies();
      }
    });
  }

  @Override
  public Long save(final MergedAnomalyResultDTO mergedAnomalyResultDTO) {
    if (mergedAnomalyResultDTO.getId() != null) {
      update(mergedAnomalyResultDTO);
      return mergedAnomalyResultDTO.getId();
    }
    return saveAnomaly(mergedAnomalyResultDTO, new HashSet<>());
  }

  @Override
  public int update(final MergedAnomalyResultDTO mergedAnomalyResultDTO) {
    if (mergedAnomalyResultDTO.getId() == null) {
      final Long id = save(mergedAnomalyResultDTO);
      if (id > 0) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return updateAnomaly(mergedAnomalyResultDTO, new HashSet<>());
    }
  }

  private Long saveAnomaly(final MergedAnomalyResultDTO mergedAnomalyResultDTO,
      final Set<MergedAnomalyResultDTO> visitedAnomalies) {
    Preconditions.checkNotNull(mergedAnomalyResultDTO);
    Preconditions.checkNotNull(visitedAnomalies);

    visitedAnomalies.add(mergedAnomalyResultDTO);

    if (mergedAnomalyResultDTO.getId() != null) {
      updateAnomaly(mergedAnomalyResultDTO, visitedAnomalies);
      return mergedAnomalyResultDTO.getId();
    }

    final MergedAnomalyResultDTO mergeAnomalyBean = convertMergeAnomalyDTO2Bean(
        mergedAnomalyResultDTO);
    final Set<Long> childAnomalyIds = saveChildAnomalies(mergedAnomalyResultDTO, visitedAnomalies);
    mergeAnomalyBean.setChildIds(childAnomalyIds);

    final Long id = genericPojoDao.put(mergeAnomalyBean);
    mergedAnomalyResultDTO.setId(id);
    return id;
  }

  private int updateAnomaly(final MergedAnomalyResultDTO mergedAnomalyResultDTO,
      final Set<MergedAnomalyResultDTO> visitedAnomalies) {
    checkArgument(mergedAnomalyResultDTO.getId() != null,
        "Anomaly id is null. Anomaly id should not be null for an update");

    visitedAnomalies.add(mergedAnomalyResultDTO);

    final MergedAnomalyResultDTO mergeAnomalyBean = convertMergeAnomalyDTO2Bean(
        mergedAnomalyResultDTO);
    final Set<Long> childAnomalyIds = saveChildAnomalies(mergedAnomalyResultDTO, visitedAnomalies);
    mergeAnomalyBean.setChildIds(childAnomalyIds);

    return genericPojoDao.update(mergeAnomalyBean);
  }

  private Set<Long> saveChildAnomalies(final MergedAnomalyResultDTO parentAnomaly,
      final Set<MergedAnomalyResultDTO> visitedAnomalies) {
    final Set<Long> childIds = new HashSet<>();
    final Set<MergedAnomalyResultDTO> childAnomalies = parentAnomaly.getChildren();
    if (childAnomalies == null || childAnomalies.isEmpty()) {
      // No child anomalies to save
      return childIds;
    }

    for (final MergedAnomalyResultDTO child : childAnomalies) {
      if (child.getId() == null) {
        // Prevent cycles
        if (visitedAnomalies.contains(child)) {
          throw new IllegalArgumentException("Loop detected! Child anomaly referencing ancestor");
        }
      }
      child.setChild(true);
      childIds.add(saveAnomaly(child, visitedAnomalies));
    }

    return childIds;
  }

  @Override
  public MergedAnomalyResultDTO findById(final Long id) {
    final MergedAnomalyResultDTO anomaly = genericPojoDao.get(id, MergedAnomalyResultDTO.class);
    if (anomaly == null) {
      return null;
    }
    return decorate(anomaly, new HashSet<>());
  }

  @Override
  public List<MergedAnomalyResultDTO> findByIds(final List<Long> idList) {
    final List<MergedAnomalyResultDTO> mergedAnomalyResultBeanList =
        genericPojoDao.get(idList, MergedAnomalyResultDTO.class);
    if (CollectionUtils.isNotEmpty(mergedAnomalyResultBeanList)) {
      return convertMergedAnomalyBean2DTO(mergedAnomalyResultBeanList);
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * TODO spyne Refactor. Have a AnomalyFilter object to handle these. Else we'll keep adding params and methods.
   *
   * @return filtered list of anomalies
   */
  @Override
  public List<MergedAnomalyResultDTO> findByStartEndTimeInRangeAndDetectionConfigId(
      final long startTime,
      final long endTime,
      final long alertId,
      final Long enumerationItemId) {
    final List<Predicate> predicates = new ArrayList<>(List.of(
        Predicate.LT("startTime", endTime),
        Predicate.GT("endTime", startTime),
        Predicate.EQ("detectionConfigId", alertId)
    ));
    if (enumerationItemId != null) {
      predicates.add(Predicate.EQ("enumerationItemId", enumerationItemId));
    }

    final List<MergedAnomalyResultDTO> list = genericPojoDao
        .get(Predicate.AND(predicates.toArray(new Predicate[0])), MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByCreatedTimeInRangeAndDetectionConfigId(
      final long startTime,
      final long endTime, final long alertId) {
    final Predicate predicate =
        Predicate.AND(
            Predicate.GE("createTime", new Timestamp(startTime)),
            Predicate.LT("createTime", new Timestamp(endTime)),
            Predicate.EQ("detectionConfigId", alertId));
    final List<MergedAnomalyResultDTO> list = genericPojoDao
        .get(predicate, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByFunctionId(final Long functionId) {
    final Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("functionId", functionId);

    final List<MergedAnomalyResultDTO> list = genericPojoDao.executeParameterizedSQL(
        FIND_BY_FUNCTION_ID,
        filterParams,
        MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByTime(final long startTime, final long endTime) {
    final Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("startTime", startTime);
    filterParams.put("endTime", endTime);

    final List<MergedAnomalyResultDTO> list =
        genericPojoDao
            .executeParameterizedSQL(FIND_BY_TIME, filterParams, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public void updateAnomalyFeedback(final MergedAnomalyResultDTO entity) {
    final AnomalyFeedbackDTO feedbackDTO = (AnomalyFeedbackDTO) entity.getFeedback();
    if (feedbackDTO != null) {
      if (feedbackDTO.getId() == null) {
        final Long feedbackId = genericPojoDao.put(feedbackDTO);
        feedbackDTO.setId(feedbackId);
      } else {
        final AnomalyFeedbackDTO existingFeedback = genericPojoDao
            .get(feedbackDTO.getId(), AnomalyFeedbackDTO.class);
        existingFeedback
            .setFeedbackType(feedbackDTO.getFeedbackType())
            .setComment(feedbackDTO.getComment());
        genericPojoDao.update(existingFeedback);
      }
      entity.setAnomalyFeedbackId(feedbackDTO.getId());
    }
    for (final MergedAnomalyResultDTO child : entity.getChildren()) {
      child.setFeedback(feedbackDTO);
      updateAnomalyFeedback(child);
    }
    genericPojoDao.update(entity);
  }

  @Override
  public MergedAnomalyResultDTO findParent(final MergedAnomalyResultDTO entity) {
    final List<MergedAnomalyResultDTO> candidates = genericPojoDao.get(Predicate.AND(
        Predicate.EQ("detectionConfigId", entity.getDetectionConfigId()),
        Predicate.LE("startTime", entity.getStartTime()),
        Predicate.GE("endTime", entity.getEndTime())), MergedAnomalyResultDTO.class);
    for (final MergedAnomalyResultDTO candidate : candidates) {
      if (candidate.getChildIds() != null && !candidate.getChildIds().isEmpty()) {
        for (final Long id : candidate.getChildIds()) {
          if (entity.getId().equals(id)) {
            return decorate(candidate,
                new HashSet<>(Collections.singleton(candidate.getId())));
          }
        }
      }
    }
    return null;
  }

  @Override
  public MergedAnomalyResultDTO convertMergeAnomalyDTO2Bean(final MergedAnomalyResultDTO entity) {
    optional(entity.getFeedback())
        .map(feedback -> (AnomalyFeedbackDTO) feedback)
        .map(AnomalyFeedbackDTO::getId)
        .ifPresent(entity::setAnomalyFeedbackId);

    optional(entity.getAnomalyFunction())
        .map(AnomalyFunctionDTO::getId)
        .ifPresent(entity::setFunctionId);

    return entity;
  }

  public MergedAnomalyResultDTO decorate(final MergedAnomalyResultDTO anomaly,
      final Set<Long> visitedAnomalyIds) {

    if (anomaly.getFunctionId() != null) {
      final AnomalyFunctionDTO anomalyFunctionDTO = genericPojoDao
          .get(anomaly.getFunctionId(), AnomalyFunctionDTO.class);
      anomaly.setAnomalyFunction(anomalyFunctionDTO);
    }

    if (anomaly.getAnomalyFeedbackId() != null) {
      final AnomalyFeedbackDTO anomalyFeedbackDTO = genericPojoDao
          .get(anomaly.getAnomalyFeedbackId(), AnomalyFeedbackDTO.class);
      anomaly.setFeedback(anomalyFeedbackDTO);
    }

    visitedAnomalyIds.add(anomaly.getId());
    anomaly
        .setChildren(getChildAnomalies(anomaly, visitedAnomalyIds));

    return anomaly;
  }

  private Set<MergedAnomalyResultDTO> getChildAnomalies(
      final MergedAnomalyResultDTO mergedAnomalyResultDTO, final Set<Long> visitedAnomalyIds) {
    final Set<MergedAnomalyResultDTO> children = new HashSet<>();
    if (mergedAnomalyResultDTO.getChildIds() != null) {
      for (final Long id : mergedAnomalyResultDTO.getChildIds()) {
        if (id == null || visitedAnomalyIds.contains(id)) {
          continue;
        }

        final MergedAnomalyResultDTO childBean = genericPojoDao.get(id,
            MergedAnomalyResultDTO.class);
        final MergedAnomalyResultDTO child = decorate(childBean, visitedAnomalyIds);
        children.add(child);
      }
    }
    return children;
  }

  @Override
  public List<MergedAnomalyResultDTO> convertMergedAnomalyBean2DTO(
      final List<MergedAnomalyResultDTO> mergedAnomalyResultBeanList) {
    final List<Future<MergedAnomalyResultDTO>> mergedAnomalyResultDTOFutureList = new ArrayList<>(
        mergedAnomalyResultBeanList.size());
    for (final MergedAnomalyResultDTO mergedAnomalyResultDTO : mergedAnomalyResultBeanList) {
      final Future<MergedAnomalyResultDTO> future =
          EXECUTOR_SERVICE.submit(() -> decorate(mergedAnomalyResultDTO,
              new HashSet<>()));
      mergedAnomalyResultDTOFutureList.add(future);
    }

    final List<MergedAnomalyResultDTO> mergedAnomalyResultDTOList = new ArrayList<>(
        mergedAnomalyResultBeanList.size());
    for (final Future future : mergedAnomalyResultDTOFutureList) {
      try {
        mergedAnomalyResultDTOList.add((MergedAnomalyResultDTO) future.get(60, TimeUnit.SECONDS));
      } catch (final InterruptedException | TimeoutException | ExecutionException e) {
        LOG.warn("Failed to convert MergedAnomalyResultDTO from bean: {}", e.toString());
      }
    }

    return mergedAnomalyResultDTOList;
  }

  @Override
  public List<MergedAnomalyResultDTO> findByPredicate(final Predicate predicate) {
    final List<MergedAnomalyResultDTO> beanList = new ArrayList<>(super.findByPredicate(predicate));
    return convertMergedAnomalyBean2DTO(beanList);
  }

  @Override
  public long countParentAnomalies() {
    return count(Predicate.EQ("child", false));
  }

  @Override
  public long countParentAnomaliesWithoutFeedback() {
    return count(Predicate.AND(
        Predicate.EQ("anomalyFeedbackId", 0L),
        Predicate.EQ("child", false)
    ));
  }

  @Override
  public List<MergedAnomalyResultDTO> findParentAnomaliesWithFeedback() {
    return findByPredicate(Predicate.AND(
        Predicate.NEQ("anomalyFeedbackId", 0),
        Predicate.EQ("child", false)
    )).stream()
        .map(anomaly -> decorate(anomaly, new HashSet<>()))
        .collect(Collectors.toList());
  }
}
