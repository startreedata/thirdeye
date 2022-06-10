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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * This object is directly consumed by FTL templates when composing emails
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalyReportDataApi {

  private String metric;
  private String startDateTime;
  private String lift; // percentage change
  private boolean positiveLift;
  private boolean positiveWoWLift;
  private boolean positiveWo2WLift;
  private boolean positiveWo3WLift;
  private boolean positiveWo4WLift;
  private String wowValue;
  private String wowLift;
  private String wo2wValue;
  private String wo2wLift;
  private String wo3wValue;
  private String wo3wLift;
  private String wo4wValue;
  private String wo4wLift;
  private String feedback;
  private String anomalyId;
  private String anomalyURL;
  private String currentVal; // avg current val
  private String baselineVal; // avg baseline val
  private List<String> dimensions;
  private String swi;
  private String function;
  private String funcDescription;
  private String duration;
  private String startTime;
  private String endTime;
  private String timezone;
  private String issueType;
  private String score;
  private Double weight;
  private String groupKey;
  private String entityName;
  private String anomalyType;
  private String properties;
  private String metricUrn;

  public AnomalyReportDataApi() {
  }

  public String getMetric() {
    return metric;
  }

  public AnomalyReportDataApi setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public String getStartDateTime() {
    return startDateTime;
  }

  public AnomalyReportDataApi setStartDateTime(final String startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  public String getLift() {
    return lift;
  }

  public AnomalyReportDataApi setLift(final String lift) {
    this.lift = lift;
    return this;
  }

  public boolean isPositiveLift() {
    return positiveLift;
  }

  public AnomalyReportDataApi setPositiveLift(final boolean positiveLift) {
    this.positiveLift = positiveLift;
    return this;
  }

  public boolean isPositiveWoWLift() {
    return positiveWoWLift;
  }

  public AnomalyReportDataApi setPositiveWoWLift(final boolean positiveWoWLift) {
    this.positiveWoWLift = positiveWoWLift;
    return this;
  }

  public boolean isPositiveWo2WLift() {
    return positiveWo2WLift;
  }

  public AnomalyReportDataApi setPositiveWo2WLift(final boolean positiveWo2WLift) {
    this.positiveWo2WLift = positiveWo2WLift;
    return this;
  }

  public boolean isPositiveWo3WLift() {
    return positiveWo3WLift;
  }

  public AnomalyReportDataApi setPositiveWo3WLift(final boolean positiveWo3WLift) {
    this.positiveWo3WLift = positiveWo3WLift;
    return this;
  }

  public boolean isPositiveWo4WLift() {
    return positiveWo4WLift;
  }

  public AnomalyReportDataApi setPositiveWo4WLift(final boolean positiveWo4WLift) {
    this.positiveWo4WLift = positiveWo4WLift;
    return this;
  }

  public String getWowValue() {
    return wowValue;
  }

  public AnomalyReportDataApi setWowValue(final String wowValue) {
    this.wowValue = wowValue;
    return this;
  }

  public String getWowLift() {
    return wowLift;
  }

  public AnomalyReportDataApi setWowLift(final String wowLift) {
    this.wowLift = wowLift;
    return this;
  }

  public String getWo2wValue() {
    return wo2wValue;
  }

  public AnomalyReportDataApi setWo2wValue(final String wo2wValue) {
    this.wo2wValue = wo2wValue;
    return this;
  }

  public String getWo2wLift() {
    return wo2wLift;
  }

  public AnomalyReportDataApi setWo2wLift(final String wo2wLift) {
    this.wo2wLift = wo2wLift;
    return this;
  }

  public String getWo3wValue() {
    return wo3wValue;
  }

  public AnomalyReportDataApi setWo3wValue(final String wo3wValue) {
    this.wo3wValue = wo3wValue;
    return this;
  }

  public String getWo3wLift() {
    return wo3wLift;
  }

  public AnomalyReportDataApi setWo3wLift(final String wo3wLift) {
    this.wo3wLift = wo3wLift;
    return this;
  }

  public String getWo4wValue() {
    return wo4wValue;
  }

  public AnomalyReportDataApi setWo4wValue(final String wo4wValue) {
    this.wo4wValue = wo4wValue;
    return this;
  }

  public String getWo4wLift() {
    return wo4wLift;
  }

  public AnomalyReportDataApi setWo4wLift(final String wo4wLift) {
    this.wo4wLift = wo4wLift;
    return this;
  }

  public String getFeedback() {
    return feedback;
  }

  public AnomalyReportDataApi setFeedback(final String feedback) {
    this.feedback = feedback;
    return this;
  }

  public String getAnomalyId() {
    return anomalyId;
  }

  public AnomalyReportDataApi setAnomalyId(final String anomalyId) {
    this.anomalyId = anomalyId;
    return this;
  }

  public String getAnomalyURL() {
    return anomalyURL;
  }

  public AnomalyReportDataApi setAnomalyURL(final String anomalyURL) {
    this.anomalyURL = anomalyURL;
    return this;
  }

  public String getCurrentVal() {
    return currentVal;
  }

  public AnomalyReportDataApi setCurrentVal(final String currentVal) {
    this.currentVal = currentVal;
    return this;
  }

  public String getBaselineVal() {
    return baselineVal;
  }

  public AnomalyReportDataApi setBaselineVal(final String baselineVal) {
    this.baselineVal = baselineVal;
    return this;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public AnomalyReportDataApi setDimensions(final List<String> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public String getSwi() {
    return swi;
  }

  public AnomalyReportDataApi setSwi(final String swi) {
    this.swi = swi;
    return this;
  }

  public String getFunction() {
    return function;
  }

  public AnomalyReportDataApi setFunction(final String function) {
    this.function = function;
    return this;
  }

  public String getFuncDescription() {
    return funcDescription;
  }

  public AnomalyReportDataApi setFuncDescription(final String funcDescription) {
    this.funcDescription = funcDescription;
    return this;
  }

  public String getDuration() {
    return duration;
  }

  public AnomalyReportDataApi setDuration(final String duration) {
    this.duration = duration;
    return this;
  }

  public String getStartTime() {
    return startTime;
  }

  public AnomalyReportDataApi setStartTime(final String startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getEndTime() {
    return endTime;
  }

  public AnomalyReportDataApi setEndTime(final String endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public AnomalyReportDataApi setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getIssueType() {
    return issueType;
  }

  public AnomalyReportDataApi setIssueType(final String issueType) {
    this.issueType = issueType;
    return this;
  }

  public String getScore() {
    return score;
  }

  public AnomalyReportDataApi setScore(final String score) {
    this.score = score;
    return this;
  }

  public Double getWeight() {
    return weight;
  }

  public AnomalyReportDataApi setWeight(final Double weight) {
    this.weight = weight;
    return this;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public AnomalyReportDataApi setGroupKey(final String groupKey) {
    this.groupKey = groupKey;
    return this;
  }

  public String getEntityName() {
    return entityName;
  }

  public AnomalyReportDataApi setEntityName(final String entityName) {
    this.entityName = entityName;
    return this;
  }

  public String getAnomalyType() {
    return anomalyType;
  }

  public AnomalyReportDataApi setAnomalyType(final String anomalyType) {
    this.anomalyType = anomalyType;
    return this;
  }

  public String getProperties() {
    return properties;
  }

  public AnomalyReportDataApi setProperties(final String properties) {
    this.properties = properties;
    return this;
  }

  public String getMetricUrn() {
    return metricUrn;
  }

  public AnomalyReportDataApi setMetricUrn(final String metricUrn) {
    this.metricUrn = metricUrn;
    return this;
  }
}
