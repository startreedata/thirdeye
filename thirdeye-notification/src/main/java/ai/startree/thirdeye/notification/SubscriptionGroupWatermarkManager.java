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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.stream.Collectors.toMap;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Subscription Group Watermark Manager manages the watermark for each subscription group.
 * The watermark is stored in the subscriptionGroup.vectorClocks field.
 */
@Singleton
public class SubscriptionGroupWatermarkManager {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionGroupWatermarkManager.class);

  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public SubscriptionGroupWatermarkManager(
      final SubscriptionGroupManager subscriptionGroupManager) {
    this.subscriptionGroupManager = subscriptionGroupManager;
  }

  public static Map<Long, Long> buildVectorClock(Collection<AnomalyDTO> anomalies) {
    final Map<Long, Long> alertIdToAnomalyCreateTimeMax = new HashMap<>();
    for (final AnomalyDTO a : anomalies) {
      final Long alertId = a.getDetectionConfigId();
      if (alertId == null) {
        continue;
      }
      final long createTime = optional(a.getCreateTime()).map(Timestamp::getTime).orElse(-1L);
      final long currentMax = alertIdToAnomalyCreateTimeMax.getOrDefault(alertId, createTime);
      final long newMax = Math.max(currentMax, createTime);
      alertIdToAnomalyCreateTimeMax.put(alertId, newMax);
    }
    return alertIdToAnomalyCreateTimeMax;
  }

  public static Map<Long, Long> mergeWatermarks(final Map<Long, Long> a,
      final Map<Long, Long> b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    final Map<Long, Long> result = new HashMap<>(a);
    b.forEach((key, value) -> result.merge(key, value, Math::max));

    return result;
  }

  public static Map<Long, Long> newVectorClocks(final List<AlertAssociationDto> alertAssociations,
      final Map<Long, Long> vectorClocks) {
    final Map<Long, Long> vc = optional(vectorClocks).orElse(Map.of());
    return alertAssociations.stream()
        .map(AlertAssociationDto::getAlert)
        .map(AbstractDTO::getId)
        .collect(toMap(
            id -> id,
            id -> vc.getOrDefault(id, 0L),
            (a, b) -> b)
        );
  }

  public static long getCreateTimeWindowStart(final Map<Long, Long> vectorClocks,
      final long endTime,
      final Long alertId) {
    long startTime = vectorClocks.get(alertId);

    // Do not notify anomalies older than MAX_ANOMALY_NOTIFICATION_LOOKBACK
    final long minStartTime = endTime - Constants.NOTIFICATION_ANOMALY_MAX_LOOKBACK_MS;
    return Math.max(startTime, minStartTime);
  }

  public void updateWatermarks(final SubscriptionGroupDTO sg,
      final Collection<AnomalyDTO> anomalies) {
    if (anomalies.isEmpty()) {
      return;
    }
    final var merged = mergeWatermarks(sg.getVectorClocks(), buildVectorClock(anomalies));
    sg.setVectorClocks(merged);

    LOG.info("Updating watermarks for subscription config : {}", sg.getId());
    subscriptionGroupManager.save(sg);
  }
}
