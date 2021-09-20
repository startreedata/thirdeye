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

package org.apache.pinot.thirdeye.detection.alert.feed;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.detection.alert.AnomalyFetcherFactory;
import org.apache.pinot.thirdeye.detection.alert.fetcher.AnomalyFetcher;
import org.apache.pinot.thirdeye.detection.alert.fetcher.BaseAnomalyFetcher;
import org.apache.pinot.thirdeye.detection.anomaly.alert.util.AlertFilterHelper;
import org.apache.pinot.thirdeye.detection.detector.email.filter.AlertFilter;
import org.apache.pinot.thirdeye.detection.detector.email.filter.AlertFilterFactory;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertSnapshotManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedConfig;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFetcherConfig;
import org.apache.pinot.thirdeye.spi.detection.AnomalyNotifiedStatus;
import org.apache.pinot.thirdeye.spi.detection.AnomalySource;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnionAnomalyFeed implements AnomalyFeed {

  public static final TimeGranularity EXPIRE_TIME = TimeGranularity.fromString("7_DAYS");
  private static final Logger LOG = LoggerFactory.getLogger(UnionAnomalyFeed.class);

  private final AlertSnapshotManager alertSnapshotManager;
  private final List<AnomalyFetcher> anomalyFetchers;
  private final List<AlertFilter> alertCandidatesFilters;
  private final DateTime alertTime;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  private AlertFilterFactory alertFilterFactory;
  private AlertSnapshotDTO alertSnapshot;

  public UnionAnomalyFeed(final AlertSnapshotManager alertSnapshotManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.anomalyFetchers = new ArrayList<>();
    this.alertCandidatesFilters = new ArrayList<>();
    this.alertTime = DateTime.now();
    this.alertSnapshotManager = alertSnapshotManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  @Override
  public void init(AlertFilterFactory alertFilterFactory, AnomalyFeedConfig anomalyFeedConfig) {
    this.alertFilterFactory = alertFilterFactory;
    AnomalySource anomalySourceType = anomalyFeedConfig.getAnomalySourceType();
    String anomalySource = anomalyFeedConfig.getAnomalySource();
    if (anomalyFeedConfig.getAlertSnapshotId() != null) {
      alertSnapshot = alertSnapshotManager.findById(anomalyFeedConfig.getAlertSnapshotId());
    }
    if (alertSnapshot == null) { // null handling
      LOG.error("Alert snapshot is null.");
      throw new NullPointerException("Alert snapshot is null");
    }
    List<AnomalyFetcherConfig> anomalyFetcherSpecs = anomalyFeedConfig.getAnomalyFetcherConfigs();
    List<Map<String, String>> alertFilterSpecs = anomalyFeedConfig.getAlertFilterConfigs();

    for (AnomalyFetcherConfig anomalyFetcherSpec : anomalyFetcherSpecs) {
      AnomalyFetcher anomalyFetcher;
      try {
        anomalyFetcher = AnomalyFetcherFactory.fromClassName(anomalyFetcherSpec.getType());
      } catch (Exception e) {
        LOG.error("Cannot instantiate anomaly fetcher {}", anomalyFetcherSpec.getType(), e);
        continue;
      }

      anomalyFetcherSpec.setAnomalySourceType(anomalySourceType);
      anomalyFetcherSpec.setAnomalySource(anomalySource);
      anomalyFetcher.init(anomalyFetcherSpec);
      anomalyFetchers.add(anomalyFetcher);
    }

    for (Map<String, String> alertFilterSpec : alertFilterSpecs) {
      alertCandidatesFilters.add(alertFilterFactory.fromSpec(alertFilterSpec));
    }
  }

  @Override
  public Collection<MergedAnomalyResultDTO> getAnomalyFeed() {
    Set<MergedAnomalyResultDTO> mergedAnomalyResultSet = new HashSet<>();
    for (AnomalyFetcher anomalyFetcher : anomalyFetchers) {
      mergedAnomalyResultSet.addAll(anomalyFetcher.getAlertCandidates(alertTime, alertSnapshot));
    }

    // If mergedAnomalyResultSet is empty, then no act after that.
    if (mergedAnomalyResultSet.isEmpty()) {
      return mergedAnomalyResultSet;
    }

    List<MergedAnomalyResultDTO> mergedAnomalyResults = new ArrayList<>(mergedAnomalyResultSet);
    mergedAnomalyResults = AlertFilterHelper
        .applyFiltrationRule(mergedAnomalyResults, alertFilterFactory);

    for (AlertFilter alertFilter : alertCandidatesFilters) {
      Iterator<MergedAnomalyResultDTO> mergedAnomalyIterator = mergedAnomalyResults.iterator();
      while (mergedAnomalyIterator.hasNext()) {
        MergedAnomalyResultDTO mergedAnomalyResult = mergedAnomalyIterator.next();
        if (!alertFilter.isQualified(mergedAnomalyResult)) {
          mergedAnomalyIterator.remove();
        }
      }
    }
    return mergedAnomalyResults;
  }

  @Override
  public void updateSnapshot(Collection<MergedAnomalyResultDTO> alertedAnomalies) {
    if (alertSnapshot == null) {
      LOG.error("No alertSnapshot was initiated.");
      throw new NullPointerException("Alert snapshot is null");
    }

    updateSnapshot(alertTime, new ArrayList<>(alertedAnomalies));
    alertSnapshotManager.update(alertSnapshot);
  }

  public void updateSnapshot(DateTime alertTime, List<MergedAnomalyResultDTO> alertedAnomalies) {
    // Set the lastNotifyTime to current time if there is alerted anomalies
    if (alertedAnomalies.size() > 0) {
      alertSnapshot.setLastNotifyTime(alertTime.getMillis());
    }

    Multimap<String, AnomalyNotifiedStatus> snapshot = alertSnapshot.getSnapshot();
    // update snapshots based on anomalies
    for (MergedAnomalyResultDTO anomaly : alertedAnomalies) {
      String snapshotKey = BaseAnomalyFetcher.getSnapshotKey(anomaly);
      AnomalyNotifiedStatus lastNotifyStatus = alertSnapshot.getLatestStatus(snapshot, snapshotKey);
      AnomalyNotifiedStatus statusToBeUpdated = new AnomalyNotifiedStatus(alertTime.getMillis(),
          anomaly.getWeight());
      if (lastNotifyStatus.getLastNotifyTime() > anomaly.getStartTime()) {
        // anomaly is a continuing issue, and the status should be appended to the end of snapshot
        snapshot.put(snapshotKey, statusToBeUpdated);
      } else {
        // anomaly is a new issue, override the status list
        snapshot.removeAll(snapshotKey);
        snapshot.put(snapshotKey, statusToBeUpdated);
      }

      // Set notified flag
      anomaly.setNotified(true);
      mergedAnomalyResultManager.update(anomaly);
    }

    // cleanup stale status in snapshot
    DateTime expiredTime = alertTime.minus(EXPIRE_TIME.toPeriod());
    List<String> keysToBeRemoved = new ArrayList<>();
    for (String snapshotKey : snapshot.keySet()) {
      AnomalyNotifiedStatus latestStatus = alertSnapshot.getLatestStatus(snapshot, snapshotKey);
      if (latestStatus.getLastNotifyTime() < expiredTime.getMillis()) {
        keysToBeRemoved.add(snapshotKey);
      }
    }
    for (String key : keysToBeRemoved) {
      snapshot.removeAll(key);
    }
    alertSnapshot.setSnapshot(snapshot);
  }
}
