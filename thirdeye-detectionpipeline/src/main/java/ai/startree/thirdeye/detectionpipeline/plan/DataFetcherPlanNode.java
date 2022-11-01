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
package ai.startree.thirdeye.detectionpipeline.plan;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.operator.DataFetcherOperator;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataFetcherPlanNode extends DetectionPipelinePlanNode {

  private DataSourceCache dataSourceCache = null;
  private DatasetConfigManager datasetDao = null;
  private List<Predicate> predicates = null;

  public DataFetcherPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    this.dataSourceCache = (DataSourceCache) planNodeContext.getProperties()
        .get(Constants.K_DATA_SOURCE_CACHE);
    this.datasetDao = (DatasetConfigManager) Objects.requireNonNull(planNodeContext.getProperties()
        .get(Constants.K_DATASET_MANAGER));
    this.predicates = planNodeContext.getPredicates();
  }

  @Override
  public String getType() {
    return "DataFetcher";
  }

  @Override
  public Map<String, Object> getParams() {
    return optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(null);
  }

  @Override
  public Operator buildOperator() throws Exception {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    dataFetcherOperator.init(createOperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPredicates(predicates)
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.of(
            Constants.K_DATA_SOURCE_CACHE, dataSourceCache,
            Constants.K_DATASET_MANAGER, datasetDao))
    );
    return dataFetcherOperator;
  }
}
