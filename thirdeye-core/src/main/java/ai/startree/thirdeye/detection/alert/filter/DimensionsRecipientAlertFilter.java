/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.filter;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterNotification;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.annotation.AlertFilter;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The detection alert filter that can send notifications through multiple channels
 * to a set of unconditional and another set of conditional recipients, based on the
 * value of a specified anomaly dimension combinations.
 *
 * You can configure multiple dimension combinations along with a variety of alerting
 * channels and reference links.
 *
 * <pre>
 * dimensionRecipients:
 *   - dimensions:
 *       country: IN
 *       device: Android
 *     notify:
 *       jiraScheme:
 *         project: ANDROID
 *         assignee: android-oncall
 *       emailScheme:
 *         recipients:
 *           to:
 *           - "android-team@comany.com"
 *   - dimension:
 *       country: US
 *       device: IOS
 *     notify:
 *       jiraScheme:
 *         project: IOS
 *         assignee: ios-oncall
 * </pre>
 */
@AlertFilter(type = "DIMENSIONS_ALERTER_PIPELINE")
public class DimensionsRecipientAlertFilter extends StatefulDetectionAlertFilter {

  public static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  public static final String PROP_DIMENSION = "dimensions";
  public static final String PROP_NOTIFY = "notify";
  public static final String PROP_REF_LINKS = "referenceLinks";
  public static final String PROP_DIMENSION_RECIPIENTS = "dimensionRecipients";

  final List<Map<String, Object>> dimensionRecipients;
  final List<Long> detectionConfigIds;

  public DimensionsRecipientAlertFilter(DataProvider provider, SubscriptionGroupDTO config,
      long endTime, final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(provider, config, endTime, mergedAnomalyResultManager,
        detectionConfigManager);
    this.dimensionRecipients = ConfigUtils
        .getList(this.config.getProperties().get(PROP_DIMENSION_RECIPIENTS));
    this.detectionConfigIds = ConfigUtils
        .getLongs(this.config.getProperties().get(PROP_DETECTION_CONFIG_IDS));
  }

  @Override
  public DetectionAlertFilterResult run() {
    DetectionAlertFilterResult result = new DetectionAlertFilterResult();

    Set<MergedAnomalyResultDTO> anomalies = this
        .filter(this.makeVectorClocks(this.detectionConfigIds));

    // Prepare mapping from dimension-recipients to anomalies
    for (Map<String, Object> dimensionRecipient : this.dimensionRecipients) {
      Multimap<String, String> dimensionFilters = ConfigUtils
          .getMultimap(dimensionRecipient.get(PROP_DIMENSION));
      Set<MergedAnomalyResultDTO> notifyAnomalies = new HashSet<>();
      for (MergedAnomalyResultDTO anomaly : anomalies) {
        Multimap<String, String> anomalousDims = MetricEntity.fromURN(anomaly.getMetricUrn())
            .getFilters();
        if (anomalousDims.entries().containsAll(dimensionFilters.entries())) {
          notifyAnomalies.add(anomaly);
        }
      }

      if (!notifyAnomalies.isEmpty()) {
        // TODO remove manual mapping when SubscriptionGroupDto->properties is refactored
        System.out.println(ConfigUtils.getMap(dimensionRecipient.get(PROP_NOTIFY)));
        Map<String, List<String>> emailMap = ConfigUtils.getMap(
            ConfigUtils.getMap(
              ConfigUtils.getMap(
                dimensionRecipient.get(PROP_NOTIFY))
              .get("emailScheme"))
            .get("recipients"));
        NotificationSchemesDto notificationSchemes = new NotificationSchemesDto()
            .setEmailScheme(new EmailSchemeDto()
            .setTo(emailMap.get("to"))
            .setCc(emailMap.get("cc"))
            .setBcc(emailMap.get("bcc")));
        SubscriptionGroupDTO subsConfig = SubscriptionUtils.makeChildSubscriptionConfig(config,
            notificationSchemes,
            ConfigUtils.getMap(dimensionRecipient.get(PROP_REF_LINKS)));
        result.addMapping(
            new DetectionAlertFilterNotification(subsConfig, dimensionFilters),
            notifyAnomalies);
      }
    }

    // Notify the remaining anomalies to default recipients
    Set<MergedAnomalyResultDTO> notifiedAnomalies = new HashSet<>(result.getAllAnomalies());
    Set<MergedAnomalyResultDTO> defaultAnomalies = new HashSet<>();
    for (MergedAnomalyResultDTO anomaly : anomalies) {
      if (!notifiedAnomalies.contains(anomaly)) {
        defaultAnomalies.add(anomaly);
      }
    }
    if (!defaultAnomalies.isEmpty()) {
      result.addMapping(new DetectionAlertFilterNotification(config), defaultAnomalies);
    }

    return result;
  }
}
