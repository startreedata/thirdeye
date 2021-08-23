package org.apache.pinot.thirdeye.task.runner;

import static org.apache.pinot.thirdeye.detection.algorithm.MergeWrapper.PROP_GROUP_KEY;
import static org.apache.pinot.thirdeye.detection.algorithm.MergeWrapper.copyAnomalyInfo;
import static org.apache.pinot.thirdeye.detection.wrapper.BaselineFillingMergeWrapper.isExistingAnomaly;
import static org.apache.pinot.thirdeye.detection.wrapper.ChildKeepingMergeWrapper.PROP_PATTERN_KEY;

import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.detection.algorithm.MergeWrapper;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.model.AnomalySlice;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnomalyMerger {

  private final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DataProvider dataProvider;

  @Inject
  public AnomalyMerger(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final DataProvider dataProvider) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.dataProvider = dataProvider;
  }

  public void mergeAndSave(final DetectionPipelineTaskInfo taskInfo,
      final AlertDTO alert,
      final List<MergedAnomalyResultDTO> anomalies) {
    if (anomalies.isEmpty()) {
      return;
    }

    final List<MergedAnomalyResultDTO> existingAnomalies = retrieveAnomaliesFromDatabase(
        taskInfo,
        alert,
        anomalies);

    final List<MergedAnomalyResultDTO> generatedAndExistingAnomalies = new ArrayList<>();
    generatedAndExistingAnomalies.addAll(anomalies);
    generatedAndExistingAnomalies.addAll(existingAnomalies);

    final List<MergedAnomalyResultDTO> mergedAnomalies = merge(alert,
        generatedAndExistingAnomalies);

    for (final MergedAnomalyResultDTO mergedAnomalyResultDTO : mergedAnomalies) {
      mergedAnomalyResultManager.save(mergedAnomalyResultDTO);
      if (mergedAnomalyResultDTO.getId() == null) {
        LOG.error("Failed to store anomaly: {}", mergedAnomalyResultDTO);
      }
    }
  }

  /**
   * Copied from {@link org.apache.pinot.thirdeye.detection.wrapper.ChildKeepingMergeWrapper}
   * The Wrapper code is legacy and will be subsequently deleted
   *
   * @param alert alert DTO
   * @param anomalies list of generated anomalies
   * @return merged list of anomalies
   */
  private List<MergedAnomalyResultDTO> merge(final AlertDTO alert,
      final Collection<MergedAnomalyResultDTO> anomalies) {
    final long maxGap = getMaxGap(alert);
    final long maxDuration = MapUtils
        .getLongValue(alert.getProperties(), "maxDuration", TimeUnit.DAYS.toMillis(7));

    final List<MergedAnomalyResultDTO> input = new ArrayList<>(anomalies);
    final Map<Long, MergedAnomalyResultDTO> existingParentAnomalies = new HashMap<>();
    for (final MergedAnomalyResultDTO anomaly : input) {
      if (anomaly.getId() != null && !anomaly.getChildren().isEmpty()) {
        existingParentAnomalies
            .put(anomaly.getId(), copyAnomalyInfo(anomaly, new MergedAnomalyResultDTO()));
      }
    }

    input.sort(MergeWrapper.COMPARATOR);

    final List<MergedAnomalyResultDTO> output = new ArrayList<>();

    final Map<MergeWrapper.AnomalyKey, MergedAnomalyResultDTO> parents = new HashMap<>();
    for (final MergedAnomalyResultDTO anomaly : input) {
      if (anomaly.isChild()) {
        continue;
      }

      // Prevent merging of grouped anomalies
      String groupKey = "";
      if (anomaly.getProperties().containsKey(PROP_GROUP_KEY)) {
        groupKey = anomaly.getProperties().get(PROP_GROUP_KEY);
      }
      String patternKey = "";
      if (anomaly.getProperties().containsKey(PROP_PATTERN_KEY)) {
        patternKey = anomaly.getProperties().get(PROP_PATTERN_KEY);
      } else if (!Double.isNaN(anomaly.getAvgBaselineVal()) && !Double
          .isNaN(anomaly.getAvgCurrentVal())) {
        patternKey = (anomaly.getAvgCurrentVal() > anomaly.getAvgBaselineVal()) ? "UP" : "DOWN";
      }
      final MergeWrapper.AnomalyKey key =
          new MergeWrapper.AnomalyKey(anomaly.getMetric(), anomaly.getCollection(),
              anomaly.getDimensions(),
              StringUtils.join(Arrays.asList(groupKey, patternKey), ","), "", anomaly.getType());
      final MergedAnomalyResultDTO parent = parents.get(key);

      if (parent == null || anomaly.getStartTime() - parent.getEndTime() > maxGap) {
        // no parent, too far away
        parents.put(key, anomaly);
        output.add(anomaly);
      } else if (anomaly.getEndTime() <= parent.getEndTime()
          || anomaly.getEndTime() - parent.getStartTime() <= maxDuration) {
        // fully merge into existing
        if (parent.getChildren().isEmpty()) {
          parent.getChildren().add(copyAnomalyInfo(parent, new MergedAnomalyResultDTO()));
        }
        parent.setEndTime(Math.max(parent.getEndTime(), anomaly.getEndTime()));

        // merge the anomaly's properties into parent
        ThirdEyeUtils.mergeAnomalyProperties(parent.getProperties(), anomaly.getProperties());
        // merge the anomaly severity
        if (parent.getSeverityLabel().compareTo(anomaly.getSeverityLabel()) > 0) {
          // set the highest severity
          parent.setSeverityLabel(anomaly.getSeverityLabel());
        }
        if (anomaly.getChildren().isEmpty()) {
          parent.getChildren().add(anomaly);
        } else {
          parent.getChildren().addAll(anomaly.getChildren());
        }
      } else {
        // partially overlap but potential merged anomaly is beyond max duration or merge not possible, do not merge
        parents.put(key, anomaly);
        output.add(anomaly);
      }
    }

    // Refill current and baseline values for qualified parent anomalies
    // Ignore filling baselines for exiting parent anomalies and grouped anomalies
    final Collection<MergedAnomalyResultDTO> parentAnomalies = Collections2.filter(output,
        mergedAnomaly -> mergedAnomaly != null && !mergedAnomaly.getChildren().isEmpty()
            && !isExistingAnomaly(
            existingParentAnomalies, mergedAnomaly) && !StringUtils
            .isBlank(mergedAnomaly.getMetricUrn()));
//    super.fillCurrentAndBaselineValue(new ArrayList<>(parentAnomalies));
    return output;
  }

  private long getMaxGap(final AlertDTO alert) {
    return MapUtils
        .getLongValue(alert.getProperties(), "maxGap", TimeUnit.HOURS.toMillis(2));
  }

  protected List<MergedAnomalyResultDTO> retrieveAnomaliesFromDatabase(
      final DetectionPipelineTaskInfo taskInfo,
      final AlertDTO alert, final List<MergedAnomalyResultDTO> anomalies) {
    final long minTime = anomalies.stream()
        .map(MergedAnomalyResultDTO::getStartTime)
        .mapToLong(e -> e)
        .min()
        .orElse(taskInfo.getStart());

    final long maxTime = anomalies.stream()
        .map(MergedAnomalyResultDTO::getEndTime)
        .mapToLong(e -> e)
        .max()
        .orElse(taskInfo.getEnd());

    final long maxGap = getMaxGap(alert);
    final AnomalySlice effectiveSlice = new AnomalySlice()
        .withDetectionId(alert.getId())
        .withStart(minTime - maxGap - 1)
        .withEnd(maxTime + maxGap + 1);

    final Collection<MergedAnomalyResultDTO> existingAnomalies = dataProvider
        .fetchAnomalies(Collections.singleton(effectiveSlice))
        .get(effectiveSlice);
    return new ArrayList<>(existingAnomalies);
  }
}
