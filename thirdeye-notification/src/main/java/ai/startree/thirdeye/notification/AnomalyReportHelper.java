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

import static ai.startree.thirdeye.notification.NotificationReportBuilder.ANOMALY_VIEW_PREFIX;
import static ai.startree.thirdeye.spi.Constants.NOTIFICATIONS_PERCENTAGE_FORMAT;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AnomalyReportDataApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.commons.collections4.MapUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

public class AnomalyReportHelper {

  public static AnomalyReportDataApi buildAnomalyReportEntity(final AnomalyDTO anomaly,
      final String feedbackVal,
      final String alertName,
      final String alertDescription,
      final DateTimeFormatter dateTimeFormatter,
      final String uiPublicUrl) {
    final Properties props = new Properties();
    props.putAll(anomaly.getProperties());
    final double lift = getLift(anomaly.getAvgCurrentVal(), anomaly.getAvgBaselineVal());
    final String baselineVal = roundedNanSafeOf(anomaly.getAvgBaselineVal());

    return new AnomalyReportDataApi()
        .setAnomalyId(String.valueOf(anomaly.getId()))
        .setAnomalyURL(getAnomalyURL(uiPublicUrl))
        .setBaselineVal(baselineVal)
        .setCurrentVal(roundedNanSafeOf(anomaly.getAvgCurrentVal()))
        .setLowerBound(roundedNanSafeOf(anomaly.getLowerBound()))
        .setUpperBound(roundedNanSafeOf(anomaly.getUpperBound()))
        .setLift(baselineVal.equals("-") ? "" : formattedLiftValue(lift))
        .setPositiveLift(getLiftDirection(lift))
        .setSwi(String.format(NOTIFICATIONS_PERCENTAGE_FORMAT, 0d))
        .setDimensions(getDimensionsList(anomaly.getDimensionMap()))
        .setDuration(getTimeDiffInHours(anomaly.getStartTime(), anomaly.getEndTime())) // duratio
        .setFeedback(feedbackVal)
        .setFunction(alertName)
        .setFuncDescription(alertDescription)
        .setMetric(anomaly.getMetric())
        .setStartDateTime(dateTimeFormatter.print(anomaly.getStartTime()))
        .setEndTime(dateTimeFormatter.print(anomaly.getEndTime()))
        .setTimezone(getTimezoneString(dateTimeFormatter))
        .setIssueType(getIssueType(anomaly))
        .setProperties(SpiUtils.encodeCompactedProperties(props))
        .setMetricUrn(anomaly.getMetricUrn());
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
  public static String getIssueType(AnomalyDTO anomalyResultDTO) {
    Map<String, String> properties = anomalyResultDTO.getProperties();
    if (MapUtils.isNotEmpty(properties) && properties
        .containsKey(AnomalyDTO.ISSUE_TYPE_KEY)) {
      return properties.get(AnomalyDTO.ISSUE_TYPE_KEY);
    }
    return null;
  }

  /**
   * Returns a human readable lift value to be displayed in the notification templates
   */
  public static String formattedLiftValue(final double lift) {

    return String.format(Constants.NOTIFICATIONS_PERCENTAGE_FORMAT, lift * 100);
  }
  
  private static String roundedNanSafeOf(final @Nullable Double value) {
    if (value == null || value.isNaN()) {
      return "-";
    }
    return ThirdEyeUtils.getRoundedValue(value);
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
  public static String getTimezoneString(final @NonNull DateTimeFormatter dateTimeFormatter) {
    final DateTimeZone dateTimeZone = Objects.requireNonNull(dateTimeFormatter.getZone(),
        "The dateTimeFormatter passed to this method must have a defined timezone");
    TimeZone tz = TimeZone.getTimeZone(dateTimeZone.getID());
    return tz.getDisplayName(true, 0);
  }
}
