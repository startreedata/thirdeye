/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.operator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller;
import ai.startree.thirdeye.detectionpipeline.spec.TimeIndexFillerSpec;
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
    this.timeIndexFiller = createTimeIndexFiller(planNode.getParams());

    checkArgument(inputMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 input node.");
    checkArgument(outputKeyMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 output node.");
  }

  @Override
  public void execute() throws Exception {
    final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
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
