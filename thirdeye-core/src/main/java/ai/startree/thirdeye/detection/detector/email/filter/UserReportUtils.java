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
package ai.startree.thirdeye.detection.detector.email.filter;

import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import java.util.Comparator;
import java.util.List;

public class UserReportUtils {

  /**
   * Evaluate user report anomaly is qualified given alert filter, user report anomaly, as well as
   * total anomaly set
   * Runs through total anomaly set, find out if total qualified region for system anomalies can
   * reach more than 50% of user report region,
   * return user report anomaly as qualified, otherwise return false
   *
   * @param mergedAnomalyResultManager
   * @param alertFilter alert filter to evaluate system detected anoamlies isQualified
   */
  public static Boolean isUserReportAnomalyIsQualified(AlertFilter alertFilter,
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
        if (alertFilter.isQualified(anomalyResult)) {
          qualifiedRegion += anomalyResult.getEndTime() - anomalyResult.getStartTime();
        }
      }
    }
    return qualifiedRegion >= (endTime - startTime) * 0.5;
  }
}
