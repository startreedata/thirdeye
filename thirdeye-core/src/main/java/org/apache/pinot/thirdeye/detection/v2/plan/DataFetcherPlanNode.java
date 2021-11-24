package org.apache.pinot.thirdeye.detection.v2.plan;

import static org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.v2.operator.DataFetcherOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class DataFetcherPlanNode extends DetectionPipelinePlanNode {

  private DataSourceCache dataSourceCache = null;

  public DataFetcherPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    this.dataSourceCache = (DataSourceCache) planNodeContext.getProperties()
        .get(DATA_SOURCE_CACHE_REF_KEY);
  }

  @Override
  void setNestedProperties(final Map<String, Object> properties) {
  }

  @Override
  public String getType() {
    return "DataFetcher";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator run() throws Exception {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    dataFetcherOperator.init(new OperatorContext()
        .setStartTime(String.valueOf(this.startTime))
        .setEndTime(String.valueOf(this.endTime))
        .setTimeFormat(getParams().getOrDefault("timeFormat", OperatorContext.DEFAULT_TIME_FORMAT).toString())
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY, dataSourceCache))
    );
    return dataFetcherOperator;
  }
}
