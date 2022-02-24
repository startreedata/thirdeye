/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.notification.AnomalyReportEntityBuilder.getDateString;
import static ai.startree.thirdeye.notification.AnomalyReportEntityBuilder.getFeedbackValue;
import static ai.startree.thirdeye.notification.AnomalyReportEntityBuilder.getTimezoneString;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.detection.anomaly.alert.util.AlertScreenshotHelper;
import ai.startree.thirdeye.detection.detector.email.filter.DummyAlertFilter;
import ai.startree.thirdeye.detection.detector.email.filter.PrecisionRecallEvaluator;
import ai.startree.thirdeye.events.EventFilter;
import ai.startree.thirdeye.events.HolidayEventProvider;
import ai.startree.thirdeye.spi.Constants;
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
import com.google.common.base.Joiner;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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

  private void enrichMetricInfo(final Map<String, Object> templateData,
      final Collection<AnomalyResult> anomalies) {
    final Set<String> metrics = new TreeSet<>();
    final Set<String> datasets = new TreeSet<>();

    final Map<String, MetricConfigDTO> metricsMap = new TreeMap<>();
    for (final AnomalyResult anomalyResult : anomalies) {
      if (anomalyResult instanceof MergedAnomalyResultDTO) {
        final MergedAnomalyResultDTO mergedAnomaly = (MergedAnomalyResultDTO) anomalyResult;

        optional(mergedAnomaly.getCollection()).ifPresent(datasets::add);

        final String metricName = mergedAnomaly.getMetric();
        if (metricName != null) {
          metrics.add(metricName);
          final MetricConfigDTO metric = metricConfigManager
              .findByMetricAndDataset(metricName, mergedAnomaly.getCollection());
          if (metric != null) {
            metricsMap.put(metric.getId().toString(), metric);
          }
        }
      }
    }

    templateData.put("datasetsCount", datasets.size());
    templateData.put("datasets", StringUtils.join(datasets, ", "));
    templateData.put("metricsCount", metrics.size());
    templateData.put("metrics", StringUtils.join(metrics, ", "));
    templateData.put("metricsMap", metricsMap);
  }

  private Map<String, Object> getTemplateData(final SubscriptionGroupDTO notificationConfig,
      final Collection<AnomalyResult> anomalies) {
    final Map<String, Object> templateData = new HashMap<>();

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

    templateData.put("anomalyCount", anomalies.size());
    templateData.put("startTime", getDateString(startTime));
    templateData.put("endTime", getDateString(endTime));
    templateData.put("timeZone", getTimezoneString(dateTimeZone));
    templateData.put("notifiedCount", precisionRecallEvaluator.getTotalAlerts());
    templateData.put("feedbackCount", precisionRecallEvaluator.getTotalResponses());
    templateData.put("trueAlertCount", precisionRecallEvaluator.getTrueAnomalies());
    templateData.put("falseAlertCount", precisionRecallEvaluator.getFalseAlarm());
    templateData.put("newTrendCount", precisionRecallEvaluator.getTrueAnomalyNewTrend());
    templateData.put("alertConfigName", notificationConfig.getName());
    templateData.put("includeSummary", INCLUDE_SUMMARY);
    templateData.put("reportGenerationTimeMillis", System.currentTimeMillis());
    if (precisionRecallEvaluator.getTotalResponses() > 0) {
      templateData.put("precision", precisionRecallEvaluator.getPrecisionInResponse());
      templateData.put("recall", precisionRecallEvaluator.getRecall());
      templateData.put("falseNegative", precisionRecallEvaluator.getFalseNegativeRate());
    }
    if (notificationConfig.getRefLinks() != null) {
      templateData.put("referenceLinks", notificationConfig.getRefLinks());
    }

    return templateData;
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

  public Map<String, Object> format(final Collection<AnomalyResult> anomalies,
      final SubscriptionGroupDTO subsConfig) {
    final Map<String, Object> templateData = getTemplateData(subsConfig, anomalies);
    enrichMetricInfo(templateData, anomalies);

    DateTime windowStart = DateTime.now();
    DateTime windowEnd = new DateTime(0);

    final Map<String, Long> functionToId = new HashMap<>();
    final Multimap<String, String> anomalyDimensions = ArrayListMultimap.create();
    final Multimap<String, AnomalyReportEntity> functionAnomalyReports = ArrayListMultimap.create();
    final Multimap<String, AnomalyReportEntity> metricAnomalyReports = ArrayListMultimap.create();
    final List<AnomalyReportEntity> anomalyDetails = new ArrayList<>();
    final List<String> anomalyIds = new ArrayList<>();

    final List<AnomalyResult> sortedAnomalies = new ArrayList<>(anomalies);
    sortedAnomalies.sort(Comparator.comparingDouble(AnomalyResult::getWeight));

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

      final AnomalyFeedback feedback = anomaly.getFeedback();

      final String feedbackVal = getFeedbackValue(feedback);

      String functionName = "Alerts";
      String funcDescription = "";
      Long id = -1L;

      if (anomaly.getDetectionConfigId() != null) {
        final AlertDTO config = alertManager.findById(anomaly.getDetectionConfigId());
        Preconditions.checkNotNull(config,
            String.format("Cannot find detection config %d", anomaly.getDetectionConfigId()));
        functionName = config.getName();
        funcDescription = config.getDescription() == null ? "" : config.getDescription();
        id = config.getId();
      }

      final AnomalyReportEntity anomalyReport = AnomalyReportEntityBuilder.buildAnomalyReportEntity(
          anomaly,
          feedbackVal,
          functionName,
          funcDescription,
          dateTimeZone,
          uiConfiguration.getExternalUrl());

      // dimension filters / values
      for (final Map.Entry<String, String> entry : anomaly.getDimensions().entrySet()) {
        anomalyDimensions.put(entry.getKey(), entry.getValue());
      }

      // include notified alerts only in the email
      anomalyDetails.add(anomalyReport);
      anomalyIds.add(anomalyReport.getAnomalyId());
      functionAnomalyReports.put(functionName, anomalyReport);
      metricAnomalyReports.put(optional(anomaly.getMetric()).orElse("UNKNOWN"), anomalyReport);
      functionToId.put(functionName, id);
    }

    // holidays
    final DateTime eventStart = windowStart.minus(preEventCrawlOffset);
    final DateTime eventEnd = windowEnd.plus(postEventCrawlOffset);
    final Map<String, List<String>> targetDimensions = new HashMap<>();

    final List<EventDTO> holidays = getHolidayEvents(eventStart, eventEnd, targetDimensions);
    holidays.sort(Comparator.comparingLong(EventDTO::getStartTime));

    // Insert anomaly snapshot image
    if (anomalyDetails.size() == 1) {
      final AnomalyReportEntity singleAnomaly = anomalyDetails.get(0);
      try {
        imgPath = AlertScreenshotHelper
            .takeGraphScreenShot(singleAnomaly.getAnomalyId(),
                uiConfiguration.getExternalUrl());
      } catch (final Exception e) {
        LOG.error("Exception while embedding screenshot for anomaly {}",
            singleAnomaly.getAnomalyId(), e);
      }
    }

    templateData.put("anomalyDetails", anomalyDetails);
    templateData.put("anomalyIds", Joiner.on(",").join(anomalyIds));
    templateData.put("holidays", holidays);
    templateData.put("detectionToAnomalyDetailsMap", functionAnomalyReports.asMap());
    templateData.put("metricToAnomalyDetailsMap", metricAnomalyReports.asMap());
    templateData.put("functionToId", functionToId);

    return templateData;
  }
}
