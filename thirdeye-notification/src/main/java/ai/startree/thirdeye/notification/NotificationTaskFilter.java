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

import static ai.startree.thirdeye.spi.util.SpiUtils.bool;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.stream.Collectors.toSet;

import ai.startree.thirdeye.alert.AlertDataRetriever;
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
import java.util.Date;
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
public class NotificationTaskFilter {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationTaskFilter.class);

  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  private static final Set<AnomalyResultSource> ANOMALY_RESULT_SOURCES = Set.of(
      AnomalyResultSource.DEFAULT_ANOMALY_DETECTION,
      AnomalyResultSource.ANOMALY_REPLAY
  );

  private final AnomalyManager anomalyManager;
  private final AlertManager alertManager;
  private final AlertDataRetriever alertDataRetriever;

  @Inject
  public NotificationTaskFilter(final AnomalyManager anomalyManager,
      final AlertManager alertManager,
      final AlertDataRetriever alertDataRetriever) {
    this.anomalyManager = anomalyManager;
    this.alertManager = alertManager;
    this.alertDataRetriever = alertDataRetriever;
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

  // fixme cyril push this down to the database
  private static boolean shouldFilter(final AnomalyDTO anomaly) {
    return anomaly != null
        && !hasFeedback(anomaly)
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

  private long getMaxMergeGap(final AlertDTO alert) {
    return alertDataRetriever.getMergeMaxGap(alert).toStandardDuration().getMillis();
  }

  /**
   * Generate List of Alert Association objects from existing subscription group
   *
   * @return List of Alert Association objects
   */
  private static List<AlertAssociationDto> migrateOlderSchema(final SubscriptionGroupDTO sg) {
    return optional(sg.getProperties())
        .map(p -> (List<?>) p.get(PROP_DETECTION_CONFIG_IDS))
        .orElse(Collections.emptyList())
        .stream()
        .filter(Objects::nonNull)
        .filter(Number.class::isInstance)
        .map(Number.class::cast)
        .map(Number::longValue)
        .map(NotificationTaskFilter::newAlertRef)
        .map(alert -> new AlertAssociationDto().setAlert(alert))
        .collect(Collectors.toList());
  }
  
  /**
   * Find anomalies for the given subscription group given an end time.
   *
   * @param sg subscription group
   * @param endTime end time
   * @return result This returns a new NotificationTaskFilterResult object containing the
   *     subscription group, anomalies, completed anomalies and other metadata
   */
  public NotificationTaskFilterResult filter(final SubscriptionGroupDTO sg, final long endTime) {
    final Set<AnomalyDTO> anomalies = filterAnomalies(sg, endTime);

    final var ids = anomalies.stream()
        .map(AnomalyDTO::getId)
        .collect(toSet());

    // remove anomalies that are already being notified
    final Set<AnomalyDTO> completedAnomalies = filterCompletedAnomalies(sg);
    completedAnomalies.removeIf(a -> ids.contains(a.getId()));

    return new NotificationTaskFilterResult()
        .setSubscriptionGroup(sg)
        .setAnomalies(anomalies)
        .setCompletedAnomalies(completedAnomalies);
  }

  /**
   * Find anomalies for the given subscription group given an end time.
   *
   * @param sg subscription group
   * @param endTime end time
   * @return set of anomalies
   */
  @VisibleForTesting
  Set<AnomalyDTO> filterAnomalies(final SubscriptionGroupDTO sg, final long endTime) {
    final List<AlertAssociationDto> alertAssociations = optional(sg.getAlertAssociations())
        .orElseGet(() -> migrateOlderSchema(sg));

    // Fetch all the anomalies to be notified to the recipients
    return alertAssociations.stream()
        .filter(aa -> isAlertActive(aa.getAlert().getId()))
        .map(aa -> buildAnomalyFilter(aa, sg, endTime))
        .map(f -> filterAnomalies(f, sg.getId(), "anomalies"))
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  @VisibleForTesting
  Set<AnomalyDTO> filterCompletedAnomalies(final SubscriptionGroupDTO sg) {
    final List<AlertAssociationDto> alertAssociations = optional(sg.getAlertAssociations())
        .orElseGet(() -> migrateOlderSchema(sg));

    return alertAssociations.stream()
        .filter(aa -> isAlertActive(aa.getAlert().getId()))
        .filter(aa -> aa.getAnomalyCompletionWatermark() != null)
        .map(this::buildAnomalyFilterCompletedAnomalies)
        .map(f -> filterAnomalies(f, sg.getId(), "completed anomalies"))
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  private AnomalyFilter buildAnomalyFilterCompletedAnomalies(final AlertAssociationDto aa) {
    final Date watermark = optional(aa.getAnomalyCompletionWatermark())
        .orElseThrow(() -> new IllegalStateException("Invalid code path. Watermark is null"));

    final long alertId = aa.getAlert().getId();
    final AlertDTO alert = alertManager.findById(alertId);
    final long endTimeIsLt = alert.getLastTimestamp() - getMaxMergeGap(alert);

    return new AnomalyFilter()
        .setIsChild(false)
        .setAlertId(alertId)
        .setIsIgnored(false)
        .setEndTimeIsGte(watermark.getTime())
        .setEndTimeIsLt(endTimeIsLt);
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
    final long startTime = optional(sg.getVectorClocks())
        .map(v -> v.get(alertId))
        .orElse(0L);

    // Do not notify anomalies older than MAX_ANOMALY_NOTIFICATION_LOOKBACK
    final long minStartTime = createTimeEnd - Constants.NOTIFICATION_ANOMALY_MAX_LOOKBACK_MS;
    final long createTimeStart = Math.max(startTime, minStartTime);

    final AnomalyFilter f = new AnomalyFilter()
        .setCreateTimeWindow(new Interval(createTimeStart + 1, createTimeEnd))
        .setIsChild(false) // Notify only parent anomalies
        .setIsIgnored(false)
        .setAlertId(alertId);

    /*
     * Do not notify historical anomalies if the end time of the anomaly is before the
     * max of the alert create time and subscription group create time.
     */
    if (!bool(sg.getNotifyHistoricalAnomalies())) {
      long earliestAnomalyEndTime = Math.max(
          alert.getCreateTime().getTime(),
          sg.getCreateTime().getTime());
      if (aa.getCreateTime() != null) {
        earliestAnomalyEndTime = Math.max(earliestAnomalyEndTime, aa.getCreateTime().getTime());
      }
      f.setEndTimeIsGte(earliestAnomalyEndTime);
    }

    optional(aa.getEnumerationItem())
        .map(AbstractDTO::getId)
        .ifPresent(f::setEnumerationItemId);

    return f;
  }

  @VisibleForTesting
  Set<AnomalyDTO> filterAnomalies(final AnomalyFilter f,
      final Long subscriptionGroupId,
      final String logContext) {
    final List<AnomalyDTO> candidates = anomalyManager.filter(f);

    final Set<AnomalyDTO> anomaliesToBeNotified = candidates.stream()
        .filter(NotificationTaskFilter::shouldFilter)
        .collect(toSet());

    String createdMsg = "";
    if (f.getCreateTimeWindow() != null) {
      createdMsg = String.format("Created between %s and %s (%s and %s System Time)",
          f.getCreateTimeWindow().getStartMillis(),
          f.getCreateTimeWindow().getEndMillis(),
          toFormattedDate(f.getCreateTimeWindow().getStartMillis()),
          toFormattedDate(f.getCreateTimeWindow().getEndMillis()));
    }

    LOG.info("Subscription Group: {} Alert: {} context: {}. {}/{} filtered. {}",
        subscriptionGroupId,
        f.getAlertId(),
        logContext,
        anomaliesToBeNotified.size(),
        candidates.size(),
        createdMsg);


    return anomaliesToBeNotified;
  }
}
