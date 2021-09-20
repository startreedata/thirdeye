/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.anomalydetection.performanceEvaluation;

import java.util.List;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.joda.time.Interval;

public class PerformanceEvaluateHelper {

  /**
   * This helper initialize the performance evaluator with requested performance evaluation method.
   *
   * @param performanceEvaluationMethod The enum of performance evaluation method; if null or
   *     not found, then ANOMALY_PERCENTAGE
   * @param functionId the original function id. It is for providing user labeled anomaly
   *     information for supervised performance
   *     evaluation, such as precision, recall and f1 score.
   * @param clonedFunctionId the cloned function id. It is the function id to be evaluated. If
   *     functionId == cloneFunctionId, we evaluate the
   *     performance of the original function.
   * @param windowInterval the time interval to be evaluated.
   * @return A proper initiated performance evaluator.
   */
  public static PerformanceEvaluate getPerformanceEvaluator(
      PerformanceEvaluationMethod performanceEvaluationMethod,
      long functionId, long clonedFunctionId, Interval windowInterval,
      MergedAnomalyResultManager mergedAnomalyResultDAO) {
    PerformanceEvaluate performanceEvaluator = null;
    List<MergedAnomalyResultDTO> knownAnomalies = mergedAnomalyResultDAO
        .findOverlappingByFunctionId(functionId,
            windowInterval.getStartMillis(), windowInterval.getEndMillis());
    List<MergedAnomalyResultDTO> detectedMergedAnomalies = mergedAnomalyResultDAO
        .findOverlappingByFunctionId(
            clonedFunctionId, windowInterval.getStartMillis(), windowInterval.getEndMillis());
    switch (performanceEvaluationMethod) {
      case F1_SCORE:
        performanceEvaluator = new F1ScoreByTimePerformanceEvaluation(knownAnomalies,
            detectedMergedAnomalies);
        break;
      case RECALL:
        performanceEvaluator = new RecallByTimePreformanceEvaluation(knownAnomalies,
            detectedMergedAnomalies);
        break;
      case PRECISION:
        performanceEvaluator = new PrecisionByTimePerformanceEvaluation(knownAnomalies,
            detectedMergedAnomalies);
        break;
      case ANOMALY_PERCENTAGE:
      default:
        performanceEvaluator = new AnomalyPercentagePerformanceEvaluation(windowInterval,
            detectedMergedAnomalies);
    }
    return performanceEvaluator;
  }
}
