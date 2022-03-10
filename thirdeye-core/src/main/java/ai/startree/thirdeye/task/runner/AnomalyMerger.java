/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detection.algorithm.AnomalyKey;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnomalyMerger {

  private final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);
  private static final String PROP_PATTERN_KEY = "pattern";
  private static final String PROP_GROUP_KEY = "groupKey";
  private static final Comparator<MergedAnomalyResultDTO> COMPARATOR = (o1, o2) -> {
    // earlier for start time
    int res = Long.compare(o1.getStartTime(), o2.getStartTime());
    if (res != 0) {
      return res;
    }

    // later for end time
    res = Long.compare(o2.getEndTime(), o1.getEndTime());
    if (res != 0) {
      return res;
    }

    // pre-existing
    if (o1.getId() == null && o2.getId() != null) {
      return 1;
    }
    if (o1.getId() != null && o2.getId() == null) {
      return -1;
    }

    // more children
    return -1 * Integer.compare(o1.getChildren().size(), o2.getChildren().size());
  };

  @VisibleForTesting
  protected static final long DEFAULT_MERGE_MAX_GAP = TimeUnit.HOURS.toMillis(2);
  @VisibleForTesting
  protected static final long DEFAULT_ANOMALY_MAX_DURATION = TimeUnit.DAYS.toMillis(7);

  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  public AnomalyMerger(
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  public void mergeAndSave(final AlertDTO alert,
      final List<MergedAnomalyResultDTO> anomalies) {
    if (anomalies.isEmpty()) {
      return;
    }
    final long maxGap = getMaxGap(alert);
    final Long alertId = alert.getId();
    final List<MergedAnomalyResultDTO> existingAnomalies = retrieveRelevantAnomaliesFromDatabase(
        alertId,
        anomalies, maxGap);

    final List<MergedAnomalyResultDTO> sortedRelevantAnomalies = prepareSortedAnomalyList(
        anomalies,
        existingAnomalies);

    final long maxDurationMillis = getMaxDuration(alert);
    final Collection<MergedAnomalyResultDTO> mergedAnomalies = merge(sortedRelevantAnomalies,
        maxGap,
        maxDurationMillis);

    for (final MergedAnomalyResultDTO mergedAnomalyResultDTO : mergedAnomalies) {
      mergedAnomalyResultManager.save(mergedAnomalyResultDTO);
      if (mergedAnomalyResultDTO.getId() == null) {
        LOG.error("Failed to store anomaly: {}", mergedAnomalyResultDTO);
      }
    }
  }

  @VisibleForTesting
  List<MergedAnomalyResultDTO> prepareSortedAnomalyList(
      final List<MergedAnomalyResultDTO> anomalies,
      final List<MergedAnomalyResultDTO> existingAnomalies) {
    final List<MergedAnomalyResultDTO> generatedAndExistingAnomalies = new ArrayList<>();
    generatedAndExistingAnomalies.addAll(anomalies);
    generatedAndExistingAnomalies.addAll(existingAnomalies);

    // prepare a sorted list for processing
    generatedAndExistingAnomalies.sort(COMPARATOR);
    return generatedAndExistingAnomalies;
  }

  /**
   * Merge a list of anomalies given a max gap and a max duration.
   */
  @VisibleForTesting
  Collection<MergedAnomalyResultDTO> merge(final Collection<MergedAnomalyResultDTO> anomalies,
      final long maxGap, final long maxDurationMillis) {
    final Map<AnomalyKey, MergedAnomalyResultDTO> parents = new HashMap<>();
    for (final MergedAnomalyResultDTO anomaly : anomalies) {
      // skip child anomalies. merge their parents instead
      if (anomaly.isChild()) {
        continue;
      }

      // Prevent merging of grouped anomalies
      final AnomalyKey key = createAnomalyKey(anomaly);
      final MergedAnomalyResultDTO parent = parents.get(key);

      if (shouldMerge(parent, anomaly, maxGap, maxDurationMillis)) {
        mergeIntoParent(parent, anomaly);
      } else {
        parents.put(key, anomaly);
      }
    }
    return parents.values();
  }

  private AnomalyKey createAnomalyKey(final MergedAnomalyResultDTO anomaly) {
    final String groupKey = anomaly.getProperties().getOrDefault(PROP_GROUP_KEY, "");
    final String patternKey = getPatternKey(anomaly);
    return new AnomalyKey(anomaly.getMetric(),
        anomaly.getCollection(),
        anomaly.getDimensions(),
        StringUtils.join(Arrays.asList(groupKey, patternKey), ","),
        "",
        anomaly.getType());
  }

  private void mergeIntoParent(final MergedAnomalyResultDTO parent,
      final MergedAnomalyResultDTO child) {
    // fully merge into existing
    final Set<MergedAnomalyResultDTO> children = parent.getChildren();

    // if this parent has no children, then add itself as a child
    if (children.isEmpty()) {
      children.add(copyAnomalyInfo(parent, new MergedAnomalyResultDTO()));
    }
    // Extend the end time to match the child anomaly end time.
    parent.setEndTime(Math.max(parent.getEndTime(), child.getEndTime()));

    // merge the anomaly's properties into parent
    ThirdEyeUtils.mergeAnomalyProperties(parent.getProperties(), child.getProperties());

    // merge the anomaly severity
    if (parent.getSeverityLabel().compareTo(child.getSeverityLabel()) > 0) {
      // set the highest severity
      parent.setSeverityLabel(child.getSeverityLabel());
    }

    // If anomaly is a child anomaly, add itself else add all its children
    if (child.getChildren().isEmpty()) {
      children.add(child);
    } else {
      children.addAll(child.getChildren());
    }
  }

  /**
   * Merge anomalies if
   * - parent exists
   * - parent end time and child start time respects max allowed gap between anomalies
   * - parent anomaly post merge respects maxDuration
   *
   * @param parent parent anomaly
   * @param child child anomaly
   * @return whether they should be merged
   */
  private boolean shouldMerge(
      final MergedAnomalyResultDTO parent,
      final MergedAnomalyResultDTO child,
      final long maxGap,
      final long maxDurationMillis
  ) {

    return parent != null
        && child.getStartTime() - parent.getEndTime() <= maxGap
        && (child.getEndTime() <= parent.getEndTime()
        || child.getEndTime() - parent.getStartTime() <= maxDurationMillis);
  }

  private String getPatternKey(final MergedAnomalyResultDTO anomaly) {
    String patternKey = "";
    if (anomaly.getProperties().containsKey(PROP_PATTERN_KEY)) {
      patternKey = anomaly.getProperties().get(PROP_PATTERN_KEY);
    } else if (!Double.isNaN(anomaly.getAvgBaselineVal()) && !Double
        .isNaN(anomaly.getAvgCurrentVal())) {
      patternKey = (anomaly.getAvgCurrentVal() > anomaly.getAvgBaselineVal()) ? "UP" : "DOWN";
    }
    return patternKey;
  }

  @VisibleForTesting
  protected long getMaxGap(final AlertDTO alert) {
    return MapUtils
        .getLongValue(alert.getProperties(), "maxGap", DEFAULT_MERGE_MAX_GAP);
  }

  private long getMaxDuration(final AlertDTO alert) {
    return MapUtils
        .getLongValue(alert.getProperties(), "maxDuration", DEFAULT_ANOMALY_MAX_DURATION);
  }

  private List<MergedAnomalyResultDTO> retrieveRelevantAnomaliesFromDatabase(
      final Long alertId,
      final List<MergedAnomalyResultDTO> anomalies, final long maxGap) {
    checkArgument(alertId != null, "must be an existing alert");

    final long minTime = anomalies.stream()
        .map(MergedAnomalyResultDTO::getStartTime)
        .mapToLong(e -> e)
        .min()
        .orElseThrow(() -> new RuntimeException(
            "When trying to merge. No startTime in the anomalies. This should not happen."));

    final long maxTime = anomalies.stream()
        .map(MergedAnomalyResultDTO::getEndTime)
        .mapToLong(e -> e)
        .max()
        .orElseThrow(() -> new RuntimeException(
            "When trying to merge. No endTime in the anomalies. This should not happen."));

    long mergeLowerBound = minTime - maxGap - 1;
    long mergeUpperBound = maxTime + maxGap + 1;

    return mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(mergeLowerBound,
        mergeUpperBound,
        alertId);
  }

  public static MergedAnomalyResultDTO copyAnomalyInfo(MergedAnomalyResultDTO from,
      MergedAnomalyResultDTO to) {
    to.setStartTime(from.getStartTime());
    to.setEndTime(from.getEndTime());
    to.setMetric(from.getMetric());
    to.setCollection(from.getCollection());
    to.setDetectionConfigId(from.getDetectionConfigId());
    to.setAnomalyResultSource(from.getAnomalyResultSource());
    to.setAvgBaselineVal(from.getAvgBaselineVal());
    to.setAvgCurrentVal(from.getAvgCurrentVal());
    to.setFeedback(from.getFeedback());
    to.setAnomalyFeedbackId(from.getAnomalyFeedbackId());
    to.setScore(from.getScore());
    to.setWeight(from.getWeight());
    to.setProperties(from.getProperties());
    to.setType(from.getType());
    to.setSeverityLabel(from.getSeverityLabel());
    return to;
  }
}
