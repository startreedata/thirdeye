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
package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.notification.anomalyfilter.AlertFilterFactory;
import ai.startree.thirdeye.notification.anomalyfilter.AnomalyFilter;
import ai.startree.thirdeye.notification.anomalyfilter.DummyAnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class to evaluate the performance of a list of merged anomalies
 * Precision and Recall Evaluator with two constructor
 * 1) Anomaly Detection System evaluation: calculate on-going performance using "notified" flag
 * 2) Alert Filter evaluation: calculate performance of alert filter on a list of anomalies
 */
public class PrecisionRecallEvaluator {

  public static final String PRECISION = "precision";
  public static final String WEIGHTED_PRECISION = "weightedPrecision";
  public static final String RECALL = "recall";
  public static final String RESPONSE_RATE = "responseRate";
  public static final String TOTALALERTS = "totalAlerts";
  public static final String TOTALRESPONSES = "totalResponses";
  public static final String TRUEANOMALIES = "trueAnomalies";
  public static final String FALSEALARM = "falseAlarm";
  public static final String NEWTREND = "newTrend";
  public static final String USER_REPORT = "userReportAnomaly";
  public static final Double WEIGHT_OF_NULL_LABEL = 0.5;

  protected final AnomalyFilter anomalyFilter;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  protected boolean useAlertFilterOnAnomaly = false;
  protected AlertFilterFactory alertFilterFactory;
  protected int notifiedTrueAnomaly; // Anomaly is labeled as true and is notified
  protected int notifiedTrueAnomalyNewTrend; // Anomaly is labeled as TRUE_NEW_TREND and is notified
  protected int notifiedFalseAlarm;  // Anomaly is labeled as false and is notified
  protected int notifiedNotLabeled;  // Anomaly is notified, but not labeled
  protected int userReportTrueAnomaly; // Anomaly is user reported: true anomaly that was not sent out
  protected int userReportTrueAnomalyNewTrend; // Anomaly is user reported: true anomaly new trend that was not sent out

  // isProjected to indicate if calculating system performance or alert filter's projected performance
  protected boolean isProjected;

  /**
   * Using this constructor, PrecisionRecallEvaluator will be evaluating performance of given alert
   * filter
   * By comparing alert filter's "isQualified" and labels among the list of anomalies, get the
   * performance statistics for this alert filter
   *  @param anomalies the list of anomalies as data for alert filter
   * @param anomalyFilter the alert filter to be evaluated
   * @param mergedAnomalyResultManager
   */
  public PrecisionRecallEvaluator(List<MergedAnomalyResultDTO> anomalies,
      final AnomalyFilter anomalyFilter,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.anomalyFilter = anomalyFilter;
    this.isProjected = true;
    init(anomalies);
  }
  // the weight used for NA labeled data point when calculating precision

  public static List<String> getPropertyNames() {
    return Collections.unmodifiableList(new ArrayList<>(
        Arrays.asList(RESPONSE_RATE, PRECISION, WEIGHTED_PRECISION, RECALL, TOTALALERTS,
            TOTALRESPONSES, TRUEANOMALIES,
            FALSEALARM, NEWTREND, USER_REPORT)));
  }

