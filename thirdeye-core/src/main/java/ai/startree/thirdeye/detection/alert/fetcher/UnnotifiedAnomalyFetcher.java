/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.fetcher;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyNotifiedStatus;
import ai.startree.thirdeye.spi.detection.AnomalySource;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import com.google.common.collect.Multimap;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnnotifiedAnomalyFetcher extends BaseAnomalyFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(UnnotifiedAnomalyFetcher.class);

  public static final String MAXIMUM_ANOMALY_LOOK_BACK_LENGTH = "maxAnomalyLookBack";

  public static final String DEFAULT_MAXIMUM_ANOMALY_LOOK_BACK_LENGTH = "1_DAYS";

  public UnnotifiedAnomalyFetcher(final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(mergedAnomalyResultManager);
  }

  /**
   * Fetch the list of un-notified anomalies, whose create time is after last notify time
   *
   * @param current the current DateTime
   * @param alertSnapShot the snapshot of the AnomalyFeed
   * @return a list of un-notified anomalies
   */
  @Override
  public Collection<MergedAnomalyResultDTO> getAlertCandidates(DateTime current,
      AlertSnapshotDTO alertSnapShot) {
    if (!this.active) {
      LOG.warn("UnnotifiedAnomalyFetcher is not active for fetching anomalies");
      return Collections.emptyList();
    }
    if (StringUtils.isBlank(this.anomalyFetcherConfig.getAnomalySource()) ||
        this.anomalyFetcherConfig.getAnomalySourceType() == null) {
      LOG.error("No entry of {} or {} in the AnomalyFetcherConfig", ANOMALY_SOURCE_TYPE,
          ANOMALY_SOURCE);
      return Collections.emptyList();
    }

    Period maxAnomalyLookBack = TimeGranularity.fromString(
        this.properties.getProperty(MAXIMUM_ANOMALY_LOOK_BACK_LENGTH,
            DEFAULT_MAXIMUM_ANOMALY_LOOK_BACK_LENGTH)).toPeriod();

    // Fetch anomalies who are created MAXIMUM_ANOMALY_LOOK_BACK_LENGTH ago
    AnomalySource anomalySourceType = anomalyFetcherConfig.getAnomalySourceType();
    String anomalySource = anomalyFetcherConfig.getAnomalySource();
    Predicate predicate = Predicate.AND(anomalySourceType.getPredicate(anomalySource),
        Predicate.GE("createTime",
            new Timestamp(current.minus(maxAnomalyLookBack).getMillis())));
    Set<MergedAnomalyResultDTO> alertCandidates = new HashSet<>(
        mergedAnomalyResultManager.findByPredicate(predicate));

    // parse snapshot to a map, getting the last notified time of given metric::dimension pair
    Multimap<String, AnomalyNotifiedStatus> snapshot = alertSnapShot.getSnapshot();
    if (snapshot.size() == 0) {
      return new ArrayList<>(alertCandidates);
    }

    // filter out alert candidates by snapshot
    Iterator<MergedAnomalyResultDTO> iterator = alertCandidates.iterator();
    while (iterator.hasNext()) {
      MergedAnomalyResultDTO mergedAnomaly = iterator.next();
      String snapshotKey = BaseAnomalyFetcher.getSnapshotKey(mergedAnomaly);

      if (snapshot.containsKey(snapshotKey)) {
        // If the mergedAnomaly's create time is before last notify time, discard
        long lastNotifyTime = alertSnapShot.getLatestStatus(snapshot, snapshotKey)
            .getLastNotifyTime();
        if (mergedAnomaly.getCreatedTime() < lastNotifyTime) {
          iterator.remove();
        }
      }
    }

    return alertCandidates;
  }
}
