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
package ai.startree.thirdeye.detectionpipeline.operator;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.ApplicationContext;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.components.GenericDataFetcher;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.List;
import java.util.Map;

public class DataFetcherOperator extends DetectionPipelineOperator {

  private DataFetcher<DataFetcherSpec> dataFetcher;

  public DataFetcherOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    for (final OutputBean outputBean : context.getPlanNode().getOutputs()) {
      outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
    }
    checkArgument(outputKeyMap.size() <= 1,
        "Max 1 output node is currently supported");

    final ApplicationContext applicationContext = context.getPlanNodeContext()
        .getApplicationContext();
    final DataSourceCache dataSourceCache = requireNonNull(applicationContext.getDataSourceCache());
    final DatasetConfigManager datasetDao = requireNonNull(
        applicationContext.getDatasetConfigManager());
    final Map<String, Object> params = optional(planNode.getParams()).map(TemplatableMap::valueMap)
        .orElse(null);
    final List<Predicate> predicates = optional(context.getPredicates()).orElse(List.of());
    dataFetcher = createDataFetcher(params, dataSourceCache, datasetDao, predicates);
  }

  protected DataFetcher<DataFetcherSpec> createDataFetcher(final Map<String, Object> params,
      final DataSourceCache dataSourceCache, final DatasetConfigManager datasetDao,
      final List<Predicate> predicates) {
    final Map<String, Object> componentSpec = getComponentSpec(params);
    final DataFetcherSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpec, DataFetcherSpec.class),
        "Unable to construct DataFetcherSpec");
    spec.setDataSourceCache(dataSourceCache);
    spec.setDatasetDao(datasetDao);
    spec.setTimeseriesFilters(predicates);

    final GenericDataFetcher genericDataFetcher = new GenericDataFetcher();
    genericDataFetcher.init(spec);

    return genericDataFetcher;
  }

  @Override
  public void execute() throws Exception {
    final DataTable dataTable = dataFetcher.getDataTable(detectionInterval);
    resultMap.put(outputKeyMap.values().iterator().next(),
        dataTable);
  }

  @Override
  public String getOperatorName() {
    return "DataFetcherOperator";
  }

  public DataFetcher<DataFetcherSpec> getDataFetcher() {
    return dataFetcher;
  }
}
