/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.content;

import static ai.startree.thirdeye.notification.content.NotificationContentUtils.getDateString;
import static ai.startree.thirdeye.notification.content.NotificationContentUtils.getTimezoneString;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detection.detector.email.filter.DummyAlertFilter;
import ai.startree.thirdeye.detection.detector.email.filter.PrecisionRecallEvaluator;
import ai.startree.thirdeye.events.EventFilter;
import ai.startree.thirdeye.events.HolidayEventProvider;
import ai.startree.thirdeye.notification.NotificationContext;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import ai.startree.thirdeye.spi.detection.events.EventType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
 * This class (helper) defines the overall alert message content. This will
 * be derived to implement various anomaly alerting templates.
 */
public abstract class BaseNotificationContent implements NotificationContent {

  private static final Logger LOG = LoggerFactory.getLogger(BaseNotificationContent.class);
  protected final MetricConfigManager metricConfigManager;
  private final EventManager eventManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  protected boolean includeSentAnomaliesOnly;
  protected DateTimeZone dateTimeZone;
  protected boolean includeSummary;
  protected Period preEventCrawlOffset;
  protected Period postEventCrawlOffset;
  protected String imgPath = null;
  protected NotificationContext context;
  protected Properties properties;

  protected BaseNotificationContent(final MetricConfigManager metricConfigManager,
      final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.metricConfigManager = metricConfigManager;
    this.eventManager = eventManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  @Override
  public void init(NotificationContext context) {
    this.context = context;
    this.properties = context.getProperties();

    this.includeSentAnomaliesOnly = Boolean.parseBoolean(
        properties.getProperty(INCLUDE_SENT_ANOMALY_ONLY, DEFAULT_INCLUDE_SENT_ANOMALY_ONLY));
    this.includeSummary = Boolean.parseBoolean(
        properties.getProperty(INCLUDE_SUMMARY, DEFAULT_INCLUDE_SUMMARY));
    this.dateTimeZone = DateTimeZone.forID(properties.getProperty(TIME_ZONE, DEFAULT_TIME_ZONE));

    Period defaultPeriod = Period
        .parse(properties.getProperty(EVENT_CRAWL_OFFSET, DEFAULT_EVENT_CRAWL_OFFSET));
    this.preEventCrawlOffset = defaultPeriod;
    this.postEventCrawlOffset = defaultPeriod;
    if (properties.getProperty(PRE_EVENT_CRAWL_OFFSET) != null) {
      this.preEventCrawlOffset = Period.parse(properties.getProperty(PRE_EVENT_CRAWL_OFFSET));
    }
    if (properties.getProperty(POST_EVENT_CRAWL_OFFSET) != null) {
      this.postEventCrawlOffset = Period.parse(properties.getProperty(POST_EVENT_CRAWL_OFFSET));
    }
  }

  public String getSnaphotPath() {
    return imgPath;
  }

  public void cleanup() {
    if (StringUtils.isNotBlank(imgPath)) {
      try {
        Files.deleteIfExists(new File(imgPath).toPath());
      } catch (IOException e) {
        LOG.error("Exception in deleting screenshot {}", imgPath, e);
      }
    }
  }

  protected void enrichMetricInfo(Map<String, Object> templateData,
      Collection<AnomalyResult> anomalies) {
    Set<String> metrics = new TreeSet<>();
    Set<String> datasets = new TreeSet<>();

    Map<String, MetricConfigDTO> metricsMap = new TreeMap<>();
    for (AnomalyResult anomalyResult : anomalies) {
      if (anomalyResult instanceof MergedAnomalyResultDTO) {
        MergedAnomalyResultDTO mergedAnomaly = (MergedAnomalyResultDTO) anomalyResult;

        optional(mergedAnomaly.getCollection()).ifPresent(datasets::add);

        final String metricName = mergedAnomaly.getMetric();
        if (metricName != null) {
          metrics.add(metricName);
          MetricConfigDTO metric = this.metricConfigManager
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

  protected Map<String, Object> getTemplateData(SubscriptionGroupDTO notificationConfig,
      Collection<AnomalyResult> anomalies) {
    Map<String, Object> templateData = new HashMap<>();

    List<MergedAnomalyResultDTO> mergedAnomalyResults = new ArrayList<>();

    // Calculate start and end time of the anomalies
    DateTime startTime = DateTime.now();
    DateTime endTime = new DateTime(0L);
    for (AnomalyResult anomalyResult : anomalies) {
      if (anomalyResult instanceof MergedAnomalyResultDTO) {
        MergedAnomalyResultDTO mergedAnomaly = (MergedAnomalyResultDTO) anomalyResult;
        mergedAnomalyResults.add(mergedAnomaly);
      }
      if (anomalyResult.getStartTime() < startTime.getMillis()) {
        startTime = new DateTime(anomalyResult.getStartTime(), dateTimeZone);
      }
      if (anomalyResult.getEndTime() > endTime.getMillis()) {
        endTime = new DateTime(anomalyResult.getEndTime(), dateTimeZone);
      }
    }

    PrecisionRecallEvaluator precisionRecallEvaluator = new PrecisionRecallEvaluator(
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
    templateData.put("includeSummary", includeSummary);
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
  protected List<EventDTO> getHolidayEvents(DateTime start, DateTime end,
      Map<String, List<String>> targetDimensions) {
    EventFilter eventFilter = new EventFilter();
    eventFilter.setEventType(EventType.HOLIDAY.name());
    eventFilter.setStartTime(start.minus(preEventCrawlOffset).getMillis());
    eventFilter.setEndTime(end.plus(postEventCrawlOffset).getMillis());
    eventFilter.setTargetDimensionMap(targetDimensions);

    LOG.info("Fetching holidays with preEventCrawlOffset {} and postEventCrawlOffset {}",
        preEventCrawlOffset, postEventCrawlOffset);
    return new HolidayEventProvider(eventManager).getEvents(eventFilter);
  }
}
