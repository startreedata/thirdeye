/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.content;

import static ai.startree.thirdeye.notification.content.NotificationContent.PERCENTAGE_FORMAT;
import static ai.startree.thirdeye.notification.content.NotificationContent.RAW_VALUE_FORMAT;

import ai.startree.thirdeye.spi.Constants.CompareMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalyReportEntity {

  String metric;
  String startDateTime;
  String lift; // percentage change
  boolean positiveLift;
  boolean positiveWoWLift;
  boolean positiveWo2WLift;
  boolean positiveWo3WLift;
  boolean positiveWo4WLift;
  String wowValue;
  String wowLift;
  String wo2wValue;
  String wo2wLift;
  String wo3wValue;
  String wo3wLift;
  String wo4wValue;
  String wo4wLift;
  String feedback;
  String anomalyId;
  String anomalyURL;
  String currentVal; // avg current val
  String baselineVal; // avg baseline val
  List<String> dimensions;
  String swi;
  String function;
  String funcDescription;
  String duration;
  String startTime;
  String endTime;
  String timezone;
  String issueType;
  String score;
  Double weight;
  String groupKey;
  String entityName;
  String anomalyType;
  String properties;
  String metricUrn;

  public AnomalyReportEntity(String anomalyId,
      String anomalyURL,
      String baselineVal,
      String currentVal,
      String lift,
      boolean positiveLift,
      Double swi,
      List<String> dimensions,
      String duration,
      String feedback,
      String function,
      String funcDescription,
      String metric,
      String startTime,
      String endTime,
      String timezone,
      String issueType,
      String anomalyType,
      String properties,
      String metricUrn) {
    this.anomalyId = anomalyId;
    this.anomalyURL = anomalyURL;
    this.baselineVal = baselineVal;
    this.currentVal = currentVal;
    this.dimensions = dimensions;
    this.duration = duration;
    this.feedback = feedback;
    this.function = function;
    this.funcDescription = funcDescription;
    this.swi = "";
    if (swi != null) {
      this.swi = String.format(PERCENTAGE_FORMAT, swi * 100);
    }
    if (baselineVal.equals("-")) {
      this.lift = "";
    } else {
      this.lift = lift;
    }
    this.positiveLift = positiveLift;
    this.metric = metric;
    this.startDateTime = startTime;
    this.endTime = endTime;
    this.timezone = timezone;
    this.issueType = issueType;
    this.anomalyType = anomalyType;
    this.properties = properties;
    this.metricUrn = metricUrn;
  }

  public void setSeasonalValues(CompareMode compareMode, double seasonalValue, double current) {
    double lift = NotificationContentUtils.getLift(current, seasonalValue);
    switch (compareMode) {
      case Wo4W:
        this.wo4wValue = String.format(RAW_VALUE_FORMAT, seasonalValue);
        this.wo4wLift = String.format(PERCENTAGE_FORMAT, lift);
        this.positiveWo4WLift = NotificationContentUtils.getLiftDirection(lift);
        break;
      case Wo3W:
        this.wo3wValue = String.format(RAW_VALUE_FORMAT, seasonalValue);
        this.wo3wLift = String.format(PERCENTAGE_FORMAT, lift * 100);
        this.positiveWo3WLift = NotificationContentUtils.getLiftDirection(lift);
        break;
      case Wo2W:
        this.wo2wValue = String.format(RAW_VALUE_FORMAT, seasonalValue);
        this.wo2wLift = String.format(PERCENTAGE_FORMAT, lift * 100);
        this.positiveWo2WLift = NotificationContentUtils.getLiftDirection(lift);
        break;
      case WoW:
        this.wowValue = String.format(RAW_VALUE_FORMAT, seasonalValue);
        this.wowLift = String.format(PERCENTAGE_FORMAT, lift * 100);
        this.positiveWoWLift = NotificationContentUtils.getLiftDirection(lift);
        break;
      default:
    }
  }

  public String getMetric() {
    return metric;
  }

  public AnomalyReportEntity setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public String getStartDateTime() {
    return startDateTime;
  }

  public AnomalyReportEntity setStartDateTime(final String startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  public String getLift() {
    return lift;
  }

  public AnomalyReportEntity setLift(final String lift) {
    this.lift = lift;
    return this;
  }

  public boolean isPositiveLift() {
    return positiveLift;
  }

  public AnomalyReportEntity setPositiveLift(final boolean positiveLift) {
    this.positiveLift = positiveLift;
    return this;
  }

  public boolean isPositiveWoWLift() {
    return positiveWoWLift;
  }

  public AnomalyReportEntity setPositiveWoWLift(final boolean positiveWoWLift) {
    this.positiveWoWLift = positiveWoWLift;
    return this;
  }

  public boolean isPositiveWo2WLift() {
    return positiveWo2WLift;
  }

  public AnomalyReportEntity setPositiveWo2WLift(final boolean positiveWo2WLift) {
    this.positiveWo2WLift = positiveWo2WLift;
    return this;
  }

  public boolean isPositiveWo3WLift() {
    return positiveWo3WLift;
  }

  public AnomalyReportEntity setPositiveWo3WLift(final boolean positiveWo3WLift) {
    this.positiveWo3WLift = positiveWo3WLift;
    return this;
  }

  public boolean isPositiveWo4WLift() {
    return positiveWo4WLift;
  }

  public AnomalyReportEntity setPositiveWo4WLift(final boolean positiveWo4WLift) {
    this.positiveWo4WLift = positiveWo4WLift;
    return this;
  }

  public String getWowValue() {
    return wowValue;
  }

  public AnomalyReportEntity setWowValue(final String wowValue) {
    this.wowValue = wowValue;
    return this;
  }

  public String getWowLift() {
    return wowLift;
  }

  public AnomalyReportEntity setWowLift(final String wowLift) {
    this.wowLift = wowLift;
    return this;
  }

  public String getWo2wValue() {
    return wo2wValue;
  }

  public AnomalyReportEntity setWo2wValue(final String wo2wValue) {
    this.wo2wValue = wo2wValue;
    return this;
  }

  public String getWo2wLift() {
    return wo2wLift;
  }

  public AnomalyReportEntity setWo2wLift(final String wo2wLift) {
    this.wo2wLift = wo2wLift;
    return this;
  }

  public String getWo3wValue() {
    return wo3wValue;
  }

  public AnomalyReportEntity setWo3wValue(final String wo3wValue) {
    this.wo3wValue = wo3wValue;
    return this;
  }

  public String getWo3wLift() {
    return wo3wLift;
  }

  public AnomalyReportEntity setWo3wLift(final String wo3wLift) {
    this.wo3wLift = wo3wLift;
    return this;
  }

  public String getWo4wValue() {
    return wo4wValue;
  }

  public AnomalyReportEntity setWo4wValue(final String wo4wValue) {
    this.wo4wValue = wo4wValue;
    return this;
  }

  public String getWo4wLift() {
    return wo4wLift;
  }

  public AnomalyReportEntity setWo4wLift(final String wo4wLift) {
    this.wo4wLift = wo4wLift;
    return this;
  }

  public String getFeedback() {
    return feedback;
  }

  public AnomalyReportEntity setFeedback(final String feedback) {
    this.feedback = feedback;
    return this;
  }

  public String getAnomalyId() {
    return anomalyId;
  }

  public AnomalyReportEntity setAnomalyId(final String anomalyId) {
    this.anomalyId = anomalyId;
    return this;
  }

  public String getAnomalyURL() {
    return anomalyURL;
  }

  public AnomalyReportEntity setAnomalyURL(final String anomalyURL) {
    this.anomalyURL = anomalyURL;
    return this;
  }

  public String getCurrentVal() {
    return currentVal;
  }

  public AnomalyReportEntity setCurrentVal(final String currentVal) {
    this.currentVal = currentVal;
    return this;
  }

  public String getBaselineVal() {
    return baselineVal;
  }

  public AnomalyReportEntity setBaselineVal(final String baselineVal) {
    this.baselineVal = baselineVal;
    return this;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public AnomalyReportEntity setDimensions(final List<String> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public String getSwi() {
    return swi;
  }

  public AnomalyReportEntity setSwi(final String swi) {
    this.swi = swi;
    return this;
  }

  public String getFunction() {
    return function;
  }

  public AnomalyReportEntity setFunction(final String function) {
    this.function = function;
    return this;
  }

  public String getFuncDescription() {
    return funcDescription;
  }

  public AnomalyReportEntity setFuncDescription(final String funcDescription) {
    this.funcDescription = funcDescription;
    return this;
  }

  public String getDuration() {
    return duration;
  }

  public AnomalyReportEntity setDuration(final String duration) {
    this.duration = duration;
    return this;
  }

  public String getStartTime() {
    return startTime;
  }

  public AnomalyReportEntity setStartTime(final String startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getEndTime() {
    return endTime;
  }

  public AnomalyReportEntity setEndTime(final String endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public AnomalyReportEntity setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getIssueType() {
    return issueType;
  }

  public AnomalyReportEntity setIssueType(final String issueType) {
    this.issueType = issueType;
    return this;
  }

  public String getScore() {
    return score;
  }

  public AnomalyReportEntity setScore(final String score) {
    this.score = score;
    return this;
  }

  public Double getWeight() {
    return weight;
  }

  public AnomalyReportEntity setWeight(final Double weight) {
    this.weight = weight;
    return this;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public AnomalyReportEntity setGroupKey(final String groupKey) {
    this.groupKey = groupKey;
    return this;
  }

  public String getEntityName() {
    return entityName;
  }

  public AnomalyReportEntity setEntityName(final String entityName) {
    this.entityName = entityName;
    return this;
  }

  public String getAnomalyType() {
    return anomalyType;
  }

  public AnomalyReportEntity setAnomalyType(final String anomalyType) {
    this.anomalyType = anomalyType;
    return this;
  }

  public String getProperties() {
    return properties;
  }

  public AnomalyReportEntity setProperties(final String properties) {
    this.properties = properties;
    return this;
  }

  public String getMetricUrn() {
    return metricUrn;
  }

  public AnomalyReportEntity setMetricUrn(final String metricUrn) {
    this.metricUrn = metricUrn;
    return this;
  }
}
