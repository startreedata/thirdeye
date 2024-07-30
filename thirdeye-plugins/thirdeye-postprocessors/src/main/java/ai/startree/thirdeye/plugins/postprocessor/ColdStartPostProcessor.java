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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static ai.startree.thirdeye.spi.util.AnomalyUtils.addLabel;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColdStartPostProcessor implements AnomalyPostProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ColdStartPostProcessor.class);

  private static final String NAME = "COLD_START";
  private static final boolean DEFAULT_IGNORE = false;
  private static final Period DEFAULT_COLD_START_PERIOD = Period.ZERO;
  private static final long TIMEOUT = 20_000;

  private final boolean ignore;
  private final Period coldStartPeriod;
  private final String tableName;
  private final @Nullable String namespace;

  private final @NonNull MinMaxTimeLoader minMaxTimeLoader;
  private final @NonNull DatasetConfigManager datasetDao;

  private final String labelName;
  private final @NonNull DataSourceManager dataSourceDao;

  public ColdStartPostProcessor(final ColdStartPostProcessorSpec spec) {
    this.ignore = optional(spec.getIgnore()).orElse(DEFAULT_IGNORE);
    this.coldStartPeriod = isoPeriod(spec.getColdStartPeriod(), DEFAULT_COLD_START_PERIOD);
    this.tableName = requireNonNull(spec.getTableName());

    this.minMaxTimeLoader = requireNonNull(spec.getMinMaxTimeLoader());
    this.datasetDao = requireNonNull(spec.getDatasetConfigManager());
    this.dataSourceDao = requireNonNull(spec.getDataSourceManager());
    this.namespace = spec.getNamespace();

    this.labelName = labelName(this.coldStartPeriod);
  }

  @VisibleForTesting
  protected static String labelName(final Period coldStartPeriod) {
    return coldStartPeriod.toString() + " cold start";
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap) throws Exception {
    final DatasetConfigDTO datasetConfigDTO = datasetDao.findByNameAndNamespaceOrUnsetNamespace(
        tableName, namespace);
    checkArgument(datasetConfigDTO != null,
        "Could not find dataset %s with namespace %s.",
        tableName, namespace);
    final DataSourceDTO dataSourceDTO = dataSourceDao.findUniqueByNameAndNamespace(
        datasetConfigDTO.getDataSource(), datasetConfigDTO.namespace());
    checkState(dataSourceDTO != null, "Could not find datasource %s in namespace %s",
        datasetConfigDTO.getDataSource(), datasetConfigDTO.namespace());

    // don't fail if dataset min is not found - continue with a 0 minDateTime
    final long datasetMinTime = optional(
        minMaxTimeLoader.fetchMinTimeAsync(dataSourceDTO, datasetConfigDTO, null)
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
    final List<AnomalyDTO> anomalies = operatorResult.getAnomalies();
    if (anomalies == null) {
      return;
    }

    for (final AnomalyDTO anomalyResultDTO : anomalies) {
      if (anomalyResultDTO.getStartTime() <= endOfColdStart.getMillis()) {
        final AnomalyLabelDTO newLabel = new AnomalyLabelDTO().setIgnore(ignore).setName(labelName);
        addLabel(anomalyResultDTO, newLabel);
      }
    }
  }

  public static class Factory implements AnomalyPostProcessorFactory {

    @Override
    public String name() {
      return NAME;
    }

    @Override
    public AnomalyPostProcessor build(final Map<String, Object> params,
        final PostProcessingContext context) {
      final ColdStartPostProcessorSpec spec = VANILLA_OBJECT_MAPPER.convertValue(params,
          ColdStartPostProcessorSpec.class);
      spec.setMinMaxTimeLoader(context.getMinMaxTimeLoader());
      spec.setDatasetConfigManager(context.getDatasetConfigManager());
      spec.setDataSourceManager(context.getDataSourceManager());
      spec.setNamespace(context.getNamespace());
      return new ColdStartPostProcessor(spec);
    }
  }
}
