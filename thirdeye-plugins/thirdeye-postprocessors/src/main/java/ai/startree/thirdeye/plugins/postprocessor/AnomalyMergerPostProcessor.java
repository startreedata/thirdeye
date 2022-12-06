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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnomalyMergerPostProcessor implements AnomalyPostProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyMergerPostProcessor.class);

  @VisibleForTesting
  protected static final Period DEFAULT_MERGE_MAX_GAP = Period.hours(2);
  @VisibleForTesting
  protected static final Period DEFAULT_ANOMALY_MAX_DURATION = Period.days(7);

  private static final String NAME = "COLD_START";

  private Period mergeMaxGap;
  private Period mergeMaxDuration;
  private final Long alertId;
  private DetectionPipelineUsage usage;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  // obtained at runtime
  private Chronology chronology;

  public AnomalyMergerPostProcessor(final AnomalyMergerSpec spec) {
    this.mergeMaxGap = isoPeriod(spec.getMergeMaxGap(), DEFAULT_MERGE_MAX_GAP);
    this.mergeMaxDuration = isoPeriod(spec.getMergeMaxDuration(), DEFAULT_ANOMALY_MAX_DURATION);
    this.alertId = spec.getAlertId();
    this.usage = spec.getUsage();

    this.mergedAnomalyResultManager = spec.getMergedAnomalyResultManager();
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    chronology = detectionInterval.getChronology();

    for (final OperatorResult operatorResult : resultMap.values()) {
      postProcessResult(operatorResult);
    }

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult) {

  }

  public static class Factory implements AnomalyPostProcessorFactory {

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public AnomalyPostProcessor build(final Map<String, Object> params, final PostProcessingContext context) {
      final AnomalyMergerSpec spec = new ObjectMapper().convertValue(params, AnomalyMergerSpec.class);
      spec.setMergedAnomalyResultManager(context.getMergedAnomalyResultManager());
      spec.setAlertId(context.getAlertId());
      spec.setUsage(context.getUsage());

      return new AnomalyMergerPostProcessor(spec);
    }
  }
}
