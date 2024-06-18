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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.Constants.METRICS_CACHE_TIMEOUT;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Suppliers.memoizeWithExpiration;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.base.AbstractInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnomalyManagerImpl extends AbstractManagerImpl<AnomalyDTO>
    implements AnomalyManager {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyManagerImpl.class);

  private static final AnomalyFilter NOT_CHILD_NOT_IGNORED_FILTER = new AnomalyFilter()
      .setIsChild(false).setIsIgnored(false);
  private static final AnomalyFilter HAS_FEEDBACK_FILTER = NOT_CHILD_NOT_IGNORED_FILTER
      .copy().setHasFeedback(true);
  private static final AnomalyFilter HAS_NO_FEEDBACK_FILTER = NOT_CHILD_NOT_IGNORED_FILTER
      .copy().setHasFeedback(false);

  // TODO inject as dependency
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10,
      new ThreadFactoryBuilder().setNameFormat("anomaly-manager-%d").build());

  @Inject
  public AnomalyManagerImpl(final GenericPojoDao genericPojoDao) {
    super(AnomalyDTO.class, genericPojoDao);

    Gauge.builder("thirdeye_anomalies",
            memoizeWithExpiration(() -> count(NOT_CHILD_NOT_IGNORED_FILTER),
                METRICS_CACHE_TIMEOUT.toMinutes(),
                TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
    Gauge.builder("thirdeye_anomaly_feedbacks",
            memoizeWithExpiration(() -> count(HAS_FEEDBACK_FILTER), METRICS_CACHE_TIMEOUT.toMinutes(),
                TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);

    final Supplier<ConfusionMatrix> cachedConfusionMatrix = memoizeWithExpiration(
        this::computeConfusionMatrixForAnomalies, METRICS_CACHE_TIMEOUT.toMinutes(),
        TimeUnit.MINUTES);
    Gauge.builder("thirdeye_anomaly_precision",
            () -> cachedConfusionMatrix.get().getPrecision())
        .register(Metrics.globalRegistry);
    Gauge.builder("thirdeye_anomaly_response_rate",
            () -> cachedConfusionMatrix.get().getResponseRate())
        .register(Metrics.globalRegistry);
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

  @Override
  public List<AnomalyDTO> findAll() {
    final List<AnomalyDTO> anomalies = super.findAll();
    return decorateWithFeedback(anomalies);
  }

  @Override
  public List<AnomalyDTO> filter(final DaoFilter daoFilter) {
    final List<AnomalyDTO> anomalies = super.filter(daoFilter);
    // FIXME CYRIL this filter is only decorating with feedback - while some others decorate with feedback and children
    return decorateWithFeedback(anomalies);
  }

  @Override
  public void updateAnomalyFeedback(final AnomalyDTO entity) {
    final AnomalyFeedbackDTO feedbackDTO = (AnomalyFeedbackDTO) entity.getFeedback();
    if (feedbackDTO != null) {
      if (feedbackDTO.getId() == null) {
        feedbackDTO.setCreatedBy(feedbackDTO.getUpdatedBy());
        final Long feedbackId = genericPojoDao.create(feedbackDTO);
        feedbackDTO.setId(feedbackId);
      } else {
        final AnomalyFeedbackDTO existingFeedback = genericPojoDao
            .get(feedbackDTO.getId(), AnomalyFeedbackDTO.class);
        existingFeedback
            .setFeedbackType(feedbackDTO.getFeedbackType())
            .setComment(feedbackDTO.getComment())
            .setCause(feedbackDTO.getCause())
            .setUpdatedBy(feedbackDTO.getUpdatedBy());
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
    final Predicate predicate = Predicate.AND(
        Predicate.EQ("detectionConfigId", entity.getDetectionConfigId()),
        Predicate.LE("startTime", entity.getStartTime()),
        Predicate.GE("endTime", entity.getEndTime()));
    final List<AnomalyDTO> candidates = genericPojoDao.get(
        new DaoFilter().setPredicate(predicate).setBeanClass(AnomalyDTO.class));
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

  @Override
  public List<AnomalyDTO> decorate(final List<AnomalyDTO> l) {
    final List<Future<AnomalyDTO>> fList = l.stream()
        .map(anomalyDTO -> EXECUTOR_SERVICE.submit(() -> decorate(anomalyDTO, new HashSet<>())))
        .toList();

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

  private AnomalyDTO decorate(final AnomalyDTO anomaly, final Set<Long> visitedAnomalyIds) {
    if (anomaly.getAnomalyFeedbackId() != null) {
      final AnomalyFeedbackDTO anomalyFeedbackDTO = genericPojoDao
          .get(anomaly.getAnomalyFeedbackId(), AnomalyFeedbackDTO.class);
      anomaly.setFeedback(anomalyFeedbackDTO);
    }

    visitedAnomalyIds.add(anomaly.getId());
    anomaly.setChildren(getChildAnomalies(anomaly, visitedAnomalyIds));

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

  private List<AnomalyDTO> decorateWithFeedback(final List<AnomalyDTO> anomalies) {
    final List<Long> feedbackIds = anomalies.stream()
        .map(AnomalyDTO::getAnomalyFeedbackId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    final List<AnomalyFeedbackDTO> feedbacks = genericPojoDao.get(feedbackIds,
        AnomalyFeedbackDTO.class);
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
  public List<AnomalyDTO> filter(final @NonNull AnomalyFilter af) {
    final Predicate predicate = toPredicate(af);
    final List<AnomalyDTO> list = filter(new DaoFilter().setPredicate(predicate));
    return decorate(list);
  }
  
  @Override
  public long count(final @NonNull AnomalyFilter filter) {
    Predicate finalPredicate = toPredicate(filter);
    return count(finalPredicate);
  }

  private Predicate toPredicate(final AnomalyFilter af) {
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

    optional(af.isChild())
        .map(isChild -> Predicate.EQ("child", isChild))
        .ifPresent(predicates::add);

    optional(af.hasFeedback()).map(
        hasFeedback -> hasFeedback ? Predicate.NEQ("anomalyFeedbackId", 0)
            : Predicate.EQ("anomalyFeedbackId", 0)).ifPresent(predicates::add);

    optional(af.isIgnored())
        .map(isIgnored -> Predicate.EQ("ignored", isIgnored))
        .ifPresent(predicates::add);

    optional(af.getStartEndWindow())
        .map(window -> Predicate.AND(Predicate.LT("startTime", window.getEndMillis()),
            Predicate.GT("endTime", window.getStartMillis())))
        .ifPresent(predicates::add);

    optional(af.getEndTimeIsGte())
        .map(endTime -> Predicate.GE("endTime", endTime))
        .ifPresent(predicates::add);

    optional(af.getEndTimeIsLt())
        .map(endTime -> Predicate.LT("endTime", endTime))
        .ifPresent(predicates::add);

    optional(af.getEndTimeIsLte())
        .map(endTime -> Predicate.LE("endTime", endTime))
        .ifPresent(predicates::add);

    optional(af.getStartTimeIsGte())
        .map(start -> Predicate.GE("startTime", start))
        .ifPresent(predicates::add);

    return Predicate.AND(predicates.toArray(new Predicate[]{}));
  }

  private ConfusionMatrix computeConfusionMatrixForAnomalies() {
    final long withNoFeedbackCount = count(HAS_NO_FEEDBACK_FILTER);
    final List<AnomalyDTO> withFeedbacks = filter(HAS_FEEDBACK_FILTER);
    // todo cyril - would be simpler to be able to count by feedback type in the db directly - here we parse and load anomalies in memory
    
    return new ConfusionMatrix()
        .addUnclassified(withNoFeedbackCount)
        .addFromAnomalies(withFeedbacks);
  }

  public static class ConfusionMatrix {
    private long truePositive = 0;
    private long falsePositive = 0;
    private long trueNegative = 0;
    private long falseNegative = 0;
    private long unclassified = 0;

    public ConfusionMatrix addFromAnomalies(final @NonNull List<AnomalyDTO> anomalies) {
      for (final AnomalyDTO a: anomalies) {
        final @Nullable AnomalyFeedback feedback = a.getFeedback();
        if (feedback == null) {
          addUnclassified(1);
        } else {
          switch (feedback.getFeedbackType()) {
            case ANOMALY_EXPECTED:
            case ANOMALY_NEW_TREND:
            case ANOMALY:
              addTruePositive(1);
              break;
            case NOT_ANOMALY:
              addFalsePositive(1);
              break;
            case NO_FEEDBACK:
              addUnclassified(1);
              break;
            default:
              throw new UnsupportedOperationException(
                  "Feedback type not implemented for stats: %s".formatted(feedback.getFeedbackType()));
          }
        }
      }
      return this;
    }

    public long getTruePositive() {
      return truePositive;
    }

    public ConfusionMatrix addTruePositive(final long value) {
      this.truePositive += value;
      return this;
    }

    public long getFalsePositive() {
      return falsePositive;
    }

    public ConfusionMatrix addFalsePositive(final long value) {
      this.falsePositive += value;
      return this;
    }

    public long getTrueNegative() {
      return trueNegative;
    }

    public ConfusionMatrix addTrueNegative(final long value) {
      this.trueNegative += value;
      return this;
    }

    public long getFalseNegative() {
      return falseNegative;
    }

    public ConfusionMatrix addFalseNegative(final long value) {
      this.falseNegative += value;
      return this;
    }

    public long getUnclassified() {
      return unclassified;
    }

    public ConfusionMatrix addUnclassified(final long value) {
      this.unclassified += value;
      return this;
    }

    public void incTruePositive() {
      this.truePositive++;
    }

    public void incTrueNegative() {
      this.trueNegative++;
    }

    public void incFalsePositive() {
      this.falsePositive++;
    }

    public void incFalseNegative() {
      this.falseNegative++;
    }

    public void incUnclassified() {
      this.unclassified++;
    }

    public double getPrecision() {
      if(truePositive == 0) {
        return 0;
      } else {
        return truePositive / (double) (truePositive + falsePositive);
      }
    }

    public double getResponseRate() {
      if(unclassified == 0) {
        return 1;
      } else {
        return 1 - unclassified / (double) (truePositive + falsePositive + trueNegative + falseNegative + unclassified);
      }
    }
  }
}
