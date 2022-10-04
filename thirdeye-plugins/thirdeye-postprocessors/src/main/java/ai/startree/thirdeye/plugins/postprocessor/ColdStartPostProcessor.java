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

import static ai.startree.thirdeye.plugins.postprocessor.LabelUtils.addLabel;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

public class ColdStartPostProcessor implements AnomalyPostProcessor<ColdStartPostProcessorSpec> {

  private static final boolean DEFAULT_IGNORE = false;
  private static final Period DEFAULT_COLD_START_PERIOD = Period.ZERO;
  private static final long TIMEOUT = 20_000;

  private boolean ignore;
  private Period coldStartPeriod;
  private String tableName;

  private MinMaxTimeLoader minMaxTimeLoader;
  private DatasetConfigManager datasetDao;

  private String labelName;

  @Override
  public void init(final ColdStartPostProcessorSpec spec) {
    this.ignore = optional(spec.getIgnore()).orElse(DEFAULT_IGNORE);
    this.coldStartPeriod = isoPeriod(spec.getColdStartPeriod(), DEFAULT_COLD_START_PERIOD);
    this.tableName = Objects.requireNonNull(spec.getTableName());

    this.minMaxTimeLoader = spec.getMinMaxTimeLoader();
    this.datasetDao = spec.getDatasetConfigManager();

    this.labelName = labelName(this.coldStartPeriod);
  }

  @VisibleForTesting
  protected static String labelName(final Period coldStartPeriod) {
    return coldStartPeriod.toString() + " cold start";
  }

  @Override
  public Class<ColdStartPostProcessorSpec> specClass() {
    return ColdStartPostProcessorSpec.class;
  }

  @Override
  public String name() {
    return "ColdStart";
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    final DatasetConfigDTO datasetConfigDTO = Objects.requireNonNull(datasetDao.findByDataset(
        tableName), "Could not find dataset " + tableName);
    // don't fail if dataset min is not found - continue with a 0 minDateTime
    final long datasetMinTime = optional(minMaxTimeLoader.fetchMinTimeAsync(datasetConfigDTO, null)
        .get(TIMEOUT, TimeUnit.MILLISECONDS)).orElse(0L);
    final DateTime datasetMinDateTime = new DateTime(datasetMinTime,
        detectionInterval.getChronology());
    final DateTime endOfColdStart = datasetMinDateTime.plus(coldStartPeriod);

    for (final OperatorResult operatorResult : resultMap.values()) {
      postProcessResult(operatorResult, endOfColdStart);
    }

    return resultMap;
  }

  private void postProcessResult(final OperatorResult operatorResult,
      final DateTime endOfColdStart) {
    // todo cyril default implementation of getAnomalies throws error - obliged to catch here - change default implem?
    try {
      final List<MergedAnomalyResultDTO> anomalies = operatorResult.getAnomalies();
      for (final MergedAnomalyResultDTO anomalyResultDTO : anomalies) {
        if (anomalyResultDTO.getStartTime() <= endOfColdStart.getMillis()) {
          final AnomalyLabelDTO newLabel = new AnomalyLabelDTO().setIgnore(ignore)
              .setName(labelName);
          addLabel(anomalyResultDTO, newLabel);
        }
      }
    } catch (final UnsupportedOperationException e) {
      // no anomalies - continue
    }
  }
}
