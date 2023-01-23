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
package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import ai.startree.thirdeye.spi.detection.AnomalySeverity;
import ai.startree.thirdeye.spi.detection.AnomalyType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class AnomalyApi implements ThirdEyeCrudApi<AnomalyApi> {

  private Long id;
  private String namespace;
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

  @Deprecated
  private MetricApi metric;
  private AlertMetadataApi metadata;

  private List<AnomalyApi> children; // ids of the anomalies this anomaly merged from
  private Boolean isChild;
  @Deprecated
  @JsonIgnore
  private AnomalyType type;
  private AnomalySeverity severity;

  private AnomalyFeedbackApi feedback;

  private EnumerationItemApi enumerationItem;
  private List<AnomalyLabelApi> anomalyLabels;
  private AuthorizationConfigurationApi auth;

  public Long getId() {
    return id;
  }

  public AnomalyApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getNamespace() {
    return namespace;
  }

  public AnomalyApi setNamespace(final String namespace) {
    this.namespace = namespace;
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

  @Deprecated
  public MetricApi getMetric() {
    return metric;
  }

  @Deprecated
  public AnomalyApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public AlertMetadataApi getMetadata() {
    return metadata;
  }

  public AnomalyApi setMetadata(final AlertMetadataApi metadata) {
    this.metadata = metadata;
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

  @Deprecated
  public AnomalyType getType() {
    return type;
  }

  @Deprecated
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

  public EnumerationItemApi getEnumerationItem() {
    return enumerationItem;
  }

  public AnomalyApi setEnumerationItem(
      final EnumerationItemApi enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }

  public List<AnomalyLabelApi> getAnomalyLabels() {
    return anomalyLabels;
  }

  public AnomalyApi setAnomalyLabels(
      final List<AnomalyLabelApi> anomalyLabels) {
    this.anomalyLabels = anomalyLabels;
    return this;
  }

  public AuthorizationConfigurationApi getAuth() {
    return auth;
  }

  public AnomalyApi setAuth(final AuthorizationConfigurationApi auth) {
    this.auth = auth;
    return this;
  }
}
