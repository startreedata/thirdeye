/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.content;

import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.Constants.CompareMode;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyType;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.Weeks;

public class NotificationContentUtils {

  protected static String getDateString(DateTime dateTime) {
    return dateTime.toString(NotificationContent.DEFAULT_DATE_PATTERN);
  }

  public static String getDateString(long millis, DateTimeZone dateTimeZone) {
    return (new DateTime(millis, dateTimeZone)).toString(NotificationContent.DEFAULT_DATE_PATTERN);
  }

  public static double getLift(double current, double expected) {
    if (expected == 0) {
      return 1d;
    } else {
      return current / expected - 1;
    }
  }

  /**
   * Get the sign of the severity change
   */
  public static boolean getLiftDirection(double lift) {
    return !(lift < 0);
  }

  /**
   * Convert the duration into hours, represented in String
   */
  public static String getTimeDiffInHours(long start, long end) {
    double duration = (double) ((end - start) / 1000) / 3600;
    return ThirdEyeUtils.getRoundedValue(duration) + ((duration == 1) ? (" hour") : (" hours"));
  }

  /**
   * Flatten the dimension map
   */
  public static List<String> getDimensionsList(Multimap<String, String> dimensions) {
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
  public static String getAnomalyURL(MergedAnomalyResultDTO anomalyResultDTO,
      String dashboardUrl) {
    return dashboardUrl + "/anomalies/view/id/";
  }

  /**
   * Retrieve the issue type of an anomaly
   */
  public static String getIssueType(MergedAnomalyResultDTO anomalyResultDTO) {
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
  public static String getFormattedLiftValue(MergedAnomalyResultDTO anomaly, double lift) {
    String liftValue = String.format(NotificationContent.PERCENTAGE_FORMAT, lift * 100);

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
  public static String getPredictedValue(MergedAnomalyResultDTO anomaly) {
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
  public static String getCurrentValue(MergedAnomalyResultDTO anomaly) {
    String current = ThirdEyeUtils.getRoundedValue(anomaly.getAvgCurrentVal());

    if (current.equalsIgnoreCase(String.valueOf(Double.NaN))) {
      current = "-";
    }
    return current;
  }

  /**
   * Convert Feedback value to user readable values
   */
  public static String getFeedbackValue(AnomalyFeedback feedback) {
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
  public static Period getBaselinePeriod(CompareMode compareMode) {
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

  /**
   * Get the timezone in String
   */
  public static String getTimezoneString(DateTimeZone dateTimeZone) {
    TimeZone tz = TimeZone.getTimeZone(dateTimeZone.getID());
    return tz.getDisplayName(true, 0);
  }

  /**
   * Get the value of matched filter key of given anomaly result
   *
   * @param anomaly a MergedAnomalyResultDTO instance
   * @param matchText a text to be matched in the filter keys
   * @return a list of filter values
   */
  public static List<String> getMatchedFilterValues(MergedAnomalyResultDTO anomaly,
      String matchText) {
    Multimap<String, String> filterSet = AnomalyUtils.generateFilterSetForTimeSeriesQuery(anomaly);
    for (String filterKey : filterSet.keySet()) {
      if (filterKey.contains(matchText)) {
        return new ArrayList<>(filterSet.get(filterKey));
      }
    }
    return Collections.emptyList();
  }
}
