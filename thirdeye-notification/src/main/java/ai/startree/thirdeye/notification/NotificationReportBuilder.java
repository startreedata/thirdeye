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

import static ai.startree.thirdeye.notification.AnomalyReportHelper.getFeedbackValue;
import static ai.startree.thirdeye.notification.AnomalyReportHelper.getTimezoneString;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.config.TimeConfiguration;
import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.notification.anomalyfilter.DummyAnomalyFilter;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.AnomalyReportDataApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This email formatter lists the anomalies by their functions or metric.
 */
@Singleton
public class NotificationReportBuilder {

  public static final String ANOMALY_VIEW_PREFIX = "anomalies/";
  private static final Logger LOG = LoggerFactory.getLogger(NotificationReportBuilder.class);
  private static final boolean INCLUDE_SUMMARY = false;

  private final AlertManager alertManager;
  private final UiConfiguration uiConfiguration;
  private final EnumerationItemManager enumerationItemManager;

  private final DateTimeFormatter dateTimeFormatter;

  @Inject
  public NotificationReportBuilder(final AlertManager alertManager,
      final UiConfiguration uiConfiguration,
      final EnumerationItemManager enumerationItemManager,
      final TimeConfiguration timeConfiguration) {
    this.alertManager = alertManager;
    this.uiConfiguration = uiConfiguration;
    this.enumerationItemManager = enumerationItemManager;

    dateTimeFormatter = DateTimeFormat.forPattern(timeConfiguration.getDateTimePattern())
        .withZone(timeConfiguration.getTimezone());
  }

  public NotificationReportApi buildNotificationReportApi(
      final SubscriptionGroupDTO notificationConfig,
      final Collection<AnomalyDTO> anomalies) {

    final List<AnomalyDTO> mergedAnomalyResults = new ArrayList<>();

    // Calculate start and end time of the anomalies
    long startTime = System.currentTimeMillis();
    long endTime = 0L;
    for (final AnomalyDTO anomaly : anomalies) {
      mergedAnomalyResults.add(anomaly);
      if (anomaly.getStartTime() < startTime) {
        startTime = anomaly.getStartTime();
      }
      if (anomaly.getEndTime() > endTime) {
        endTime = anomaly.getEndTime();
      }
    }

    final PrecisionRecallEvaluator precisionRecallEvaluator = new PrecisionRecallEvaluator(
        mergedAnomalyResults,
        new DummyAnomalyFilter());

    final NotificationReportApi report = new NotificationReportApi()
        .setStartTime(dateTimeFormatter.print(startTime))
        .setEndTime(dateTimeFormatter.print(endTime))
        .setTimeZone(getTimezoneString(dateTimeFormatter))
        .setNotifiedCount(precisionRecallEvaluator.getTotalAlerts())
        .setFeedbackCount(precisionRecallEvaluator.getTotalResponses())
        .setTrueAlertCount(precisionRecallEvaluator.getTrueAnomalies())
        .setFalseAlertCount(precisionRecallEvaluator.getFalseAlarm())
        .setNewTrendCount(precisionRecallEvaluator.getTrueAnomalyNewTrend())
        .setAlertConfigName(notificationConfig.getName())
        .setIncludeSummary(INCLUDE_SUMMARY)
        .setReportGenerationTimeMillis(System.currentTimeMillis());

    if (precisionRecallEvaluator.getTotalResponses() > 0) {
      report.setPrecision(precisionRecallEvaluator.getPrecisionInResponse());
      report.setRecall(precisionRecallEvaluator.getRecall());
      report.setFalseNegative(precisionRecallEvaluator.getFalseNegativeRate());
    }
    if (notificationConfig.getRefLinks() != null) {
      report.setReferenceLinks(notificationConfig.getRefLinks());
    }
    report.setDashboardHost(uiConfiguration.getExternalUrl());

    return report;
  }

  public List<AnomalyReportApi> toSortedAnomalyReports(
      final Set<AnomalyDTO> anomalies) {
    requireNonNull(anomalies, "anomalies is null");
    if (anomalies.isEmpty()) {
      return List.of();
    }

    final List<AnomalyDTO> sortedAnomalyResults = new ArrayList<>(anomalies);
    sortedAnomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    return sortedAnomalyResults.stream()
        .map(this::toAnomalyReportApi)
        .collect(Collectors.toList());
  }

  private AnomalyReportApi toAnomalyReportApi(final AnomalyDTO anomaly) {
    return new AnomalyReportApi()
        .setAnomaly(toAnomalyApi(anomaly))
        .setData(toAnomalyReportDataApi(anomaly))
        .setUrl(getDashboardUrl(anomaly.getId()));
  }

  private AnomalyReportDataApi toAnomalyReportDataApi(final AnomalyDTO anomaly) {
    final AnomalyFeedback feedback = anomaly.getFeedback();
    final String feedbackVal = getFeedbackValue(feedback);

    String alertName = "Alerts";
    String alertDescription = "";

    if (anomaly.getDetectionConfigId() != null) {
      final AlertDTO alert = alertManager.findById(anomaly.getDetectionConfigId());
      Preconditions.checkNotNull(alert,
          "Cannot find detection config %d", anomaly.getDetectionConfigId());
      alertName = alert.getName();
      alertDescription = alert.getDescription() == null ? "" : alert.getDescription();
    }

    return AnomalyReportHelper.buildAnomalyReportEntity(
        anomaly,
        feedbackVal,
        alertName,
        alertDescription,
        dateTimeFormatter,
        uiConfiguration.getExternalUrl());
  }

  private AnomalyApi toAnomalyApi(final AnomalyDTO anomaly) {
    final AnomalyApi anomalyApi = ApiBeanMapper.toApi(anomaly);

    optional(anomaly.getEnumerationItem())
        .map(EnumerationItemDTO::getId)
        .map(enumerationItemManager::findById)
        .ifPresent(dto -> anomalyApi.setEnumerationItem(new EnumerationItemApi()
            .setId(dto.getId())
            .setName(dto.getName())
        ));

    return anomalyApi;
  }

  private String getDashboardUrl(final Long id) {
    String extUrl = uiConfiguration.getExternalUrl();
    if (!extUrl.matches(".*/")) {
      extUrl += "/";
    }
    return String.format("%s%s%s", extUrl, ANOMALY_VIEW_PREFIX, id);
  }
}
