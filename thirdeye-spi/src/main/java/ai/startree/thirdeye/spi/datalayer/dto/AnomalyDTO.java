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
package ai.startree.thirdeye.spi.datalayer.dto;

import static ai.startree.thirdeye.spi.util.TimeUtils.fromEpoch;

import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import ai.startree.thirdeye.spi.detection.AnomalySeverity;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalyDTO extends AbstractDTO implements Comparable<AnomalyDTO>, Serializable {

  public static final String ISSUE_TYPE_KEY = "issue_type";
  private Long functionId;
  private Long anomalyFeedbackId;
  private String collection;
  private String metric;
  @JsonIgnore
  @Deprecated
  private DimensionMap dimensions = new DimensionMap();
  private long startTime;
  private long endTime;
  private double avgCurrentVal; // actual value
  private double avgBaselineVal; // expected value
  private double score; // confidence level
  private double weight; // change percentage, whose absolute value is severity
  private double impactToGlobal; // the impact of this anomaly to the global metric
  private AnomalyResultSource anomalyResultSource = AnomalyResultSource.DEFAULT_ANOMALY_DETECTION;
  // Additional anomaly detection properties (e.g., patter=UP, etc.)
  // Being used as identifying if two merged anomalies have same anomaly detection properties, thus can be mergeable
  private Map<String, String> properties = new HashMap<>();

  private boolean notified;
  private String message;
  @Deprecated
  @JsonIgnore
  private String metricUrn;
  private Long detectionConfigId;
  private Set<Long> childIds; // ids of the anomalies this anomaly merged from
  private boolean isChild;
  @Deprecated
  @JsonIgnore
  private String type;
  private AnomalySeverity severityLabel = AnomalySeverity.DEFAULT;
  private String source; // expected format: [alert-name]/[node-name]
  // TODO: Remove raw anomaly id list after old merged anomalies are cleaned up
  @Deprecated
  private List<Long> rawAnomalyIdList;
  @JsonIgnore
  private AnomalyFeedbackDTO feedback;
  @JsonIgnore
  @Deprecated
  private Object anomalyFunction;
  @JsonIgnore
  private Set<AnomalyDTO> children = new HashSet<>();
  // flag to be set when severity changes but not to be persisted
  @JsonIgnore
  private boolean renotify = false;

  private EnumerationItemDTO enumerationItem;
  private List<AnomalyLabelDTO> anomalyLabels;

  public Set<Long> getChildIds() {
    return childIds;
  }

  public AnomalyDTO setChildIds(Set<Long> childIds) {
    this.childIds = childIds;
    return this;
  }

  public boolean isChild() {
    return isChild;
  }

  public AnomalyDTO setChild(boolean child) {
    isChild = child;
    return this;
  }

  public Long getDetectionConfigId() {
    return detectionConfigId;
  }

  public AnomalyDTO setDetectionConfigId(final Long detectionConfigId) {
    this.detectionConfigId = detectionConfigId;
    return this;
  }

  @Deprecated
  public Long getFunctionId() {
    return functionId;
  }

  @Deprecated
  public void setFunctionId(Long functionId) {
    this.functionId = functionId;
  }

  public Long getAnomalyFeedbackId() {
    return anomalyFeedbackId;
  }

  public AnomalyDTO setAnomalyFeedbackId(Long anomalyFeedbackId) {
    this.anomalyFeedbackId = anomalyFeedbackId;
    return this;
  }

  public String getCollection() {
    return collection;
  }

  public AnomalyDTO setCollection(final String collection) {
    this.collection = collection;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public AnomalyDTO setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public AnomalyDTO setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public AnomalyDTO setEndTime(long endTime) {
    this.endTime = endTime;
    return this;
  }

  /**
   * DimensionMap class is deprecated.
   * TODO spyne remove this.
   */
  @Deprecated
  public DimensionMap getDimensions() {
    return dimensions;
  }

  @Deprecated
  public void setDimensions(DimensionMap dimensions) {
    this.dimensions = dimensions;
  }

  public double getAvgCurrentVal() {
    return this.avgCurrentVal;
  }

  public AnomalyDTO setAvgCurrentVal(double val) {
    this.avgCurrentVal = val;
    return this;
  }

  public double getAvgBaselineVal() {
    return this.avgBaselineVal;
  }

  public AnomalyDTO setAvgBaselineVal(double val) {
    this.avgBaselineVal = val;
    return this;
  }

  public double getScore() {
    return score;
  }

  public AnomalyDTO setScore(double score) {
    this.score = score;
    return this;
  }

  public double getWeight() {
    return weight;
  }

  public AnomalyDTO setWeight(double weight) {
    this.weight = weight;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public AnomalyDTO setProperties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public boolean isNotified() {
    return notified;
  }

  public AnomalyDTO setNotified(boolean notified) {
    this.notified = notified;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public AnomalyDTO setMessage(String message) {
    this.message = message;
    return this;
  }

  // TODO: Remove this method after old merged anomalies are cleaned up
  @Deprecated
  public List<Long> getRawAnomalyIdList() {
    return rawAnomalyIdList;
  }

  // TODO: Remove this method after old merged anomalies are cleaned up
  @Deprecated
  public void setRawAnomalyIdList(List<Long> rawAnomalyIdList) {
    this.rawAnomalyIdList = rawAnomalyIdList;
  }

  public double getImpactToGlobal() {
    return impactToGlobal;
  }

  public AnomalyDTO setImpactToGlobal(double impactToGlobal) {
    this.impactToGlobal = impactToGlobal;
    return this;
  }

  public AnomalyResultSource getAnomalyResultSource() {
    return anomalyResultSource;
  }

  public AnomalyDTO setAnomalyResultSource(AnomalyResultSource anomalyResultSource) {
    this.anomalyResultSource = anomalyResultSource;
    return this;
  }

  @Deprecated
  public String getMetricUrn() {
    return metricUrn;
  }

  @Deprecated
  public void setMetricUrn(String metricUrn) {
    this.metricUrn = metricUrn;
  }

  @Deprecated
  public String getType() {
    return type;
  }

  @Deprecated
  public AnomalyDTO setType(String type) {
    this.type = type;
    return this;
  }

  public AnomalySeverity getSeverityLabel() {
    // default severity level is debug
    if (severityLabel == null) {
      return AnomalySeverity.DEFAULT;
    }
    return severityLabel;
  }

  public AnomalyDTO setSeverityLabel(AnomalySeverity severityLabel) {
    this.severityLabel = severityLabel;
    return this;
  }

  public List<AnomalyLabelDTO> getAnomalyLabels() {
    return anomalyLabels;
  }

  public AnomalyDTO setAnomalyLabels(
      final List<AnomalyLabelDTO> anomalyLabels) {
    this.anomalyLabels = anomalyLabels;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(getId(), startTime, endTime, collection, metric, score, impactToGlobal,
            avgBaselineVal, avgCurrentVal, anomalyResultSource, detectionConfigId,
            childIds, isChild, anomalyLabels);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AnomalyDTO)) {
      return false;
    }
    if (o == this) {
      return true;
    }
    AnomalyDTO m = (AnomalyDTO) o;
    return Objects.equals(getId(), m.getId()) && Objects.equals(startTime, m.getStartTime())
        && Objects
        .equals(endTime, m.getEndTime()) && Objects.equals(collection, m.getCollection()) && Objects
        .equals(metric, m.getMetric()) && Objects.equals(score, m.getScore())
        && Objects.equals(avgBaselineVal, m.getAvgBaselineVal())
        && Objects
        .equals(avgCurrentVal, m.getAvgCurrentVal()) && Objects
        .equals(impactToGlobal, m.getImpactToGlobal()) &&
        Objects.equals(anomalyResultSource, m.getAnomalyResultSource()) &&
        Objects.equals(detectionConfigId, m.getDetectionConfigId()) && Objects
        .equals(childIds, m.getChildIds()) &&
        Objects.equals(isChild, m.isChild()) &&
        Objects.equals(anomalyLabels, m.getAnomalyLabels());
  }

  @Override
  public int compareTo(AnomalyDTO o) {
    // compare by -startTime, id
    int diff = -ObjectUtils.compare(startTime, o.getStartTime()); // inverted to sort by
    // decreasing time
    if (diff != 0) {
      return diff;
    }
    return ObjectUtils.compare(getId(), o.getId());
  }

  @Deprecated
  public Multimap<String, String> getDimensionMap() {

    return ArrayListMultimap.create();
  }

  public AnomalyFeedback getFeedback() {
    return this.feedback;
  }

  public AnomalyDTO setFeedback(AnomalyFeedback anomalyFeedback) {
    if (anomalyFeedback == null) {
      this.feedback = null;
    } else if (anomalyFeedback instanceof AnomalyFeedbackDTO) {
      this.feedback = (AnomalyFeedbackDTO) anomalyFeedback;
    } else {
      this.feedback = new AnomalyFeedbackDTO(anomalyFeedback);
    }
    return this;
  }

  public Set<AnomalyDTO> getChildren() {
    return children;
  }

  public AnomalyDTO setChildren(Set<AnomalyDTO> children) {
    this.children = children;
    return this;
  }

  public boolean isRenotify() {
    return renotify;
  }

  public AnomalyDTO setRenotify(boolean renotify) {
    this.renotify = renotify;
    return this;
  }

  public String getSource() {
    return source;
  }

  public AnomalyDTO setSource(final String source) {
    this.source = source;
    return this;
  }

  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  public AnomalyDTO setEnumerationItem(
      final EnumerationItemDTO enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", getId())
        .add("startTime", fromEpoch(startTime))
        .add("endTime", fromEpoch(endTime))
        .toString();
  }

  @Override
  public AnomalyDTO setCreateTime(final Timestamp createTime) {
    super.setCreateTime(createTime);
    return this;
  }
}
