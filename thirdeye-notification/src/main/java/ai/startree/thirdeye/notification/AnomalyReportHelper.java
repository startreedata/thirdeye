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

import static ai.startree.thirdeye.notification.NotificationReportBuilder.ANOMALY_VIEW_PREFIX;
import static ai.startree.thirdeye.spi.Constants.NOTIFICATIONS_PERCENTAGE_FORMAT;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AnomalyReportDataApi;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class AnomalyReportHelper {

  public static AnomalyReportDataApi buildAnomalyReportEntity(final MergedAnomalyResultDTO anomaly,
      final String feedbackVal,
      final String alertName,
      final String alertDescription,
      final DateTimeZone dateTimeZone,
      final String uiPublicUrl) {
    final Properties props = new Properties();
    props.putAll(anomaly.getProperties());
    final double lift = getLift(anomaly.getAvgCurrentVal(), anomaly.getAvgBaselineVal());
    final String baselineVal = getPredictedValue(anomaly);

    return new AnomalyReportDataApi()
        .setAnomalyId(String.valueOf(anomaly.getId()))
        .setAnomalyURL(getAnomalyURL(uiPublicUrl))
        .setBaselineVal(baselineVal)
        .setCurrentVal(getCurrentValue(anomaly))
        .setLift(baselineVal.equals("-") ? "" : getFormattedLiftValue(anomaly, lift))
        .setPositiveLift(getLiftDirection(lift))
        .setSwi(String.format(NOTIFICATIONS_PERCENTAGE_FORMAT, 0d))
        .setDimensions(getDimensionsList(anomaly.getDimensionMap()))
        .setDuration(getTimeDiffInHours(anomaly.getStartTime(), anomaly.getEndTime())) // duratio
        .setFeedback(feedbackVal)
        .setFunction(alertName)
        .setFuncDescription(alertDescription)
        .setMetric(anomaly.getMetric())
        .setStartDateTime(getDateString(anomaly.getStartTime(), dateTimeZone))
        .setEndTime(getDateString(anomaly.getEndTime(), dateTimeZone))
        .setTimezone(getTimezoneString(dateTimeZone))
        .setIssueType(getIssueType(anomaly))
        .setAnomalyType(anomaly.getType().getLabel())
        .setProperties(SpiUtils.encodeCompactedProperties(props))
        .setMetricUrn(anomaly.getMetricUrn());
  }

  public static String getDateString(DateTime dateTime) {
    return dateTime.toString(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN);
  }

  public static String getDateString(long millis, DateTimeZone dateTimeZone) {
    return (new DateTime(millis,
        dateTimeZone)).toString(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN);
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
  public static String getAnomalyURL(String dashboardUrl) {
    return dashboardUrl + "/" + ANOMALY_VIEW_PREFIX;
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
    String liftValue = String.format(Constants.NOTIFICATIONS_PERCENTAGE_FORMAT, lift * 100);

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
   * Get the timezone in String
   */
  public static String getTimezoneString(DateTimeZone dateTimeZone) {
    TimeZone tz = TimeZone.getTimeZone(dateTimeZone.getID());
    return tz.getDisplayName(true, 0);
  }
}
