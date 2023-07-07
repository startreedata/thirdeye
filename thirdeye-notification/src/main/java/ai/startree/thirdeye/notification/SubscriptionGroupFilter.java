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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.notification.SubscriptionGroupWatermarkManager.findStartTime;
import static ai.startree.thirdeye.notification.SubscriptionGroupWatermarkManager.newVectorClocks;
import static ai.startree.thirdeye.spi.util.AnomalyUtils.isIgnore;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.stream.Collectors.toSet;

import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.Interval;

/**
 * The Subscription Group filter collects all anomalies and returns back a Result
 */
@Singleton
public class SubscriptionGroupFilter {

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

  private static boolean shouldFilter(final AnomalyDTO anomaly, final long startTime) {
    return anomaly != null
        && !anomaly.isChild()
        && !hasFeedback(anomaly)
        && anomaly.getCreateTime().getTime() > startTime
        && !isIgnore(anomaly)
        && ANOMALY_RESULT_SOURCES.contains(anomaly.getAnomalyResultSource());
  }

  private static AlertDTO fromId(final Long id) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(id);
    return alert;
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
        .orElseGet(() -> generate(sg));

    // Fetch all the anomalies to be notified to the recipients
    final Map<Long, Long> vectorClocks = newVectorClocks(alertAssociations, sg.getVectorClocks());
    return alertAssociations.stream()
        .filter(aa -> isAlertActive(aa.getAlert().getId()))
        .map(alertAssociation -> findAnomaliesForAlertAssociation(alertAssociation,
            vectorClocks,
            endTime))
        .flatMap(Collection::stream)
        .collect(toSet());
  }

  /**
   * Generate List of Alert Association objects from existing subscription group
   *
   * @return List of Alert Association objects
   */
  private List<AlertAssociationDto> generate(final SubscriptionGroupDTO subscriptionGroup) {
    final List<Long> alertIds = SubscriptionGroupFilterUtils.getLongs(subscriptionGroup.getProperties()
        .get(PROP_DETECTION_CONFIG_IDS));

    return alertIds.stream()
        .map(SubscriptionGroupFilter::fromId)
        .map(alert -> new AlertAssociationDto().setAlert(alert))
        .collect(Collectors.toList());
  }

  private boolean isAlertActive(final long alertId) {
    final AlertDTO alert = alertManager.findById(alertId);
    return alert != null && alert.isActive();
  }

  private Set<AnomalyDTO> findAnomaliesForAlertAssociation(
      final AlertAssociationDto aa,
      final Map<Long, Long> vectorClocks,
      final long endTime) {
    final long alertId = aa.getAlert().getId();
    final long startTime = findStartTime(vectorClocks, endTime, alertId);

    final AnomalyFilter anomalyFilter = new AnomalyFilter()
        .setCreateTimeWindow(new Interval(startTime + 1, endTime))
        .setAlertId(alertId);

    optional(aa.getEnumerationItem())
        .map(AbstractDTO::getId)
        .ifPresent(anomalyFilter::setEnumerationItemId);

    final Collection<AnomalyDTO> candidates = anomalyManager.filter(anomalyFilter);

    return candidates.stream()
        .filter(anomaly -> shouldFilter(anomaly, startTime))
        .collect(toSet());
  }
}
