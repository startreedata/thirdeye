package org.apache.pinot.thirdeye.detection.v2.operator;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

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

public class DataFetcherOperator extends DetectionPipelineOperator<DataTable> {

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
  protected BaseComponent createComponent(final Map<String, Object> componentSpecMap) {
    final DataFetcherSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpecMap, DataFetcherSpec.class),
        "Unable to construct DataFetcherSpec");
    spec.setDataSourceCache(dataSourceCache);

    final GenericDataFetcher genericDataFetcher = new GenericDataFetcher();
    genericDataFetcher.init(spec);

    return genericDataFetcher;
  }

  @Override
  public void execute() throws Exception {
    for (String key : this.getComponents().keySet()) {
      final BaseComponent component = this.getComponents().get(key);
      if (component instanceof DataFetcher) {
        final DataFetcher fetcher = (DataFetcher) component;
        final DataTable dataTable = fetcher.getDataTable();
        resultMap.put(optional(outputKeyMap.get(key)).orElse(key), dataTable);
      }
    }
  }

  @Override
  public String getOperatorName() {
    return "DataFetcherOperator";
  }
}
