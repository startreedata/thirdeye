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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.AnomalyUtils.isIgnore;
import static ai.startree.thirdeye.spi.util.SpiUtils.bool;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.stream.Collectors.toSet;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Subscription Group filter collects all anomalies and returns back a Result
 */
@Singleton
public class SubscriptionGroupFilter {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionGroupFilter.class);

  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  private static final Set<AnomalyResultSource> ANOMALY_RESULT_SOURCES = Set.of(
      AnomalyResultSource.DEFAULT_ANOMALY_DETECTION,
      AnomalyResultSource.ANOMALY_REPLAY
  );

  private final AnomalyManager anomalyManager;
  private final AlertManager alertManager;

  @Inject
  public SubscriptionGroupFilter(final AnomalyManager anomalyManager,
      final AlertManager alertManager) {
    this.anomalyManager = anomalyManager;
    this.alertManager = alertManager;
  }

  /**
   * Helper to determine presence of user-feedback for an anomaly
   *
   * @param anomaly anomaly
   * @return {@code true} if feedback exists and is TRUE or FALSE, {@code false} otherwise
   */
  private static boolean hasFeedback(final AnomalyDTO anomaly) {
    return anomaly.getFeedback() != null
        && !anomaly.getFeedback().getFeedbackType().isUnresolved();
  }

  private static boolean shouldFilter(final AnomalyDTO anomaly) {
    return anomaly != null
        && !hasFeedback(anomaly)
        && !isIgnore(anomaly)
        && ANOMALY_RESULT_SOURCES.contains(anomaly.getAnomalyResultSource());
  }

  private static AlertDTO newAlertRef(final Long id) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(id);
    return alert;
  }

  private static String toFormattedDate(final long ts) {
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(ts));
  }

  /**
   * Find anomalies for the given subscription group given an end time.
   *
   * @param sg subscription group
   * @param endTime end time
   * @return set of anomalies
   */
  public Set<AnomalyDTO> filter(final SubscriptionGroupDTO sg, final long endTime) {
    final List<AlertAssociationDto> alertAssociations = optional(sg.getAlertAssociations())
        .orElseGet(() -> migrateOlderSchema(sg));

    // Fetch all the anomalies to be notified to the recipients
    return alertAssociations.stream()
        .filter(aa -> isAlertActive(aa.getAlert().getId()))
        .map(aa -> buildAnomalyFilter(aa, sg, endTime))
        .map(f -> filterAnomalies(f, sg.getId()))
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  /**
   * Generate List of Alert Association objects from existing subscription group
   *
   * @return List of Alert Association objects
   */
  private List<AlertAssociationDto> migrateOlderSchema(final SubscriptionGroupDTO sg) {
    return optional(sg.getProperties())
        .map(p -> (List<?>) p.get(PROP_DETECTION_CONFIG_IDS))
        .orElse(Collections.emptyList())
        .stream()
        .filter(Objects::nonNull)
        .filter(Number.class::isInstance)
        .map(Number.class::cast)
        .map(Number::longValue)
        .map(SubscriptionGroupFilter::newAlertRef)
        .map(alert -> new AlertAssociationDto().setAlert(alert))
        .collect(Collectors.toList());
  }

  private boolean isAlertActive(final long alertId) {
    final AlertDTO alert = alertManager.findById(alertId);
    return alert != null && alert.isActive();
  }

  private AnomalyFilter buildAnomalyFilter(final AlertAssociationDto aa,
      final SubscriptionGroupDTO sg,
      final long createTimeEnd) {
    final long alertId = aa.getAlert().getId();
    final AlertDTO alert = alertManager.findById(alertId);
    long startTime = optional(sg.getVectorClocks())
        .map(v -> v.get(alertId))
        .orElse(0L);

    // Do not notify anomalies older than MAX_ANOMALY_NOTIFICATION_LOOKBACK
    final long minStartTime = createTimeEnd - Constants.NOTIFICATION_ANOMALY_MAX_LOOKBACK_MS;
    final long createTimeStart = Math.max(startTime, minStartTime);

    final AnomalyFilter f = new AnomalyFilter()
        .setCreateTimeWindow(new Interval(createTimeStart + 1, createTimeEnd))
        .setIsChild(false) // Notify only parent anomalies
        .setAlertId(alertId);

    /*
     * Do not notify historical anomalies if the end time of the anomaly is before the
     * max of the alert create time and subscription group create time.
     *
     * TODO spyne This should also take the alert association tine when available.
     */
    if (!bool(sg.getNotifyHistoricalAnomalies())) {
      f.setEndTimeIsGte(Math.max(alert.getCreateTime().getTime(), sg.getCreateTime().getTime()));
    }

    optional(aa.getEnumerationItem())
        .map(AbstractDTO::getId)
        .ifPresent(f::setEnumerationItemId);

    return f;
  }

  @VisibleForTesting
  Set<AnomalyDTO> filterAnomalies(final AnomalyFilter f, final Long subscriptionGroupId) {
    final List<AnomalyDTO> candidates = anomalyManager.filter(f);

    final Set<AnomalyDTO> anomaliesToBeNotified = candidates.stream()
        .filter(SubscriptionGroupFilter::shouldFilter)
        .collect(toSet());

    LOG.info("Subscription Group: {} Alert: {}. "
            + "{} out of {} candidates to be notified. Created between {} and {} ({} and {} System Time)",
        subscriptionGroupId,
        f.getAlertId(),
        anomaliesToBeNotified.size(),
        candidates.size(),
        f.getCreateTimeWindow().getStartMillis(),
        f.getCreateTimeWindow().getEndMillis(),
        toFormattedDate(f.getCreateTimeWindow().getStartMillis()),
        toFormattedDate(f.getCreateTimeWindow().getEndMillis()));

    return anomaliesToBeNotified;
  }
}
