/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalySeverity;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.Labeler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A threshold-based labeler for anomaly severity
 *
 * Threshold-based severity labeler, which labels anomalies with severity based on deviation from
 * baseline and duration
 * of the anomalies. It tries to label anomalies from highest to lowest if deviation or duration
 * exceeds the threshold
 */
public class ThresholdSeverityLabeler implements Labeler<SeverityThresholdLabelerSpec> {

  private final static Logger LOG = LoggerFactory.getLogger(ThresholdSeverityLabeler.class);
  // severity map ordered by priority from top to bottom
  private TreeMap<AnomalySeverity, Threshold> severityMap;

  @Override
  public void init(SeverityThresholdLabelerSpec spec) {
    this.severityMap = new TreeMap<>();
    for (String key : spec.getSeverity().keySet()) {
      try {
        AnomalySeverity severity = AnomalySeverity.valueOf(key.toUpperCase());
        Threshold threshold = new Threshold();
        if (spec.getSeverity().get(key).containsKey(SeverityThresholdLabelerSpec.CHANGE_KEY)) {
          threshold.change = (Double) spec.getSeverity()
              .get(key)
              .get(SeverityThresholdLabelerSpec.CHANGE_KEY);
        }
        if (spec.getSeverity().get(key).containsKey(SeverityThresholdLabelerSpec.DURATION_KEY)) {
          try {
            threshold.duration = (Long) spec.getSeverity()
                .get(key)
                .get(SeverityThresholdLabelerSpec.DURATION_KEY);
          } catch (ClassCastException e) {
            threshold.duration = ((Integer) spec.getSeverity().get(key).get(
                SeverityThresholdLabelerSpec.DURATION_KEY))
                .longValue();
          }
        }
        this.severityMap.put(severity, threshold);
      } catch (IllegalArgumentException e) {
        LOG.error("Cannot find valid anomaly severity, so ignoring...", e);
      }
    }
  }

  @Override
  public void init(SeverityThresholdLabelerSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
  }

  @Override
  public Map<MergedAnomalyResultDTO, AnomalySeverity> label(
      List<MergedAnomalyResultDTO> anomalies) {
    Map<MergedAnomalyResultDTO, AnomalySeverity> res = new HashMap<>();
    for (MergedAnomalyResultDTO anomaly : anomalies) {
      double currVal = anomaly.getAvgCurrentVal();
      double baseVal = anomaly.getAvgBaselineVal();
      if (Double.isNaN(currVal) || Double.isNaN(baseVal)) {
        LOG.warn("Unable to label anomaly for detection {} from {} to {}, so skipping labeling...",
            anomaly.getDetectionConfigId(), anomaly.getStartTime(), anomaly.getEndTime());
        continue;
      }
      double deviation = Math.abs(currVal - baseVal) / baseVal;
      long duration = anomaly.getEndTime() - anomaly.getStartTime();
      for (Map.Entry<AnomalySeverity, Threshold> entry : severityMap.entrySet()) {
        if (deviation >= entry.getValue().change || duration >= entry.getValue().duration) {
          res.put(anomaly, entry.getKey());
          break;
        }
      }
    }
    return res;
  }

  public static class Threshold {

    public double change;
    public long duration;

    public Threshold() {
      this.change = Double.MAX_VALUE;
      this.duration = Long.MAX_VALUE;
    }

    public Threshold(double change, long duration) {
      this.change = change;
      this.duration = duration;
    }
  }
}
