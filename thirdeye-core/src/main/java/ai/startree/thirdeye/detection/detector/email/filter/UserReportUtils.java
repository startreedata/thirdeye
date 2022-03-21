/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
