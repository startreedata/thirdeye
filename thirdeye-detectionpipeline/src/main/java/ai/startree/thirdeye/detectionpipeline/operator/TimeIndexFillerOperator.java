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

import ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller;
import ai.startree.thirdeye.detectionpipeline.spec.TimeIndexFillerSpec;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.IndexFiller;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeIndexFillerOperator extends DetectionPipelineOperator {

  private static final Logger LOG = LoggerFactory.getLogger(TimeIndexFillerOperator.class);
  private static final String OPERATOR_NAME = "TimeIndexFillerOperator";

  private IndexFiller<? extends AbstractSpec> timeIndexFiller;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    this.timeIndexFiller = createTimeIndexFiller(optional(planNode.getParams()).map(TemplatableMap::valueMap)
        .orElse(null));

    checkArgument(inputMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 input node.");
    checkArgument(outputKeyMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 output node.");
  }

  @Override
  public void execute() throws Exception {
    final Map<String, DataTable> timeSeriesMap = DetectionUtils.getDataTableMap(inputMap);
    checkArgument(timeSeriesMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 DataTable in input");
    final DataTable dataTable = timeIndexFiller.fillIndex(detectionInterval,
        timeSeriesMap.values().iterator().next());
    resultMap.put(outputKeyMap.values().iterator().next(), dataTable);
  }

  @Override
  public String getOperatorName() {
    return OPERATOR_NAME;
  }

  private IndexFiller<? extends AbstractSpec> createTimeIndexFiller(
      final Map<String, Object> params) {
    final Map<String, Object> componentSpec = getComponentSpec(params);
    final TimeIndexFillerSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpec, TimeIndexFillerSpec.class),
        "Unable to construct TimeIndexFillerSpec");

    final IndexFiller<TimeIndexFillerSpec> timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    return timeIndexFiller;
  }
}
