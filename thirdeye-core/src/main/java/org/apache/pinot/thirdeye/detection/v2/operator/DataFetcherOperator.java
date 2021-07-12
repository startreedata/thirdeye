package org.apache.pinot.thirdeye.detection.v2.operator;

import static org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;

import java.util.Map;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.v2.spec.DataFetcherSpec;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.PlanNodeBean.OutputBean;
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
  protected AbstractSpec getComponentSpec(Map<String, Object> componentSpecs, String componentKey) {
    final AbstractSpec componentSpec = super.getComponentSpec(componentSpecs, componentKey);
    if (componentSpec instanceof DataFetcherSpec) {
      ((DataFetcherSpec) componentSpec).setDataSourceCache(dataSourceCache);
    }
    return componentSpec;
  }

  @Override
  public void init(final OperatorContext context) {
    this.dataSourceCache = (DataSourceCache) context.getProperties()
        .get(DATA_SOURCE_CACHE_REF_KEY);
    super.init(context);
    for (OutputBean outputBean : context.getDetectionPlanApi().getOutputs()) {
      outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
    }
  }

  @Override
  public void execute() throws Exception {
    for (Object key : this.getComponents().keySet()) {
      final BaseComponent component = this.getComponents().get(key);
      if (component instanceof DataFetcher) {
        final DataFetcher fetcher = (DataFetcher) component;
        final DataTable dataTable = fetcher.getDataTable();
        if (outputKeyMap.containsKey(key)) {
          resultMap.put(outputKeyMap.get(key), dataTable);
        } else {
          resultMap.put(key.toString(), dataTable);
        }
      }
    }
  }

  @Override
  public String getOperatorName() {
    return "DataFetcherOperator";
  }
}
