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

import ai.startree.thirdeye.alert.AlertDataRetriever;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Subscription Group Watermark Manager manages the watermark for each subscription group.
 * The watermark is stored in the subscriptionGroup.vectorClocks field.
 */
@Singleton
public class NotificationTaskPostProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationTaskPostProcessor.class);

  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AlertManager alertManager;
  private final AnomalyManager anomalyManager;
  private final AlertDataRetriever alertDataRetriever;

  @Inject
  public NotificationTaskPostProcessor(
      final SubscriptionGroupManager subscriptionGroupManager,
      final AlertManager alertManager,
      final AnomalyManager anomalyManager,
      final AlertDataRetriever alertDataRetriever) {
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.alertManager = alertManager;
    this.anomalyManager = anomalyManager;
    this.alertDataRetriever = alertDataRetriever;
  }

  private static Map<Long, Long> buildVectorClock(final Collection<AnomalyDTO> anomalies) {
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

  private static Map<Long, Long> mergeWatermarks(final Map<Long, Long> a,
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

  private static long initialWatermark(final AlertAssociationDto aa,
      final AlertDTO alert,
      final SubscriptionGroupDTO sg) {
    long initialWatermark = Math.max(alert.getCreateTime().getTime(), sg.getCreateTime().getTime());
    if (aa.getCreateTime() != null) {
      initialWatermark = Math.max(initialWatermark, aa.getCreateTime().getTime());
    }
    // initialize to lastTimestamp of the alert
    return Math.max(initialWatermark, alert.getLastTimestamp());
  }

  public void postProcess(final NotificationTaskFilterResult result,
      final Map<NotificationSpecDTO, Exception> specToException) {
    if (specToException.values().stream().allMatch(Objects::nonNull)) {
      // all notification channels failed
      throw new RuntimeException(
          String.format(
              "Failed to notify anomalies for all channels: %s. All exceptions were logged. Throwing one of the exception encountered.",
              specToException.keySet()
                  .stream()
                  .map(NotificationSpecDTO::getType)
                  .collect(Collectors.joining(","))),
          specToException.values().iterator().next());
    }
    // at least one notification channel notified successfully - update the watermarks
    /* Update anomalies */
    for (final AnomalyDTO anomaly : result.getAnomalies()) {
      anomalyManager.update(anomaly.setNotified(true));
    }

    /* Record watermarks */
    final SubscriptionGroupDTO sg = result.getSubscriptionGroup();
    updateWatermarks(sg, result.getAnomalies());

    /* Update completion watermarks */
    for (final AlertAssociationDto aa : optional(sg.getAlertAssociations()).orElse(List.of())) {
      final AlertDTO alert = alertManager.findById(aa.getAlert().getId());
      final Period mergeMaxGap = alertDataRetriever.getMergeMaxGap(alert);
      final long mergeMaxGapMillis = mergeMaxGap.toStandardDuration().getMillis();
      if (mergeMaxGapMillis <= 0) {
        LOG.warn("Alert {} has invalid mergeMaxGap: {}", alert.getId(), mergeMaxGapMillis);
        continue;
      }
      final Date w = aa.getAnomalyCompletionWatermark();
      final long w_next = w != null
          ? Math.max(w.getTime(), alert.getLastTimestamp() - mergeMaxGapMillis)
          : initialWatermark(aa, alert, sg);
      aa.setAnomalyCompletionWatermark(new Timestamp(w_next));
    }
    subscriptionGroupManager.save(sg);
  }

  @VisibleForTesting
  protected void updateWatermarks(final SubscriptionGroupDTO sg,
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
