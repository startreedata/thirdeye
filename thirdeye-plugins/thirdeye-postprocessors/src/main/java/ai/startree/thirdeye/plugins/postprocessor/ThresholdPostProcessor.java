package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.plugins.postprocessor.LabelUtils.addLabel;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_METRIC;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_TIMESTAMP;
import static ai.startree.thirdeye.spi.detection.AnomalyDetector.KEY_CURRENT;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;

/**
 * Apply a label on anomalies that have their value out of a threshold.
 *
 * Does not look at the value inside the anomaly object. Directly looks at the timeseries value.
 * By default, the timeseries is the timeseries used for detection,
 * but it can be overridden by a custom DataTable.
 * This allows use case like:
 * - monitor metric1
 * - ignore if metric2 was out of threshold
 */
public class ThresholdPostProcessor implements AnomalyPostProcessor<ThresholdPostProcessorSpec> {

  public static final String NAME = "THRESHOLD";

  private static final boolean DEFAULT_IGNORE = false;
  @VisibleForTesting
  protected static final String DEFAULT_VALUE_NAME = "Value";
  // fixme cyril temporary value. Should be null but the frontend does not support null in some places.
  @VisibleForTesting
  protected static final Double NOT_ACTIVATED_VALUE = -1.;

  // find better system for null values - min=max?
  private Double min;
  private Double max;
  private String timestampColum;
  private String valueColumn;

  private boolean ignore;
  private String labelName;

  @Override
  public void init(final ThresholdPostProcessorSpec spec) {
    this.ignore = optional(spec.getIgnore()).orElse(DEFAULT_IGNORE);
    this.min = optional(spec.getMin()).orElse(NOT_ACTIVATED_VALUE);
    this.max = optional(spec.getMax()).orElse(NOT_ACTIVATED_VALUE);
    this.timestampColum = optional(spec.getTimestamp()).orElse(DEFAULT_TIMESTAMP);
    this.valueColumn = optional(spec.getMetric()).orElse(DEFAULT_METRIC);

    final String valueName = optional(spec.getValueName()).orElse(DEFAULT_VALUE_NAME);
    this.labelName = labelName(this.min, this.max, valueName);
  }

  @Override
  public Class<ThresholdPostProcessorSpec> specClass() {
    return ThresholdPostProcessorSpec.class;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    // short-circuit if no thresholds
    if (!isActivated(min) && !isActivated(max)) {
      return resultMap;
    }
    final @Nullable DataTable customThresholdTable = (DataTable) resultMap.get(KEY_CURRENT);

    for (final Entry<String, OperatorResult> entry : resultMap.entrySet()) {
      if (entry.getKey().equals(KEY_CURRENT)) {
        continue;
      }
      postProcessResult(entry.getValue(), customThresholdTable);
    }

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult,
      @Nullable DataTable customThresholdTable) {
    final List<MergedAnomalyResultDTO> anomalies;
    // todo cyril default implementation of getAnomalies throws error - obliged to catch here - change default implem?
    try {
      anomalies = operatorResult.getAnomalies();
    } catch (final UnsupportedOperationException e) {
      // no anomalies - do nothing
      return;
    }

    final DataFrame df;
    if (customThresholdTable == null) {
      timestampColum = COL_TIME;
      valueColumn = COL_VALUE;
      df = optional(operatorResult.getTimeseries()).map(TimeSeries::getDataFrame)
          .orElseThrow(() -> new IllegalArgumentException(
              "Invalid input. OperatorResult contains anomalies but no timeseries."));
    } else {
      df = customThresholdTable.getDataFrame();
    }

    final Set<Long> timestampOutOfThresholds = timestampOutOfThresholds(df);

    for (final MergedAnomalyResultDTO anomalyResultDTO : anomalies) {
      if (timestampOutOfThresholds.contains(anomalyResultDTO.getStartTime())) {
        final AnomalyLabelDTO newLabel = new AnomalyLabelDTO().setIgnore(ignore).setName(labelName);
        addLabel(anomalyResultDTO, newLabel);
      }
    }
  }

  private Set<Long> timestampOutOfThresholds(final DataFrame df) {
    final Set<Long> outOfThreshold = new HashSet<>();
    for (int i = 0; i < df.size(); i++) {
      if (isOutOfThreshold(df.getDouble(valueColumn, i))) {
        outOfThreshold.add(df.getLong(timestampColum, i));
      }
    }
    return outOfThreshold;
  }

  private boolean isOutOfThreshold(final double value) {
    return (isActivated(min) && value <= min) || (isActivated(max) && value >= max);
  }

  private static boolean isActivated(final Double extremum) {
    return !NOT_ACTIVATED_VALUE.equals(extremum);
  }

  @VisibleForTesting
  protected static String labelName(final Double min, final Double max, final String valueName) {
    final boolean minIsActivated = isActivated(min);
    final boolean maxIsActivated = isActivated(max);
    if (minIsActivated && maxIsActivated) {
      return String.format("%s outside [%s, %s] range", valueName, min, max);
    }
    if (minIsActivated) {
      return valueName + " smaller than " + min;
    }
    if (maxIsActivated) {
      return valueName + " bigger than " + max;
    }

    return "";
  }
}
