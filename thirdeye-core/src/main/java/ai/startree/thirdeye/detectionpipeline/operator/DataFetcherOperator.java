/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.operator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertEvaluator;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.components.GenericDataFetcher;
import ai.startree.thirdeye.detectionpipeline.plan.PlanNodeFactory;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.TimeseriesFilter;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;

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

    final DataSourceCache dataSourceCache = (DataSourceCache) context.getProperties()
        .get(PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY);
    dataFetcher = createDataFetcher(planNode.getParams(), dataSourceCache);
  }

  protected DataFetcher<DataFetcherSpec> createDataFetcher(final Map<String, Object> params,
      final DataSourceCache dataSourceCache) {
    final Map<String, Object> componentSpec = getComponentSpec(params);
    final DataFetcherSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpec, DataFetcherSpec.class),
        "Unable to construct DataFetcherSpec");
    spec.setDataSourceCache(dataSourceCache);
    @SuppressWarnings("unchecked") final List<TimeseriesFilter> timeseriesFilters =
        (List<TimeseriesFilter>) params.getOrDefault(AlertEvaluator.EVALUATION_FILTERS_KEY, List.of());
    spec.setTimeseriesFilters(timeseriesFilters);

    final GenericDataFetcher genericDataFetcher = new GenericDataFetcher();
    genericDataFetcher.init(spec);

    return genericDataFetcher;
  }

  @Override
  public void execute() throws Exception {
    final Interval detectionInterval = new Interval(startTime, endTime);
    final DataTable dataTable = dataFetcher.getDataTable(detectionInterval);
    resultMap.put(outputKeyMap.values().iterator().next(), dataTable);
  }

  @Override
  public String getOperatorName() {
    return "DataFetcherOperator";
  }

  public DataFetcher<DataFetcherSpec> getDataFetcher() {
    return dataFetcher;
  }
}
