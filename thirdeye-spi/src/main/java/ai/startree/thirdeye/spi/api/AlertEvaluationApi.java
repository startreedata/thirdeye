/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class AlertEvaluationApi {

  private AlertApi alert;
  private Map<String, DetectionEvaluationApi> detectionEvaluations;
  private Map<String, Map<String, DetectionEvaluationApi>> evaluations;
  private Date start;
  private Date end;
  private EvaluationContextApi evaluationContext;
  private Date lastTimestamp;
  Boolean dryRun;

  public AlertApi getAlert() {
    return alert;
  }

  public AlertEvaluationApi setAlert(final AlertApi alert) {
    this.alert = alert;
    return this;
  }

  public Map<String, DetectionEvaluationApi> getDetectionEvaluations() {
    return detectionEvaluations;
  }

  public AlertEvaluationApi setDetectionEvaluations(
      final Map<String, DetectionEvaluationApi> detectionEvaluations) {
    this.detectionEvaluations = detectionEvaluations;
    return this;
  }

  public Date getStart() {
    return start;
  }

  public AlertEvaluationApi setStart(final Date start) {
    this.start = start;
    return this;
  }

  public Date getEnd() {
    return end;
  }

  public AlertEvaluationApi setEnd(final Date end) {
    this.end = end;
    return this;
  }

  public EvaluationContextApi getEvaluationContext() {
    return evaluationContext;
  }

  public AlertEvaluationApi setEvaluationContext(
      final EvaluationContextApi evaluationContext) {
    this.evaluationContext = evaluationContext;
    return this;
  }

  public Date getLastTimestamp() {
    return lastTimestamp;
  }

  public AlertEvaluationApi setLastTimestamp(final Date lastTimestamp) {
    this.lastTimestamp = lastTimestamp;
    return this;
  }

  public Map<String, Map<String, DetectionEvaluationApi>> getEvaluations() {
    return evaluations;
  }

  public AlertEvaluationApi setEvaluations(
      final Map<String, Map<String, DetectionEvaluationApi>> evaluations) {
    this.evaluations = evaluations;
    return this;
  }

  public Boolean isDryRun() {
    return dryRun;
  }

  public AlertEvaluationApi setDryRun(final Boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }
}
