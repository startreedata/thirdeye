/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_METRIC;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_TIMESTAMP;
import static ai.startree.thirdeye.spi.detection.AnomalyDetector.KEY_CURRENT;
import static ai.startree.thirdeye.spi.util.AnomalyUtils.addLabel;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * By default, the timeseries used is the timeseries contained by the OperatorResult containing the
 * anomalies. It can be overridden by a custom DataTable.
 * This allows use case like:
 * - monitor metric1
 * - ignore if metric2 was out of threshold
 */
public class ThresholdPostProcessor implements AnomalyPostProcessor {

  private static final String NAME = "THRESHOLD";

  private static final boolean DEFAULT_IGNORE = false;
  @VisibleForTesting
  protected static final String DEFAULT_VALUE_NAME = "Value";
  // fixme cyril temporary value. Should be null but the frontend does not support null in some places.
  @VisibleForTesting
  protected static final Double NOT_ACTIVATED_VALUE = -1.;

  // find better system for null values - min=max?
  private final Double min;
  private final Double max;
  private String timestampColum;
  private String valueColumn;

  private final boolean ignore;
  private final String labelName;

  public ThresholdPostProcessor(final ThresholdPostProcessorSpec spec) {
    this.ignore = optional(spec.getIgnore()).orElse(DEFAULT_IGNORE);
    this.min = optional(spec.getMin()).orElse(NOT_ACTIVATED_VALUE);
    this.max = optional(spec.getMax()).orElse(NOT_ACTIVATED_VALUE);
    this.timestampColum = optional(spec.getTimestamp()).orElse(DEFAULT_TIMESTAMP);
    this.valueColumn = optional(spec.getMetric()).orElse(DEFAULT_METRIC);

    final String valueName = optional(spec.getValueName()).orElse(DEFAULT_VALUE_NAME);
    this.labelName = labelName(this.min, this.max, valueName);
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    // get and remove side-input.
    // todo cyril - remove is performed because currently downstream logic (eg toDetectionEvaluationApi) makes a lot of assumptions on what is returned by the root node
    //   it expects size 1 in some places, and AnomalyDetectorResults only in other places
    final OperatorResult thresholdSideInput = resultMap.remove(KEY_CURRENT);

    // short-circuit if no thresholds
    if (!isActivated(min) && !isActivated(max)) {
      return resultMap;
    }

    for (final Entry<String, OperatorResult> entry : resultMap.entrySet()) {
      postProcessResult(entry.getValue(), thresholdSideInput);
    }

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult,
      @Nullable OperatorResult thresholdSideInput) {
    final List<AnomalyDTO> anomalies = operatorResult.getAnomalies();
    if (anomalies == null) {
      return;
    }

    final DataFrame df;
    if (thresholdSideInput == null) {
      timestampColum = COL_TIME;
      valueColumn = COL_CURRENT;
      df = optional(operatorResult.getTimeseries()).map(TimeSeries::getDataFrame)
          .orElseThrow(() -> new IllegalArgumentException(
              "Invalid input. OperatorResult contains anomalies but no timeseries."));
    } else if (thresholdSideInput instanceof DataTable) {
      df = ((DataTable) thresholdSideInput).getDataFrame();
    } else {
      // extract the df from the timeseries - throw error if OperatorResult does not contain a timeseries
      final TimeSeries ts = thresholdSideInput.getTimeseries();
      checkArgument(ts != null,
          "Provided customInput for THRESHOLD does not contain a timeseries.");
      df = ts.getDataFrame();
    }

    final Set<Long> timestampOutOfThresholds = timestampOutOfThresholds(df);

    for (final AnomalyDTO anomalyResultDTO : anomalies) {
      if (timestampOutOfThresholds.contains(anomalyResultDTO.getStartTime())) {
        final AnomalyLabelDTO newLabel = new AnomalyLabelDTO().setIgnore(ignore).setName(labelName);
        addLabel(anomalyResultDTO, newLabel);
      }
    }
  }

  private Set<Long> timestampOutOfThresholds(final DataFrame df) {
    final Set<Long> outOfThreshold = new HashSet<>();
    // note - doing this on the whole dataframe is not efficient could be done between min and max of the anomalies only
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

  public static class Factory implements AnomalyPostProcessorFactory {

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public AnomalyPostProcessor build(final Map<String, Object> params, final PostProcessingContext context) {
      final ThresholdPostProcessorSpec spec = new ObjectMapper().convertValue(params,
          ThresholdPostProcessorSpec.class);
      return new ThresholdPostProcessor(spec);
    }
  }
}
