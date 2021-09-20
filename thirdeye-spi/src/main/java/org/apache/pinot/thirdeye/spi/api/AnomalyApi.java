package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResultSource;
import org.apache.pinot.thirdeye.spi.detection.AnomalySeverity;
import org.apache.pinot.thirdeye.spi.detection.AnomalyType;

@JsonInclude(Include.NON_NULL)
public class AnomalyApi implements ThirdEyeCrudApi<AnomalyApi> {

  private Long id;
  private Date startTime;
  private Date endTime;

  private Double avgCurrentVal;
  private Double avgBaselineVal;
  private Double score;
  private Double weight;
  private Double impactToGlobal;
  private AnomalyResultSource sourceType;

  private Date created;
  private Boolean notified;

  private String message;

  private AlertApi alert;
  private AlertNodeApi alertNode;
  private MetricApi metric;

  private List<AnomalyApi> children; // ids of the anomalies this anomaly merged from
  private Boolean isChild;
  private AnomalyType type;
  private AnomalySeverity severity;

  private AnomalyFeedbackApi feedback;

  public Long getId() {
    return id;
  }

  public AnomalyApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public Date getStartTime() {
    return startTime;
  }

  public AnomalyApi setStartTime(final Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public Date getEndTime() {
    return endTime;
  }

  public AnomalyApi setEndTime(final Date endTime) {
    this.endTime = endTime;
    return this;
  }

  public Double getAvgCurrentVal() {
    return avgCurrentVal;
  }

  public AnomalyApi setAvgCurrentVal(final Double avgCurrentVal) {
    this.avgCurrentVal = avgCurrentVal;
    return this;
  }

  public Double getAvgBaselineVal() {
    return avgBaselineVal;
  }

  public AnomalyApi setAvgBaselineVal(final Double avgBaselineVal) {
    this.avgBaselineVal = avgBaselineVal;
    return this;
  }

  public Double getScore() {
    return score;
  }

  public AnomalyApi setScore(final Double score) {
    this.score = score;
    return this;
  }

  public Double getWeight() {
    return weight;
  }

  public AnomalyApi setWeight(final Double weight) {
    this.weight = weight;
    return this;
  }

  public Double getImpactToGlobal() {
    return impactToGlobal;
  }

  public AnomalyApi setImpactToGlobal(final Double impactToGlobal) {
    this.impactToGlobal = impactToGlobal;
    return this;
  }

  public AnomalyResultSource getSourceType() {
    return sourceType;
  }

  public AnomalyApi setSourceType(final AnomalyResultSource sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public AnomalyApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Boolean getNotified() {
    return notified;
  }

  public AnomalyApi setNotified(final Boolean notified) {
    this.notified = notified;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public AnomalyApi setMessage(final String message) {
    this.message = message;
    return this;
  }

  public AlertApi getAlert() {
    return alert;
  }

  public AnomalyApi setAlert(final AlertApi alert) {
    this.alert = alert;
    return this;
  }

  public AlertNodeApi getAlertNode() {
    return alertNode;
  }

  public AnomalyApi setAlertNode(
      final AlertNodeApi alertNode) {
    this.alertNode = alertNode;
    return this;
  }

  public MetricApi getMetric() {
    return metric;
  }

  public AnomalyApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public List<AnomalyApi> getChildren() {
    return children;
  }

  public AnomalyApi setChildren(final List<AnomalyApi> children) {
    this.children = children;
    return this;
  }

  public Boolean getChild() {
    return isChild;
  }

  public AnomalyApi setChild(final Boolean child) {
    isChild = child;
    return this;
  }

  public AnomalyType getType() {
    return type;
  }

  public AnomalyApi setType(final AnomalyType type) {
    this.type = type;
    return this;
  }

  public AnomalySeverity getSeverity() {
    return severity;
  }

  public AnomalyApi setSeverity(final AnomalySeverity severity) {
    this.severity = severity;
    return this;
  }

  public AnomalyFeedbackApi getFeedback() {
    return feedback;
  }

  public AnomalyApi setFeedback(final AnomalyFeedbackApi feedback) {
    this.feedback = feedback;
    return this;
  }
}
