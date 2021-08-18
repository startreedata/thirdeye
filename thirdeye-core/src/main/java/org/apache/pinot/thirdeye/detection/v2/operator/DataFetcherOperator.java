package org.apache.pinot.thirdeye.detection.v2.operator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;

import java.util.Map;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher;
import org.apache.pinot.thirdeye.detection.v2.spec.DataFetcherSpec;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.DataFetcher;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class DataFetcherOperator extends DetectionPipelineOperator {

  private DataSourceCache dataSourceCache;

  public DataFetcherOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    this.dataSourceCache = (DataSourceCache) context.getProperties()
        .get(DATA_SOURCE_CACHE_REF_KEY);
    super.init(context);
    for (OutputBean outputBean : context.getPlanNode().getOutputs()) {
      outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
    }
  }

  @Override
  protected BaseComponent createComponent() {
    final Map<String, Object> componentSpec = getComponentSpec(planNode.getParams());
    final DataFetcherSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpec, DataFetcherSpec.class),
        "Unable to construct DataFetcherSpec");
    spec.setDataSourceCache(dataSourceCache);

    final GenericDataFetcher genericDataFetcher = new GenericDataFetcher();
    genericDataFetcher.init(spec);

    return genericDataFetcher;
  }

  @Override
  public void execute() throws Exception {
    if (component instanceof DataFetcher) {
      final DataFetcher fetcher = (DataFetcher) component;
      final DataTable dataTable = fetcher.getDataTable();

      checkArgument(outputKeyMap.size() == 1,
          "Only 1 output node is currently supported");
      resultMap.put(outputKeyMap.values().iterator().next(), dataTable);
    }
  }

  @Override
  public String getOperatorName() {
    return "DataFetcherOperator";
  }
}
