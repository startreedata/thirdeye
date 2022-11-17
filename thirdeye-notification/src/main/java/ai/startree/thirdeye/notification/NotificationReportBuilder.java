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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.notification.AnomalyReportHelper.getDateString;
import static ai.startree.thirdeye.notification.AnomalyReportHelper.getFeedbackValue;
import static ai.startree.thirdeye.notification.AnomalyReportHelper.getTimezoneString;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
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
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final EnumerationItemManager enumerationItemManager;

  private final DateTimeZone dateTimeZone;

  @Inject
  public NotificationReportBuilder(final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager,
      final UiConfiguration uiConfiguration,
      final EnumerationItemManager enumerationItemManager,
      final TimeConfiguration timeConfiguration) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
    this.uiConfiguration = uiConfiguration;
    this.enumerationItemManager = enumerationItemManager;

    dateTimeZone = timeConfiguration.getTimezone();
  }

  public NotificationReportApi buildNotificationReportApi(
      final SubscriptionGroupDTO notificationConfig,
      final Collection<? extends AnomalyResult> anomalies) {

    final List<MergedAnomalyResultDTO> mergedAnomalyResults = new ArrayList<>();

    // Calculate start and end time of the anomalies
    DateTime startTime = DateTime.now(dateTimeZone);
    DateTime endTime = new DateTime(0L, dateTimeZone);
    for (final AnomalyResult anomalyResult : anomalies) {
      if (anomalyResult instanceof MergedAnomalyResultDTO) {
        final MergedAnomalyResultDTO mergedAnomaly = (MergedAnomalyResultDTO) anomalyResult;
        mergedAnomalyResults.add(mergedAnomaly);
      }
      if (anomalyResult.getStartTime() < startTime.getMillis()) {
        startTime = new DateTime(anomalyResult.getStartTime(), dateTimeZone);
      }
      if (anomalyResult.getEndTime() > endTime.getMillis()) {
        endTime = new DateTime(anomalyResult.getEndTime(), dateTimeZone);
      }
    }

    final PrecisionRecallEvaluator precisionRecallEvaluator = new PrecisionRecallEvaluator(
        mergedAnomalyResults,
        new DummyAnomalyFilter(),
        mergedAnomalyResultManager);

    final NotificationReportApi report = new NotificationReportApi()
        .setStartTime(getDateString(startTime))
        .setEndTime(getDateString(endTime))
        .setTimeZone(getTimezoneString(dateTimeZone))
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

  public List<AnomalyReportApi> buildAnomalyReports(
      final Set<MergedAnomalyResultDTO> anomalies) {
    requireNonNull(anomalies, "anomalies is null");
    checkArgument(anomalies.size() > 0, "anomalies is empty");

    final List<MergedAnomalyResultDTO> sortedAnomalyResults = new ArrayList<>(anomalies);
    sortedAnomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    return sortedAnomalyResults.stream()
        .map(this::toAnomalyReportApi)
        .collect(Collectors.toList());
  }

  private AnomalyReportApi toAnomalyReportApi(final MergedAnomalyResultDTO anomaly) {
    return new AnomalyReportApi()
        .setAnomaly(toAnomalyApi(anomaly))
        .setData(toAnomalyReportDataApi(anomaly))
        .setUrl(getDashboardUrl(anomaly.getId()));
  }

  private AnomalyReportDataApi toAnomalyReportDataApi(final MergedAnomalyResultDTO anomaly) {
    final AnomalyFeedback feedback = anomaly.getFeedback();
    final String feedbackVal = getFeedbackValue(feedback);

    String alertName = "Alerts";
    String alertDescription = "";

    if (anomaly.getDetectionConfigId() != null) {
      final AlertDTO alert = alertManager.findById(anomaly.getDetectionConfigId());
      Preconditions.checkNotNull(alert,
          String.format("Cannot find detection config %d", anomaly.getDetectionConfigId()));
      alertName = alert.getName();
      alertDescription = alert.getDescription() == null ? "" : alert.getDescription();
    }

    return AnomalyReportHelper.buildAnomalyReportEntity(
        anomaly,
        feedbackVal,
        alertName,
        alertDescription,
        dateTimeZone,
        uiConfiguration.getExternalUrl());
  }

  private AnomalyApi toAnomalyApi(final MergedAnomalyResultDTO anomaly) {
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
