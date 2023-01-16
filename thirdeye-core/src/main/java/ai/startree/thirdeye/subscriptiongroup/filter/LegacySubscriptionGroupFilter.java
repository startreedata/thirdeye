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
package ai.startree.thirdeye.subscriptiongroup.filter;

import static ai.startree.thirdeye.spi.util.AnomalyUtils.isIgnore;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.stream.Collectors.toMap;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The detection alert filter that sends the anomaly email to all recipients
 */
@Singleton
public class LegacySubscriptionGroupFilter {

  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;

  @Inject
  public LegacySubscriptionGroupFilter(final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
  }

  /**
   * Helper to determine presence of user-feedback for an anomaly
   *
   * @param anomaly anomaly
   * @return {@code true} if feedback exists and is TRUE or FALSE, {@code false} otherwise
   */
  private static boolean hasFeedback(final MergedAnomalyResultDTO anomaly) {
    return anomaly.getFeedback() != null
        && !anomaly.getFeedback().getFeedbackType().isUnresolved();
  }

  public DetectionAlertFilterResult filter(final SubscriptionGroupDTO subscriptionGroup,
      final long endTime) {
    final List<Long> alertIds = ConfigUtils.getLongs(subscriptionGroup.getProperties()
        .get(PROP_DETECTION_CONFIG_IDS));

    final DetectionAlertFilterResult result = new DetectionAlertFilterResult();

    // Fetch all the anomalies to be notified to the recipients
    final Map<Long, Long> vectorClocks = newVectorClocks(alertIds,
        subscriptionGroup.getVectorClocks());
    final Set<MergedAnomalyResultDTO> anomalies = filter(vectorClocks, endTime);
    return result.addMapping(new DetectionAlertFilterNotification(subscriptionGroup), anomalies);
  }

  private Set<MergedAnomalyResultDTO> filter(final Map<Long, Long> vectorClocks,
      final long endTime) {
    // retrieve all candidate anomalies
    final Set<MergedAnomalyResultDTO> allAnomalies = new HashSet<>();
    for (final Long detectionId : vectorClocks.keySet()) {
      // Ignore disabled detections
      final AlertDTO detection = alertManager
          .findById(detectionId);
      if (detection == null || !detection.isActive()) {
        continue;
      }

      // No point in fetching anomalies older than MAX_ANOMALY_NOTIFICATION_LOOKBACK
      long startTime = vectorClocks.get(detectionId);
      if (startTime < endTime - Constants.ANOMALY_NOTIFICATION_LOOKBACK_TIME) {
        startTime = endTime - Constants.ANOMALY_NOTIFICATION_LOOKBACK_TIME;
      }

      final Collection<MergedAnomalyResultDTO> candidates = mergedAnomalyResultManager
          .findByCreatedTimeInRangeAndDetectionConfigId(startTime + 1, endTime, detectionId);

      final long finalStartTime = startTime;
      final Collection<MergedAnomalyResultDTO> anomalies =
          Collections2.filter(candidates, anomaly -> anomaly != null && !anomaly.isChild()
              && !hasFeedback(anomaly)
              && anomaly.getCreatedTime() > finalStartTime
              && !isIgnore(anomaly)
              && (anomaly.getAnomalyResultSource()
              .equals(AnomalyResultSource.DEFAULT_ANOMALY_DETECTION) ||
              anomaly.getAnomalyResultSource()
                  .equals(AnomalyResultSource.ANOMALY_REPLAY)));

      allAnomalies.addAll(anomalies);
    }
    return allAnomalies;
  }

  private Map<Long, Long> newVectorClocks(final Collection<Long> alertIds,
      final Map<Long, Long> vectorClocks) {
    return alertIds.stream().collect(toMap(
        id -> id,
        id -> optional(vectorClocks).orElse(Map.of()).getOrDefault(id, 0L),
        (a, b) -> b)
    );
  }
}
