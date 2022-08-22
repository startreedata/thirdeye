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
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

  // find a conflicting window
  private static final String FIND_BY_METRIC_TIME =
      "where metric=:metric and (startTime < :endTime and endTime > :startTime) order by endTime desc";

  private static final String FIND_BY_TIME =
      "where (startTime < :endTime and endTime > :startTime) "
          + "order by endTime desc";

  private static final String FIND_BY_FUNCTION_ID = "where functionId=:functionId";

  // TODO inject as dependency
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

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
  public Long save(MergedAnomalyResultDTO mergedAnomalyResultDTO) {
    if (mergedAnomalyResultDTO.getId() != null) {
      update(mergedAnomalyResultDTO);
      return mergedAnomalyResultDTO.getId();
    }
    return saveAnomaly(mergedAnomalyResultDTO, new HashSet<>());
  }

  @Override
  public int update(MergedAnomalyResultDTO mergedAnomalyResultDTO) {
    if (mergedAnomalyResultDTO.getId() == null) {
      Long id = save(mergedAnomalyResultDTO);
      if (id > 0) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return updateAnomaly(mergedAnomalyResultDTO, new HashSet<>());
    }
  }

  private Long saveAnomaly(MergedAnomalyResultDTO mergedAnomalyResultDTO,
      Set<MergedAnomalyResultDTO> visitedAnomalies) {
    Preconditions.checkNotNull(mergedAnomalyResultDTO);
    Preconditions.checkNotNull(visitedAnomalies);

    visitedAnomalies.add(mergedAnomalyResultDTO);

    if (mergedAnomalyResultDTO.getId() != null) {
      updateAnomaly(mergedAnomalyResultDTO, visitedAnomalies);
      return mergedAnomalyResultDTO.getId();
    }

    MergedAnomalyResultDTO mergeAnomalyBean = convertMergeAnomalyDTO2Bean(mergedAnomalyResultDTO);
    Set<Long> childAnomalyIds = saveChildAnomalies(mergedAnomalyResultDTO, visitedAnomalies);
    mergeAnomalyBean.setChildIds(childAnomalyIds);

    Long id = genericPojoDao.put(mergeAnomalyBean);
    mergedAnomalyResultDTO.setId(id);
    return id;
  }

  private int updateAnomaly(MergedAnomalyResultDTO mergedAnomalyResultDTO,
      Set<MergedAnomalyResultDTO> visitedAnomalies) {
    checkArgument(mergedAnomalyResultDTO.getId() != null,
        "Anomaly id is null. Anomaly id should not be null for an update");

    visitedAnomalies.add(mergedAnomalyResultDTO);

    MergedAnomalyResultDTO mergeAnomalyBean = convertMergeAnomalyDTO2Bean(mergedAnomalyResultDTO);
    Set<Long> childAnomalyIds = saveChildAnomalies(mergedAnomalyResultDTO, visitedAnomalies);
    mergeAnomalyBean.setChildIds(childAnomalyIds);

    return genericPojoDao.update(mergeAnomalyBean);
  }

  private Set<Long> saveChildAnomalies(MergedAnomalyResultDTO parentAnomaly,
      Set<MergedAnomalyResultDTO> visitedAnomalies) {
    Set<Long> childIds = new HashSet<>();
    Set<MergedAnomalyResultDTO> childAnomalies = parentAnomaly.getChildren();
    if (childAnomalies == null || childAnomalies.isEmpty()) {
      // No child anomalies to save
      return childIds;
    }

    for (MergedAnomalyResultDTO child : childAnomalies) {
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
  public MergedAnomalyResultDTO findById(Long id) {
    MergedAnomalyResultDTO mergedAnomalyResultBean = genericPojoDao
        .get(id, MergedAnomalyResultDTO.class);
    if (mergedAnomalyResultBean != null) {
      MergedAnomalyResultDTO mergedAnomalyResultDTO;
      mergedAnomalyResultDTO = convertMergedAnomalyBean2DTO(mergedAnomalyResultBean,
          new HashSet<>());
      return mergedAnomalyResultDTO;
    } else {
      return null;
    }
  }

  @Override
  public List<MergedAnomalyResultDTO> findByIds(List<Long> idList) {
    List<MergedAnomalyResultDTO> mergedAnomalyResultBeanList =
        genericPojoDao.get(idList, MergedAnomalyResultDTO.class);
    if (CollectionUtils.isNotEmpty(mergedAnomalyResultBeanList)) {
      return convertMergedAnomalyBean2DTO(mergedAnomalyResultBeanList);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<MergedAnomalyResultDTO> findOverlappingByFunctionId(long functionId,
      long searchWindowStart,
      long searchWindowEnd) {
    // LT and GT are used instead of LE and GE because ThirdEye uses end time exclusive.
    Predicate predicate = Predicate
        .AND(Predicate.LT("startTime", searchWindowEnd), Predicate.GT("endTime", searchWindowStart),
            Predicate.EQ("functionId", functionId));
    List<MergedAnomalyResultDTO> list = genericPojoDao
        .get(predicate, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByStartEndTimeInRangeAndDetectionConfigId(long startTime,
      long endTime,
      long detectionConfigId) {
    Predicate predicate =
        Predicate.AND(Predicate.LT("startTime", endTime), Predicate.GT("endTime", startTime),
            Predicate.EQ("detectionConfigId", detectionConfigId));
    List<MergedAnomalyResultDTO> list = genericPojoDao
        .get(predicate, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByDetectionConfigId(long detectionConfigId) {
    Predicate predicate = Predicate.EQ("detectionConfigId", detectionConfigId);
    List<MergedAnomalyResultDTO> list = genericPojoDao
        .get(predicate, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByCreatedTimeInRangeAndDetectionConfigId(long startTime,
      long endTime, long detectionConfigId) {
    Predicate predicate =
        Predicate.AND(
            Predicate.GE("createTime", new Timestamp(startTime)),
            Predicate.LT("createTime", new Timestamp(endTime)),
            Predicate.EQ("detectionConfigId", detectionConfigId));
    List<MergedAnomalyResultDTO> list = genericPojoDao
        .get(predicate, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  public List<MergedAnomalyResultDTO> findByMetricTime(String metric, long startTime,
      long endTime) {
    Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("metric", metric);
    filterParams.put("startTime", startTime);
    filterParams.put("endTime", endTime);

    List<MergedAnomalyResultDTO> list = genericPojoDao.executeParameterizedSQL(
        FIND_BY_METRIC_TIME, filterParams, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByFunctionId(Long functionId) {
    Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("functionId", functionId);

    List<MergedAnomalyResultDTO> list = genericPojoDao.executeParameterizedSQL(FIND_BY_FUNCTION_ID,
        filterParams, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  @Override
  public List<MergedAnomalyResultDTO> findByTime(long startTime, long endTime) {
    Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("startTime", startTime);
    filterParams.put("endTime", endTime);

    List<MergedAnomalyResultDTO> list =
        genericPojoDao
            .executeParameterizedSQL(FIND_BY_TIME, filterParams, MergedAnomalyResultDTO.class);
    return convertMergedAnomalyBean2DTO(list);
  }

  public void updateAnomalyFeedback(MergedAnomalyResultDTO entity) {
    AnomalyFeedbackDTO feedbackDTO = (AnomalyFeedbackDTO) entity.getFeedback();
    if (feedbackDTO != null) {
      if (feedbackDTO.getId() == null) {
        Long feedbackId = genericPojoDao.put(feedbackDTO);
        feedbackDTO.setId(feedbackId);
      } else {
        AnomalyFeedbackDTO existingFeedback = genericPojoDao
            .get(feedbackDTO.getId(), AnomalyFeedbackDTO.class);
        existingFeedback
            .setFeedbackType(feedbackDTO.getFeedbackType())
            .setComment(feedbackDTO.getComment());
        genericPojoDao.update(existingFeedback);
      }
      entity.setAnomalyFeedbackId(feedbackDTO.getId());
    }
    for (MergedAnomalyResultDTO child : entity.getChildren()) {
      child.setFeedback(feedbackDTO);
      this.updateAnomalyFeedback(child);
    }
    genericPojoDao.update(entity);
  }

  /**
   * Returns a list of merged anomalies that fall (partially) within a given time range for
   * a given metric id
   *
   * <br/><b>NOTE:</b> this function implements a manual join between three tables. This is bad.
   *
   * @param metricId metric id to seek anomalies for
   * @param start time range start (inclusive)
   * @param end time range end (exclusive)
   * @return List of merged anomalies (sorted by start time)
   */
  @Override
  public List<MergedAnomalyResultDTO> findAnomaliesByMetricIdAndTimeRange(Long metricId, long start,
      long end) {
    MetricConfigDTO mbean = genericPojoDao.get(metricId, MetricConfigDTO.class);
    if (mbean == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric id '%d'", metricId));
    }

    return this.getAnomaliesForMetricBeanAndTimeRange(mbean, start, end);
  }

  @Override
  public MergedAnomalyResultDTO findParent(MergedAnomalyResultDTO entity) {
    List<MergedAnomalyResultDTO> candidates = genericPojoDao.get(Predicate.AND(
        Predicate.EQ("detectionConfigId", entity.getDetectionConfigId()),
        Predicate.LE("startTime", entity.getStartTime()),
        Predicate.GE("endTime", entity.getEndTime())), MergedAnomalyResultDTO.class);
    for (MergedAnomalyResultDTO candidate : candidates) {
      if (candidate.getChildIds() != null && !candidate.getChildIds().isEmpty()) {
        for (Long id : candidate.getChildIds()) {
          if (entity.getId().equals(id)) {
            return convertMergedAnomalyBean2DTO(candidate,
                new HashSet<>(Collections.singleton(candidate.getId())));
          }
        }
      }
    }
    return null;
  }

  private List<MergedAnomalyResultDTO> getAnomaliesForMetricBeanAndTimeRange(MetricConfigDTO mbean,
      long start, long end) {

    LOG.info("Fetching anomalies for metric '{}' and dataset '{}'", mbean.getName(),
        mbean.getDataset());

    List<MergedAnomalyResultDTO> anomalyBeans = genericPojoDao.get(
        Predicate.AND(
            Predicate.EQ("metric", mbean.getName()),
            Predicate.EQ("collection", mbean.getDataset()),
            Predicate.LT("startTime", end),
            Predicate.GT("endTime", start)
        ),
        MergedAnomalyResultDTO.class);

    List<MergedAnomalyResultDTO> anomalies = new ArrayList<>(
        convertMergedAnomalyBean2DTO(anomalyBeans));

    anomalies.sort(Comparator.comparingLong(MergedAnomalyResultDTO::getStartTime));

    return anomalies;
  }

  @Override
  public MergedAnomalyResultDTO convertMergeAnomalyDTO2Bean(MergedAnomalyResultDTO entity) {
    optional(entity.getFeedback())
        .map(feedback -> (AnomalyFeedbackDTO) feedback)
        .map(AnomalyFeedbackDTO::getId)
        .ifPresent(entity::setAnomalyFeedbackId);

    optional(entity.getAnomalyFunction())
        .map(AnomalyFunctionDTO::getId)
        .ifPresent(entity::setFunctionId);

    return entity;
  }

  @Override
  public MergedAnomalyResultDTO convertMergedAnomalyBean2DTO(
      MergedAnomalyResultDTO mergedAnomalyResultBean, Set<Long> visitedAnomalyIds) {
    MergedAnomalyResultDTO mergedAnomalyResultDTO = mergedAnomalyResultBean;

    if (mergedAnomalyResultBean.getFunctionId() != null) {
      AnomalyFunctionDTO anomalyFunctionDTO = genericPojoDao
          .get(mergedAnomalyResultBean.getFunctionId(), AnomalyFunctionDTO.class);
      mergedAnomalyResultDTO.setAnomalyFunction(anomalyFunctionDTO);
    }

    if (mergedAnomalyResultBean.getAnomalyFeedbackId() != null) {
      AnomalyFeedbackDTO anomalyFeedbackDTO = genericPojoDao
          .get(mergedAnomalyResultBean.getAnomalyFeedbackId(), AnomalyFeedbackDTO.class);
      mergedAnomalyResultDTO.setFeedback(anomalyFeedbackDTO);
    }

    visitedAnomalyIds.add(mergedAnomalyResultBean.getId());
    mergedAnomalyResultDTO
        .setChildren(getChildAnomalies(mergedAnomalyResultBean, visitedAnomalyIds));

    return mergedAnomalyResultDTO;
  }

  private Set<MergedAnomalyResultDTO> getChildAnomalies(
      MergedAnomalyResultDTO mergedAnomalyResultDTO, Set<Long> visitedAnomalyIds) {
    Set<MergedAnomalyResultDTO> children = new HashSet<>();
    if (mergedAnomalyResultDTO.getChildIds() != null) {
      for (Long id : mergedAnomalyResultDTO.getChildIds()) {
        if (id == null || visitedAnomalyIds.contains(id)) {
          continue;
        }

        MergedAnomalyResultDTO childBean = genericPojoDao.get(id, MergedAnomalyResultDTO.class);
        MergedAnomalyResultDTO child = convertMergedAnomalyBean2DTO(childBean, visitedAnomalyIds);
        children.add(child);
      }
    }
    return children;
  }

  @Override
  public List<MergedAnomalyResultDTO> convertMergedAnomalyBean2DTO(
      List<MergedAnomalyResultDTO> mergedAnomalyResultBeanList) {
    List<Future<MergedAnomalyResultDTO>> mergedAnomalyResultDTOFutureList = new ArrayList<>(
        mergedAnomalyResultBeanList.size());
    for (final MergedAnomalyResultDTO mergedAnomalyResultDTO : mergedAnomalyResultBeanList) {
      Future<MergedAnomalyResultDTO> future =
          EXECUTOR_SERVICE.submit(() -> convertMergedAnomalyBean2DTO(mergedAnomalyResultDTO,
              new HashSet<>()));
      mergedAnomalyResultDTOFutureList.add(future);
    }

    List<MergedAnomalyResultDTO> mergedAnomalyResultDTOList = new ArrayList<>(
        mergedAnomalyResultBeanList.size());
    for (Future future : mergedAnomalyResultDTOFutureList) {
      try {
        mergedAnomalyResultDTOList.add((MergedAnomalyResultDTO) future.get(60, TimeUnit.SECONDS));
      } catch (InterruptedException | TimeoutException | ExecutionException e) {
        LOG.warn("Failed to convert MergedAnomalyResultDTO from bean: {}", e.toString());
      }
    }

    return mergedAnomalyResultDTOList;
  }

  @Override
  public List<MergedAnomalyResultDTO> findByPredicate(Predicate predicate) {
    List<MergedAnomalyResultDTO> dtoList = super.findByPredicate(predicate);
    List<MergedAnomalyResultDTO> beanList = new ArrayList<>();
    for (MergedAnomalyResultDTO mergedAnomalyResultDTO : dtoList) {
      beanList.add(mergedAnomalyResultDTO);
    }
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
        .map(anomaly -> convertMergedAnomalyBean2DTO(anomaly, new HashSet<>()))
        .collect(Collectors.toList());
  }
}
