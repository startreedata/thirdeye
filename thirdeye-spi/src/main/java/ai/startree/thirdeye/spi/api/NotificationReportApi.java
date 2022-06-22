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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class NotificationReportApi {

  private String startTime;
  private String endTime;
  private String timeZone;
  private Integer notifiedCount;
  private Integer feedbackCount;
  private Integer trueAlertCount;
  private Integer falseAlertCount;
  private Integer newTrendCount;
  private String alertConfigName;
  private Boolean includeSummary;
  private Long reportGenerationTimeMillis;
  private Double precision;
  private Double recall;
  private Double falseNegative;
  private Map<String, String> referenceLinks;
  private String dashboardHost;
  private List<EventApi> relatedEvents;

  public String getStartTime() {
    return startTime;
  }

  public NotificationReportApi setStartTime(final String startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getEndTime() {
    return endTime;
  }

  public NotificationReportApi setEndTime(final String endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public NotificationReportApi setTimeZone(final String timeZone) {
    this.timeZone = timeZone;
    return this;
  }

  public Integer getNotifiedCount() {
    return notifiedCount;
  }

  public NotificationReportApi setNotifiedCount(final Integer notifiedCount) {
    this.notifiedCount = notifiedCount;
    return this;
  }

  public Integer getFeedbackCount() {
    return feedbackCount;
  }

  public NotificationReportApi setFeedbackCount(final Integer feedbackCount) {
    this.feedbackCount = feedbackCount;
    return this;
  }

  public Integer getTrueAlertCount() {
    return trueAlertCount;
  }

  public NotificationReportApi setTrueAlertCount(final Integer trueAlertCount) {
    this.trueAlertCount = trueAlertCount;
    return this;
  }

  public Integer getFalseAlertCount() {
    return falseAlertCount;
  }

  public NotificationReportApi setFalseAlertCount(final Integer falseAlertCount) {
    this.falseAlertCount = falseAlertCount;
    return this;
  }

  public Integer getNewTrendCount() {
    return newTrendCount;
  }

  public NotificationReportApi setNewTrendCount(final Integer newTrendCount) {
    this.newTrendCount = newTrendCount;
    return this;
  }

  public String getAlertConfigName() {
    return alertConfigName;
  }

  public NotificationReportApi setAlertConfigName(final String alertConfigName) {
    this.alertConfigName = alertConfigName;
    return this;
  }

  public Boolean getIncludeSummary() {
    return includeSummary;
  }

  public NotificationReportApi setIncludeSummary(final Boolean includeSummary) {
    this.includeSummary = includeSummary;
    return this;
  }

  public Long getReportGenerationTimeMillis() {
    return reportGenerationTimeMillis;
  }

  public NotificationReportApi setReportGenerationTimeMillis(
      final Long reportGenerationTimeMillis) {
    this.reportGenerationTimeMillis = reportGenerationTimeMillis;
    return this;
  }

  public Double getPrecision() {
    return precision;
  }

  public NotificationReportApi setPrecision(final Double precision) {
    this.precision = precision;
    return this;
  }

  public Double getRecall() {
    return recall;
  }

  public NotificationReportApi setRecall(final Double recall) {
    this.recall = recall;
    return this;
  }

  public Double getFalseNegative() {
    return falseNegative;
  }

  public NotificationReportApi setFalseNegative(final Double falseNegative) {
    this.falseNegative = falseNegative;
    return this;
  }

  public Map<String, String> getReferenceLinks() {
    return referenceLinks;
  }

  public NotificationReportApi setReferenceLinks(
      final Map<String, String> referenceLinks) {
    this.referenceLinks = referenceLinks;
    return this;
  }

  public String getDashboardHost() {
    return dashboardHost;
  }

  public NotificationReportApi setDashboardHost(final String dashboardHost) {
    this.dashboardHost = dashboardHost;
    return this;
  }

  public List<EventApi> getRelatedEvents() {
    return relatedEvents;
  }

  public NotificationReportApi setRelatedEvents(
      final List<EventApi> relatedEvents) {
    this.relatedEvents = relatedEvents;
    return this;
  }
}
