/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.content;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.detection.detector.email.filter.DummyAlertFilter;
import ai.startree.thirdeye.detection.detector.email.filter.PrecisionRecallEvaluator;
import ai.startree.thirdeye.events.EventFilter;
import ai.startree.thirdeye.events.HolidayEventProvider;
import ai.startree.thirdeye.notification.NotificationContext;
import ai.startree.thirdeye.spi.Constants.CompareMode;
import ai.startree.thirdeye.spi.Constants.SubjectType;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import ai.startree.thirdeye.spi.detection.AnomalyType;
import ai.startree.thirdeye.spi.detection.events.EventType;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.Weeks;
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

  /**
   * Generate subject based on configuration.
   */
  public static String makeSubject(SubjectType subjectType,
      SubscriptionGroupDTO notificationConfig, Map<String, Object> templateData) {
    String baseSubject = "Thirdeye Alert : " + notificationConfig.getName();

    switch (subjectType) {
      case ALERT:
        return baseSubject;

      case METRICS:
        return baseSubject + " - " + templateData.get("metrics");

      case DATASETS:
        return baseSubject + " - " + templateData.get("datasets");

      default:
        throw new IllegalArgumentException(
            String.format("Unknown type '%s'", notificationConfig.getSubjectType()));
    }
  }

  protected static String getDateString(DateTime dateTime) {
    return dateTime.toString(DEFAULT_DATE_PATTERN);
  }

  protected static String getDateString(long millis, DateTimeZone dateTimeZone) {
    return (new DateTime(millis, dateTimeZone)).toString(DEFAULT_DATE_PATTERN);
  }

  protected static double getLift(double current, double expected) {
    if (expected == 0) {
      return 1d;
    } else {
      return current / expected - 1;
    }
  }

  /**
   * Get the sign of the severity change
   */
  protected static boolean getLiftDirection(double lift) {
    return !(lift < 0);
  }

  /**
   * Convert the duration into hours, represented in String
   */
  protected static String getTimeDiffInHours(long start, long end) {
    double duration = (double) ((end - start) / 1000) / 3600;
    return ThirdEyeUtils.getRoundedValue(duration) + ((duration == 1) ? (" hour") : (" hours"));
  }

  /**
   * Flatten the dimension map
   */
  protected static List<String> getDimensionsList(Multimap<String, String> dimensions) {
    List<String> dimensionsList = new ArrayList<>();
    if (dimensions != null && !dimensions.isEmpty()) {
      for (Map.Entry<String, Collection<String>> entry : dimensions.asMap().entrySet()) {
        dimensionsList.add(entry.getKey() + " : " + String.join(",", entry.getValue()));
      }
    }
    return dimensionsList;
  }

  /**
   * Get the url of given anomaly result
   */
  protected static String getAnomalyURL(MergedAnomalyResultDTO anomalyResultDTO,
      String dashboardUrl) {
    String urlPart = "/anomalies/view/id/";
    return dashboardUrl + urlPart;
  }

  /**
   * Retrieve the issue type of an anomaly
   */
  protected static String getIssueType(MergedAnomalyResultDTO anomalyResultDTO) {
    Map<String, String> properties = anomalyResultDTO.getProperties();
    if (MapUtils.isNotEmpty(properties) && properties
        .containsKey(MergedAnomalyResultDTO.ISSUE_TYPE_KEY)) {
      return properties.get(MergedAnomalyResultDTO.ISSUE_TYPE_KEY);
    }
    return null;
  }

  /**
   * Returns a human readable lift value to be displayed in the notification templates
   */
  protected static String getFormattedLiftValue(MergedAnomalyResultDTO anomaly, double lift) {
    String liftValue = String.format(PERCENTAGE_FORMAT, lift * 100);

    // Fetch the lift value for a SLA anomaly
    if (anomaly.getType().equals(AnomalyType.DATA_SLA)) {
      liftValue = getFormattedSLALiftValue(anomaly);
    }

    return liftValue;
  }

  /**
   * The lift value for an SLA anomaly is delay from the configured sla. (Ex: 2 days & 3 hours)
   */
  protected static String getFormattedSLALiftValue(MergedAnomalyResultDTO anomaly) {
    if (!anomaly.getType().equals(AnomalyType.DATA_SLA)
        || anomaly.getProperties() == null || anomaly.getProperties().isEmpty()
        || !anomaly.getProperties().containsKey("sla")
        || !anomaly.getProperties().containsKey("datasetLastRefreshTime")) {
      return "";
    }

    long delayInMillis = anomaly.getEndTime() - Long
        .parseLong(anomaly.getProperties().get("datasetLastRefreshTime"));
    long days = TimeUnit.MILLISECONDS.toDays(delayInMillis);
    long hours = TimeUnit.MILLISECONDS.toHours(delayInMillis) % TimeUnit.DAYS.toHours(1);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(delayInMillis) % TimeUnit.HOURS.toMinutes(1);

    String liftValue;
    if (days > 0) {
      liftValue = String.format("%d days & %d hours", days, hours);
    } else if (hours > 0) {
      liftValue = String.format("%d hours & %d mins", hours, minutes);
    } else {
      liftValue = String.format("%d mins", minutes);
    }

    return liftValue;
  }

  /**
   * The predicted value for an SLA anomaly is the configured sla. (Ex: 2_DAYS)
   */
  protected static String getSLAPredictedValue(MergedAnomalyResultDTO anomaly) {
    if (!anomaly.getType().equals(AnomalyType.DATA_SLA)
        || anomaly.getProperties() == null || anomaly.getProperties().isEmpty()
        || !anomaly.getProperties().containsKey("sla")) {
      return "-";
    }

    return anomaly.getProperties().get("sla");
  }

  /**
   * Retrieve the predicted value for the anomaly
   */
  protected static String getPredictedValue(MergedAnomalyResultDTO anomaly) {
    String predicted = ThirdEyeUtils.getRoundedValue(anomaly.getAvgBaselineVal());

    // For SLA anomalies, we use the sla as the predicted value
    if (anomaly.getType().equals(AnomalyType.DATA_SLA)) {
      predicted = getSLAPredictedValue(anomaly);
    }

    if (predicted.equalsIgnoreCase(String.valueOf(Double.NaN))) {
      predicted = "-";
    }
    return predicted;
  }

  /**
   * Retrieve the current value for the anomaly
   */
  protected static String getCurrentValue(MergedAnomalyResultDTO anomaly) {
    String current = ThirdEyeUtils.getRoundedValue(anomaly.getAvgCurrentVal());

    if (current.equalsIgnoreCase(String.valueOf(Double.NaN))) {
      current = "-";
    }
    return current;
  }

  /**
   * Convert Feedback value to user readable values
   */
  protected static String getFeedbackValue(AnomalyFeedback feedback) {
    String feedbackVal = "Not Resolved";
    if (feedback != null && feedback.getFeedbackType() != null) {
      switch (feedback.getFeedbackType()) {
        case ANOMALY:
          feedbackVal = "Resolved (Confirmed Anomaly)";
          break;
        case NOT_ANOMALY:
          feedbackVal = "Resolved (False Alarm)";
          break;
        case ANOMALY_NEW_TREND:
          feedbackVal = "Resolved (New Trend)";
          break;
        case NO_FEEDBACK:
        default:
          break;
      }
    }
    return feedbackVal;
  }

  /**
   * Convert comparison mode to Period
   */
  protected static Period getBaselinePeriod(CompareMode compareMode) {
    switch (compareMode) {
      case Wo2W:
        return Weeks.TWO.toPeriod();
      case Wo3W:
        return Weeks.THREE.toPeriod();
      case Wo4W:
        return Weeks.weeks(4).toPeriod();
      case WoW:
      default:
        return Weeks.ONE.toPeriod();
    }
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
   * Get the timezone in String
   */
  protected String getTimezoneString(DateTimeZone dateTimeZone) {
    TimeZone tz = TimeZone.getTimeZone(dateTimeZone.getID());
    return tz.getDisplayName(true, 0);
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

  /**
   * Get the value of matched filter key of given anomaly result
   *
   * @param anomaly a MergedAnomalyResultDTO instance
   * @param matchText a text to be matched in the filter keys
   * @return a list of filter values
   */
  protected List<String> getMatchedFilterValues(MergedAnomalyResultDTO anomaly, String matchText) {
    Multimap<String, String> filterSet = AnomalyUtils.generateFilterSetForTimeSeriesQuery(anomaly);
    for (String filterKey : filterSet.keySet()) {
      if (filterKey.contains(matchText)) {
        return new ArrayList<>(filterSet.get(filterKey));
      }
    }
    return Collections.emptyList();
  }
}
