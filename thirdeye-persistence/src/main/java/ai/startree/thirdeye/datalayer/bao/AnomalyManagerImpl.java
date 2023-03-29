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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.base.AbstractInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnomalyManagerImpl extends AbstractManagerImpl<AnomalyDTO>
    implements AnomalyManager {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyManagerImpl.class);

  private static final String FIND_BY_TIME =
      "where (startTime < :endTime and endTime > :startTime) "
          + "order by endTime desc";

  private static final String FIND_BY_FUNCTION_ID = "where functionId=:functionId";

  // TODO inject as dependency
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10,
      new ThreadFactoryBuilder().setNameFormat("anomaly-manager-%d").build());

  @Inject
  public AnomalyManagerImpl(final GenericPojoDao genericPojoDao) {
    super(AnomalyDTO.class, genericPojoDao);
  }

  @Override
  public List<AnomalyDTO> findAll() {
    final List<AnomalyDTO> anomalies = super.findAll();
    return decorateWithFeedback(anomalies);
  }

  @Override
  public List<AnomalyDTO> filter(final DaoFilter daoFilter) {
    final List<AnomalyDTO> anomalies = super.filter(daoFilter);
    return decorateWithFeedback(anomalies);
  }

  @Override
  public Long save(final AnomalyDTO anomalyDTO) {
    if (anomalyDTO.getId() != null) {
      update(anomalyDTO);
      return anomalyDTO.getId();
    }
    return saveAnomaly(anomalyDTO, new HashSet<>());
  }

  @Override
  public int update(final AnomalyDTO anomalyDTO) {
    if (anomalyDTO.getId() == null) {
      final Long id = save(anomalyDTO);
      if (id > 0) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return updateAnomaly(anomalyDTO, new HashSet<>());
    }
  }

  private Long saveAnomaly(final AnomalyDTO anomalyDTO,
      final Set<AnomalyDTO> visitedAnomalies) {
    Preconditions.checkNotNull(anomalyDTO);
    Preconditions.checkNotNull(visitedAnomalies);

    visitedAnomalies.add(anomalyDTO);

    if (anomalyDTO.getId() != null) {
      updateAnomaly(anomalyDTO, visitedAnomalies);
      return anomalyDTO.getId();
    }

    final AnomalyDTO mergeAnomalyBean = convertMergeAnomalyDTO2Bean(
        anomalyDTO);
    final Set<Long> childAnomalyIds = saveChildAnomalies(anomalyDTO, visitedAnomalies);
    mergeAnomalyBean.setChildIds(childAnomalyIds);

    final Long id = genericPojoDao.create(mergeAnomalyBean);
    if (id == null) {
      LOG.error("Failed to store anomaly: {}", anomalyDTO);
    }

    anomalyDTO.setId(id);
    return id;
  }

  private int updateAnomaly(final AnomalyDTO anomalyDTO,
      final Set<AnomalyDTO> visitedAnomalies) {
    checkArgument(anomalyDTO.getId() != null,
        "Anomaly id is null. Anomaly id should not be null for an update");

    visitedAnomalies.add(anomalyDTO);

    final AnomalyDTO mergeAnomalyBean = convertMergeAnomalyDTO2Bean(
        anomalyDTO);
    final Set<Long> childAnomalyIds = saveChildAnomalies(anomalyDTO, visitedAnomalies);
    mergeAnomalyBean.setChildIds(childAnomalyIds);

    return genericPojoDao.update(mergeAnomalyBean);
  }

  private Set<Long> saveChildAnomalies(final AnomalyDTO parentAnomaly,
      final Set<AnomalyDTO> visitedAnomalies) {
    final Set<Long> childIds = new HashSet<>();
    final Set<AnomalyDTO> childAnomalies = parentAnomaly.getChildren();
    if (childAnomalies == null || childAnomalies.isEmpty()) {
      // No child anomalies to save
      return childIds;
    }

    for (final AnomalyDTO child : childAnomalies) {
      if (child.getId() == null) {
        // Prevent cycles
        if (visitedAnomalies.contains(child)) {
          throw new IllegalArgumentException("Loop detected! Child anomaly referencing ancestor");
        }
      }
      child.setAuth(parentAnomaly.getAuth());
      child.setChild(true);
      childIds.add(saveAnomaly(child, visitedAnomalies));
    }

    return childIds;
  }

  @Override
  public AnomalyDTO findById(final Long id) {
    final AnomalyDTO anomaly = genericPojoDao.get(id, AnomalyDTO.class);
    if (anomaly == null) {
      return null;
    }
    return decorate(anomaly, new HashSet<>());
  }

  @Override
  public List<AnomalyDTO> findByIds(final List<Long> idList) {
    final List<AnomalyDTO> mergedAnomalyResultBeanList =
        genericPojoDao.get(idList, AnomalyDTO.class);
    if (CollectionUtils.isNotEmpty(mergedAnomalyResultBeanList)) {
      return decorate(mergedAnomalyResultBeanList);
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
  public List<AnomalyDTO> findByStartEndTimeInRangeAndDetectionConfigId(
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

    final List<AnomalyDTO> list = genericPojoDao
        .get(Predicate.AND(predicates.toArray(new Predicate[0])), AnomalyDTO.class);
    return decorate(list);
  }

  @Override
  public List<AnomalyDTO> findByFunctionId(final Long functionId) {
    final Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("functionId", functionId);

    final List<AnomalyDTO> list = genericPojoDao.executeParameterizedSQL(
        FIND_BY_FUNCTION_ID,
        filterParams,
        AnomalyDTO.class);
    return decorate(list);
  }

  @Override
  public List<AnomalyDTO> findByTime(final long startTime, final long endTime) {
    final Map<String, Object> filterParams = new HashMap<>();
    filterParams.put("startTime", startTime);
    filterParams.put("endTime", endTime);

    final List<AnomalyDTO> list =
        genericPojoDao
            .executeParameterizedSQL(FIND_BY_TIME, filterParams, AnomalyDTO.class);
    return decorate(list);
  }

  @Override
  public void updateAnomalyFeedback(final AnomalyDTO entity) {
    final AnomalyFeedbackDTO feedbackDTO = (AnomalyFeedbackDTO) entity.getFeedback();
    if (feedbackDTO != null) {
      if (feedbackDTO.getId() == null) {
        final Long feedbackId = genericPojoDao.create(feedbackDTO);
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
    for (final AnomalyDTO child : entity.getChildren()) {
      child.setFeedback(feedbackDTO);
      updateAnomalyFeedback(child);
    }
    genericPojoDao.update(entity);
  }

  @Override
  public AnomalyDTO findParent(final AnomalyDTO entity) {
    final List<AnomalyDTO> candidates = genericPojoDao.get(Predicate.AND(
        Predicate.EQ("detectionConfigId", entity.getDetectionConfigId()),
        Predicate.LE("startTime", entity.getStartTime()),
        Predicate.GE("endTime", entity.getEndTime())), AnomalyDTO.class);
    for (final AnomalyDTO candidate : candidates) {
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
  public AnomalyDTO convertMergeAnomalyDTO2Bean(final AnomalyDTO entity) {
    optional(entity.getFeedback())
        .map(feedback -> (AnomalyFeedbackDTO) feedback)
        .map(AnomalyFeedbackDTO::getId)
        .ifPresent(entity::setAnomalyFeedbackId);

    return entity;
  }

  public AnomalyDTO decorate(final AnomalyDTO anomaly,
      final Set<Long> visitedAnomalyIds) {

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

  private Set<AnomalyDTO> getChildAnomalies(
      final AnomalyDTO anomalyDTO, final Set<Long> visitedAnomalyIds) {
    final Set<AnomalyDTO> children = new HashSet<>();
    if (anomalyDTO.getChildIds() != null) {
      for (final Long id : anomalyDTO.getChildIds()) {
        if (id == null || visitedAnomalyIds.contains(id)) {
          continue;
        }

        final AnomalyDTO childBean = genericPojoDao.get(id,
            AnomalyDTO.class);
        final AnomalyDTO child = decorate(childBean, visitedAnomalyIds);
        children.add(child);
      }
    }
    return children;
  }

  @Override
  public List<AnomalyDTO> decorate(final List<AnomalyDTO> l) {
    final List<Future<AnomalyDTO>> fList = l.stream()
        .map(anomalyDTO -> EXECUTOR_SERVICE.submit(() -> decorate(anomalyDTO, new HashSet<>())))
        .collect(Collectors.toList());

    final List<AnomalyDTO> outList = new ArrayList<>(l.size());
    for (final Future<AnomalyDTO> f : fList) {
      try {
        outList.add(f.get(60, TimeUnit.SECONDS));
      } catch (final InterruptedException | TimeoutException | ExecutionException e) {
        LOG.warn("Failed to convert MergedAnomalyResultDTO from bean: {}", e.toString());
      }
    }

    return outList;
  }

  private List<AnomalyDTO> decorateWithFeedback(final List<AnomalyDTO> anomalies) {
    final List<Long> feedbackIds = anomalies.stream()
        .map(AnomalyDTO::getAnomalyFeedbackId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    final List<AnomalyFeedbackDTO> feedbacks = genericPojoDao.get(feedbackIds, AnomalyFeedbackDTO.class);
    final Map<Long, AnomalyFeedbackDTO> feedbackMap = feedbacks.stream()
        .collect(Collectors.toMap(AnomalyFeedbackDTO::getId, Function.identity()));
    anomalies.stream()
        .filter(anomaly -> anomaly.getAnomalyFeedbackId() != null)
        .forEach(anomaly -> anomaly.setFeedback(feedbackMap.get(anomaly.getAnomalyFeedbackId())));
    return anomalies;
  }

  @Override
  public List<AnomalyDTO> findByPredicate(final Predicate predicate) {
    final List<AnomalyDTO> beanList = new ArrayList<>(super.findByPredicate(predicate));
    return decorate(beanList);
  }

  @Override
  public long countParentAnomalies(final DaoFilter filter) {
    Predicate predicate = Predicate.EQ("child", false);
    if (filter != null && filter.getPredicate() != null) {
      predicate = Predicate.AND(predicate, filter.getPredicate());
    }
    return count(predicate);
  }

  @Override
  public List<AnomalyDTO> findParentAnomaliesWithFeedback(final DaoFilter filters) {
    Predicate predicate = Predicate.AND(
        Predicate.NEQ("anomalyFeedbackId", 0),
        Predicate.EQ("child", false)
    );
    if (filters != null && filters.getPredicate() != null) {
      predicate = Predicate.AND(predicate, filters.getPredicate());
    }
    return findByPredicate(predicate).stream()
        .map(anomaly -> decorate(anomaly, new HashSet<>()))
        .collect(Collectors.toList());
  }

  @Override
  public List<AnomalyDTO> filter(final AnomalyFilter af) {
    final List<Predicate> predicates = new ArrayList<>();

    optional(af.getCreateTimeWindow())
        .map(AbstractInterval::getStartMillis)
        .map(Timestamp::new)
        .map(ts -> Predicate.GE("createTime", ts))
        .ifPresent(predicates::add);

    optional(af.getCreateTimeWindow())
        .map(AbstractInterval::getEndMillis)
        .map(Timestamp::new)
        .map(ts -> Predicate.LT("createTime", ts))
        .ifPresent(predicates::add);

    optional(af.getAlertId())
        .map(id -> Predicate.EQ("detectionConfigId", id))
        .ifPresent(predicates::add);

    optional(af.getEnumerationItemId())
        .map(id -> Predicate.EQ("enumerationItemId", id))
        .ifPresent(predicates::add);

    final Predicate predicate = Predicate.AND(predicates.toArray(new Predicate[]{}));
    final List<AnomalyDTO> list = filter(new DaoFilter().setPredicate(predicate));
    return decorate(list);
  }
}