  /**
   * Evaluate user report anomaly is qualified given alert filter, user report anomaly, as well as
   * total anomaly set
   * Runs through total anomaly set, find out if total qualified region for system anomalies can
   * reach more than 50% of user report region,
   * return user report anomaly as qualified, otherwise return false
   *
   * @param mergedAnomalyResultManager
   * @param anomalyFilter alert filter to evaluate system detected anoamlies isQualified
   */
  private static Boolean isUserReportAnomalyIsQualified(AnomalyFilter anomalyFilter,
      MergedAnomalyResultDTO userReportAnomaly,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    List<MergedAnomalyResultDTO> systemAnomalies = mergedAnomalyResultManager
        .findByFunctionId(userReportAnomaly.getAnomalyFunction().getId());
    long startTime = userReportAnomaly.getStartTime();
    long endTime = userReportAnomaly.getEndTime();
    long qualifiedRegion = 0;
    systemAnomalies.sort(Comparator.comparingLong(MergedAnomalyResultDTO::getStartTime));
    for (MergedAnomalyResultDTO anomalyResult : systemAnomalies) {
      if (anomalyResult.getAnomalyResultSource()
          .equals(AnomalyResultSource.DEFAULT_ANOMALY_DETECTION)
          && anomalyResult.getEndTime() >= startTime && anomalyResult.getStartTime() <= endTime &&
          anomalyResult.getDimensions().equals(userReportAnomaly.getDimensions())) {
        if (anomalyFilter.isQualified(anomalyResult)) {
          qualifiedRegion += anomalyResult.getEndTime() - anomalyResult.getStartTime();
        }
      }
    }
    return qualifiedRegion >= (endTime - startTime) * 0.5;
  }

  public double getPrecision() {
    if (getTotalAlerts() == 0) {
      return Double.NaN;
    }
    return 1.0 * getTrueAlerts() / getTotalAlerts();
  }

  public double getPrecisionInResponse() {
    if (getTotalResponses() == 0) {
      return Double.NaN;
    }
    return 1.0 * getTrueAlerts() / getTotalResponses();
  }

  public double getWeightedPrecision() {
    if (getTotalAlerts() == 0) {
      return Double.NaN;
    }
    return 1.0 * getTrueAlerts() / (getTotalResponses()
        + WEIGHT_OF_NULL_LABEL * notifiedNotLabeled);
  }

  public double getRecall() {
    if (getTrueAnomalies() == 0) {
      return Double.NaN;
    }
    return 1.0 * getTrueAlerts() / (getTrueAnomalies() + getTrueAnomalyNewTrend());
  }

  public double getFalseNegativeRate() {
    if (getTrueAnomalies() == 0) {
      return Double.NaN;
    }
    return 1.0 * getUserReportAnomaly() / (getTrueAnomalies() + getTrueAnomalyNewTrend());
  }

  public double getResponseRate() {
    return 1.0 * getTotalResponses() / getTotalAlerts();
  }

  // Total responses is including notified labeled anomalies and user report anomalies
  public int getTotalResponses() {
    return notifiedFalseAlarm + notifiedTrueAnomaly + notifiedTrueAnomalyNewTrend
        + userReportTrueAnomaly
        + userReportTrueAnomalyNewTrend;
  }

  public int getTotalAlerts() {
    return getTotalResponses() + notifiedNotLabeled;
  }

  // number of true anomalies in global set
  public int getTrueAnomalies() {
    return notifiedTrueAnomaly + userReportTrueAnomaly;
  }

  // number of true anomalies new trend in global set
  public int getTrueAnomalyNewTrend() {
    return notifiedTrueAnomalyNewTrend + userReportTrueAnomalyNewTrend;
  }

  // number of true anomalies and true_new_trend anomalies that "NOTIFIED"
  public int getTrueAlerts() {
    return notifiedTrueAnomaly + notifiedTrueAnomalyNewTrend;
  }

  public int getUserReportAnomaly() {
    return userReportTrueAnomaly + userReportTrueAnomalyNewTrend;
  }

  public int getFalseAlarm() {
    return notifiedFalseAlarm;
  }

