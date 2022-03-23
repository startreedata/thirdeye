/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.filter;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterNotification;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalySeverity;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.annotation.AlertFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The detection alert filter that can send notifications through multiple channels
 * to a set of unconditional and another set of conditional recipients, based on the
 * value of a specified anomaly severities.
 *
 * You can configure anomaly severities along with a variety of alerting
 * channels and reference links.
 *
 * This alert pipeline have the capability of re-notify anomalies if the anomaly's severity is
 * changed after it's created.
 *
 * <pre>
 * severityRecipients:
 *   - severity:
 *       - LOW
 *     notify:
 *       jiraScheme:
 *         project: PROJECT
 *         assignee: oncall
 *       emailScheme:
 *         recipients:
 *           to:
 *           - "oncall@comany.com"
 *   - severity:
 *       - HIGH
 *       - CRITICAL
 *     notify:
 *       jiraScheme:
 *         project: PROJECT
 *         assignee: manager
 * </pre>
 */
@AlertFilter(type = "SEVERITY_ALERTER_PIPELINE")
public class AnomalySeverityAlertFilter extends StatefulDetectionAlertFilter {

  public static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  public static final String PROP_SEVERITY = "severity";
  public static final String PROP_NOTIFY = "notify";
  public static final String PROP_REF_LINKS = "referenceLinks";
  public static final String PROP_SEVERITY_RECIPIENTS = "severityRecipients";

  private final List<Map<String, Object>> severityRecipients;
  private final List<Long> detectionConfigIds;

  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationDAO;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  public AnomalySeverityAlertFilter(DataProvider provider, SubscriptionGroupDTO config,
      long endTime,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(provider, config, endTime, mergedAnomalyResultManager,
        detectionConfigManager);
    this.severityRecipients = ConfigUtils
        .getList(this.config.getProperties().get(PROP_SEVERITY_RECIPIENTS));
    this.detectionConfigIds = ConfigUtils
        .getLongs(this.config.getProperties().get(PROP_DETECTION_CONFIG_IDS));
    this.anomalySubscriptionGroupNotificationDAO =
        anomalySubscriptionGroupNotificationManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  @Override
  public DetectionAlertFilterResult run() {
    DetectionAlertFilterResult result = new DetectionAlertFilterResult();

    // retrieve the anomalies based on vector clocks
    Set<MergedAnomalyResultDTO> anomalies = this
        .filter(this.makeVectorClocks(this.detectionConfigIds));
    // find the anomalies that needs re-notifying.
    anomalies.addAll(this.retrieveRenotifyAnomalies(this.detectionConfigIds));
    // Prepare mapping from severity-recipients to anomalies
    for (Map<String, Object> severityRecipient : this.severityRecipients) {
      List<AnomalySeverity> severities = ConfigUtils.getList(severityRecipient.get(PROP_SEVERITY))
          .stream()
          .map(s -> AnomalySeverity.valueOf((String) s))
          .collect(Collectors.toList());
      Set<MergedAnomalyResultDTO> notifyAnomalies = new HashSet<>();
      for (MergedAnomalyResultDTO anomaly : anomalies) {
        if (severities.contains(anomaly.getSeverityLabel())) {
          notifyAnomalies.add(anomaly);
        }
      }
      System.out.println(ConfigUtils.getMap(severityRecipient.get(PROP_NOTIFY)));

      // TODO pass proper argument when SubscriptionGroupDto-> properties is refactored
      if (!notifyAnomalies.isEmpty()) {
        SubscriptionGroupDTO subsConfig = SubscriptionUtils.makeChildSubscriptionConfig(config,
            new NotificationSchemesDto(),
            ConfigUtils.getMap(severityRecipient.get(PROP_REF_LINKS)));
        result.addMapping(new DetectionAlertFilterNotification(subsConfig), notifyAnomalies);
      }
    }

    // Notify the remaining anomalies to default recipients
    Set<MergedAnomalyResultDTO> allNotifiedAnomalies = new HashSet<>(result.getAllAnomalies());
    Set<MergedAnomalyResultDTO> defaultAnomalies = new HashSet<>();
    for (MergedAnomalyResultDTO anomaly : anomalies) {
      if (!allNotifiedAnomalies.contains(anomaly)) {
        defaultAnomalies.add(anomaly);
      }
    }
    if (!defaultAnomalies.isEmpty()) {
      result.addMapping(new DetectionAlertFilterNotification(config), defaultAnomalies);
    }

    return result;
  }

  protected Collection<MergedAnomalyResultDTO> retrieveRenotifyAnomalies(
      Collection<Long> detectionConfigIds) {
    // find if any notification is needed
    List<AnomalySubscriptionGroupNotificationDTO> anomalySubscriptionGroupNotificationDTOs =
        this.anomalySubscriptionGroupNotificationDAO.findByPredicate(
            Predicate.IN("detectionConfigId", detectionConfigIds.toArray()));

    List<Long> anomalyIds = new ArrayList<>();
    for (AnomalySubscriptionGroupNotificationDTO anomalySubscriptionGroupNotification : anomalySubscriptionGroupNotificationDTOs) {
      // notify the anomalies if this subscription group have not sent out this anomaly yet
      if (!anomalySubscriptionGroupNotification.getNotifiedSubscriptionGroupIds()
          .contains(this.config.getId())) {
        anomalyIds.add(anomalySubscriptionGroupNotification.getAnomalyId());
        // add this subscription group to the notification record and update
        anomalySubscriptionGroupNotification.getNotifiedSubscriptionGroupIds()
            .add(this.config.getId());
        this.anomalySubscriptionGroupNotificationDAO.save(anomalySubscriptionGroupNotification);
      }
    }
    return anomalyIds.isEmpty() ? Collections.emptyList()
        : mergedAnomalyResultManager.findByIds(anomalyIds);
  }
}
