/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.detection.anomaly.alert.util.AlertScreenshotHelper.takeGraphScreenShot;
import static ai.startree.thirdeye.notification.AnomalyReportEntityBuilder.getDateString;
import static ai.startree.thirdeye.notification.AnomalyReportEntityBuilder.getFeedbackValue;
import static ai.startree.thirdeye.notification.AnomalyReportEntityBuilder.getTimezoneString;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.detection.detector.email.filter.DummyAlertFilter;
import ai.startree.thirdeye.detection.detector.email.filter.PrecisionRecallEvaluator;
import ai.startree.thirdeye.events.EventFilter;
import ai.startree.thirdeye.events.HolidayEventProvider;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import ai.startree.thirdeye.spi.detection.events.EventType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This email formatter lists the anomalies by their functions or metric.
 */
@Singleton
public class AnomalyEmailContentBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyEmailContentBuilder.class);
  private static final boolean INCLUDE_SUMMARY = false;

  private final MetricConfigManager metricConfigManager;
  private final AlertManager alertManager;
  private final UiConfiguration uiConfiguration;
  private final EventManager eventManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  private final DateTimeZone dateTimeZone;
  private final Period preEventCrawlOffset;
  private final Period postEventCrawlOffset;
  private String imgPath = null;

  @Inject
  public AnomalyEmailContentBuilder(final MetricConfigManager metricConfigManager,
      final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager,
      final UiConfiguration uiConfiguration) {
    this.metricConfigManager = metricConfigManager;
    this.eventManager = eventManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
    this.uiConfiguration = uiConfiguration;

    dateTimeZone = DateTimeZone.forID(Constants.DEFAULT_TIMEZONE);

    final Period defaultPeriod = Period.parse(Constants.NOTIFICATIONS_DEFAULT_EVENT_CRAWL_OFFSET);
    preEventCrawlOffset = defaultPeriod;
    postEventCrawlOffset = defaultPeriod;
  }

  public void cleanup() {
    if (StringUtils.isNotBlank(imgPath)) {
      try {
        Files.deleteIfExists(new File(imgPath).toPath());
      } catch (final IOException e) {
        LOG.error("Exception in deleting screenshot {}", imgPath, e);
      }
    }
  }

  private Map<String, MetricConfigDTO> buildMetricsMap(
      final Collection<AnomalyResult> anomalies) {
    final Map<String, MetricConfigDTO> metricsMap = new TreeMap<>();
    for (final AnomalyResult anomalyResult : anomalies) {
      if (anomalyResult instanceof MergedAnomalyResultDTO) {
        final MergedAnomalyResultDTO mergedAnomaly = (MergedAnomalyResultDTO) anomalyResult;

        final String metricName = mergedAnomaly.getMetric();
        if (metricName != null) {
          final MetricConfigDTO metric = metricConfigManager
              .findByMetricAndDataset(metricName, mergedAnomaly.getCollection());
          if (metric != null) {
            metricsMap.put(metric.getId().toString(), metric);
          }
        }
      }
    }
    return metricsMap;
  }

  public NotificationReportApi buildNotificationReportApi(
      final SubscriptionGroupDTO notificationConfig,
      final Collection<? extends AnomalyResult> anomalies) {

    final List<MergedAnomalyResultDTO> mergedAnomalyResults = new ArrayList<>();

    // Calculate start and end time of the anomalies
    DateTime startTime = DateTime.now();
    DateTime endTime = new DateTime(0L);
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
        mergedAnomalyResults, new DummyAlertFilter(),
        mergedAnomalyResultManager);

    final NotificationReportApi report = new NotificationReportApi();

    report.setStartTime(getDateString(startTime));
    report.setEndTime(getDateString(endTime));
    report.setTimeZone(getTimezoneString(dateTimeZone));
    report.setNotifiedCount(precisionRecallEvaluator.getTotalAlerts());
    report.setFeedbackCount(precisionRecallEvaluator.getTotalResponses());
    report.setTrueAlertCount(precisionRecallEvaluator.getTrueAnomalies());
    report.setFalseAlertCount(precisionRecallEvaluator.getFalseAlarm());
    report.setNewTrendCount(precisionRecallEvaluator.getTrueAnomalyNewTrend());
    report.setAlertConfigName(notificationConfig.getName());
    report.setIncludeSummary(INCLUDE_SUMMARY);
    report.setReportGenerationTimeMillis(System.currentTimeMillis());

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

  /**
   * Taking advantage of event data provider, extract the events around the given start and end time
   *
   * @param start the start time of the event, preEventCrawlOffset is added before the given
   *     date time
   * @param end the end time of the event, postEventCrawlOffset is added after the given date
   *     time
   * @param targetDimensions the affected dimensions
   * @return a list of related events
   */
  private List<EventDTO> getHolidayEvents(final DateTime start, final DateTime end,
      final Map<String, List<String>> targetDimensions) {
    final EventFilter eventFilter = new EventFilter();
    eventFilter.setEventType(EventType.HOLIDAY.name());
    eventFilter.setStartTime(start.minus(preEventCrawlOffset).getMillis());
    eventFilter.setEndTime(end.plus(postEventCrawlOffset).getMillis());
    eventFilter.setTargetDimensionMap(targetDimensions);

    LOG.info("Fetching holidays with preEventCrawlOffset {} and postEventCrawlOffset {}",
        preEventCrawlOffset, postEventCrawlOffset);
    return new HolidayEventProvider(eventManager).getEvents(eventFilter);
  }

  public String getTemplate() {
    return "metric-anomalies";
  }

  public List<EventApi> getRelatedEvents(final Collection<? extends AnomalyResult> anomalies) {
    DateTime windowStart = DateTime.now();
    DateTime windowEnd = new DateTime(0);

    for (final AnomalyResult anomalyResult : anomalies) {
      if (!(anomalyResult instanceof MergedAnomalyResultDTO)) {
        LOG.warn("Anomaly result {} isn't an instance of MergedAnomalyResultDTO. Skip from alert.",
            anomalyResult);
        continue;
      }
      final MergedAnomalyResultDTO anomaly = (MergedAnomalyResultDTO) anomalyResult;

      final DateTime anomalyStartTime = new DateTime(anomaly.getStartTime(), dateTimeZone);
      final DateTime anomalyEndTime = new DateTime(anomaly.getEndTime(), dateTimeZone);

      if (anomalyStartTime.isBefore(windowStart)) {
        windowStart = anomalyStartTime;
      }
      if (anomalyEndTime.isAfter(windowEnd)) {
        windowEnd = anomalyEndTime;
      }
    }

    // holidays
    final DateTime eventStart = windowStart.minus(preEventCrawlOffset);
    final DateTime eventEnd = windowEnd.plus(postEventCrawlOffset);
    final List<EventDTO> holidays = getHolidayEvents(
        eventStart,
        eventEnd,
        new HashMap<>());
    holidays.sort(Comparator.comparingLong(EventDTO::getStartTime));

    return holidays.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public Map<String, Object> format(final Collection<AnomalyResult> anomalies) {
    final Multimap<String, String> anomalyDimensions = ArrayListMultimap.create();
    final Multimap<String, AnomalyReportEntity> alertAnomalyReportsMap = ArrayListMultimap.create();
    final Multimap<String, AnomalyReportEntity> metricAnomalyReportsMap = ArrayListMultimap.create();

    final List<AnomalyResult> sortedAnomalies = new ArrayList<>(anomalies);
    sortedAnomalies.sort(Comparator.comparingDouble(AnomalyResult::getWeight));

    for (final AnomalyResult anomalyResult : anomalies) {
      if (!(anomalyResult instanceof MergedAnomalyResultDTO)) {
        LOG.warn("Anomaly result {} isn't an instance of MergedAnomalyResultDTO. Skip from alert.",
            anomalyResult);
        continue;
      }
      final MergedAnomalyResultDTO anomaly = (MergedAnomalyResultDTO) anomalyResult;
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

      final AnomalyReportEntity anomalyReport = AnomalyReportEntityBuilder.buildAnomalyReportEntity(
          anomaly,
          feedbackVal,
          alertName,
          alertDescription,
          dateTimeZone,
          uiConfiguration.getExternalUrl());

      // dimension filters / values
      for (final Map.Entry<String, String> entry : anomaly.getDimensions().entrySet()) {
        anomalyDimensions.put(entry.getKey(), entry.getValue());
      }

      // include notified alerts only in the email
      alertAnomalyReportsMap.put(alertName, anomalyReport);
      metricAnomalyReportsMap.put(optional(anomaly.getMetric()).orElse("UNKNOWN"), anomalyReport);
    }

    // Insert anomaly snapshot image
//    this.imgPath = buildScreenshot(anomalyReports);

    final Map<String, Object> templateData = new HashMap<>();
    templateData.put("detectionToAnomalyDetailsMap", alertAnomalyReportsMap.asMap());
    templateData.put("metricToAnomalyDetailsMap", metricAnomalyReportsMap.asMap());
    return templateData;
  }

  private String buildScreenshot(final List<AnomalyReportEntity> anomalyDetails) {
    if (anomalyDetails.size() == 1) {
      final AnomalyReportEntity singleAnomaly = anomalyDetails.get(0);
      try {
        return takeGraphScreenShot(
            singleAnomaly.getAnomalyId(),
            uiConfiguration.getExternalUrl());
      } catch (final Exception e) {
        LOG.error("Exception while embedding screenshot for anomaly {}",
            singleAnomaly.getAnomalyId(), e);
      }
    }
    return null;
  }
}