  public void init(List<MergedAnomalyResultDTO> anomalies) {
    if (anomalies == null || anomalies.isEmpty()) {
      return;
    }

    this.notifiedTrueAnomaly = 0;
    this.notifiedTrueAnomalyNewTrend = 0;
    this.notifiedNotLabeled = 0;
    this.notifiedFalseAlarm = 0;
    this.userReportTrueAnomaly = 0;
    this.userReportTrueAnomalyNewTrend = 0;

    for (MergedAnomalyResultDTO anomaly : anomalies) {
      AnomalyFilter anomalyFilterOfAnomaly = this.anomalyFilter;
      if (useAlertFilterOnAnomaly) {
        anomalyFilterOfAnomaly = this.alertFilterFactory
            .fromSpec(anomaly.getAnomalyFunction().getAlertFilter());
      }
      if (anomalyFilterOfAnomaly == null) {
        anomalyFilterOfAnomaly = new DummyAnomalyFilter();
      }

      AnomalyFeedback feedback = anomaly.getFeedback();
      boolean isLabeledTrueAnomaly = false;
      boolean isLabeledTrueAnomalyNewTrend = false;
      if (feedback != null && feedback.getFeedbackType() != null && feedback.getFeedbackType()
          .equals(AnomalyFeedbackType.ANOMALY_NEW_TREND)) {
        isLabeledTrueAnomalyNewTrend = true;
      } else if (feedback != null && feedback.getFeedbackType() != null &&
          (feedback.getFeedbackType().isAnomaly())) {
        isLabeledTrueAnomaly = true;
      }

      // TODO handle AnomalyFeedbackType.ANOMALY_EXPECTED

      // handle user report anomaly
      if (anomaly.getAnomalyResultSource().equals(AnomalyResultSource.USER_LABELED_ANOMALY)) {
        if (!isProjected) {
          if (isLabeledTrueAnomaly) {
            userReportTrueAnomaly++;
          } else if (isLabeledTrueAnomalyNewTrend) {
            userReportTrueAnomalyNewTrend++;
          }
        } else {
          if (isUserReportAnomalyIsQualified(anomalyFilterOfAnomaly, anomaly,
              mergedAnomalyResultManager)) {
            notifiedTrueAnomaly++;
          } else {
            userReportTrueAnomaly++;
          }
        }
      } else {
        // if system detected anomaly, if using projected evaluation, skip those true anomalies that are not notified
        // since these anomalies are originally unsent, but reverted the feedback based on user report
        boolean isNotified =
            isProjected ? anomalyFilterOfAnomaly.isQualified(anomaly) : anomaly.isNotified();

        if (isNotified) {
          if (feedback == null || feedback.getFeedbackType() == null) {
            this.notifiedNotLabeled++;
          } else if (isLabeledTrueAnomaly) {
            notifiedTrueAnomaly++;
          } else if (isLabeledTrueAnomalyNewTrend) {
            notifiedTrueAnomalyNewTrend++;
          } else {
            notifiedFalseAlarm++;
          }
        }
      }
    }
  }

  public Properties toProperties() {
    Properties evals = new Properties();
    evals.put(RESPONSE_RATE, getResponseRate());
    evals.put(PRECISION, getPrecision());
    evals.put(WEIGHTED_PRECISION, getWeightedPrecision());
    evals.put(RECALL, getRecall());
    evals.put(TOTALALERTS, getTotalAlerts());
    evals.put(TOTALRESPONSES, getTotalResponses());
    evals.put(TRUEANOMALIES, getTrueAnomalies());
    evals.put(FALSEALARM, getFalseAlarm());
    evals.put(NEWTREND, getTrueAnomalyNewTrend());
    evals.put(USER_REPORT, getUserReportAnomaly());
    return evals;
  }

  public Map<String, Number> toNumberMap() {
    Map<String, Number> evals = new HashMap<>();
    evals.put(RESPONSE_RATE, getResponseRate());
    evals.put(PRECISION, getPrecisionInResponse());
    evals.put(WEIGHTED_PRECISION, getWeightedPrecision());
    evals.put(RECALL, getRecall());
    evals.put(TOTALALERTS, getTotalAlerts());
    evals.put(TOTALRESPONSES, getTotalResponses());
    evals.put(TRUEANOMALIES, getTrueAnomalies());
    evals.put(FALSEALARM, getFalseAlarm());
    evals.put(NEWTREND, getTrueAnomalyNewTrend());
    evals.put(USER_REPORT, getUserReportAnomaly());
    return evals;
  }
}
