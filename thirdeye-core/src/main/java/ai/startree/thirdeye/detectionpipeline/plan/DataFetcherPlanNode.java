/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.plan;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.operator.DataFetcherOperator;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class DataFetcherPlanNode extends DetectionPipelinePlanNode {

  private DataSourceCache dataSourceCache = null;

  public DataFetcherPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    this.dataSourceCache = (DataSourceCache) planNodeContext.getProperties()
        .get(PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY);
  }

  @Override
  public String getType() {
    return "DataFetcher";
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    dataFetcherOperator.init(new OperatorContext()
        .setStartTime(this.startTime)
        .setEndTime(this.endTime)
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.of(PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY, dataSourceCache))
    );
    return dataFetcherOperator;
  }
}
