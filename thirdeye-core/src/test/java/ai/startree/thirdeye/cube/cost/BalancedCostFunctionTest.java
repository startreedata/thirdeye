/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.cost;

import static ai.startree.thirdeye.cube.cost.BalancedCostFunction.CHANGE_CONTRIBUTION_THRESHOLD_PARAM;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

public class BalancedCostFunctionTest {

  @Test
  public void testCreate() {
    double expectedThreshold = 3.54;
    Map<String, String> params = new HashMap<>();
    params.put(CHANGE_CONTRIBUTION_THRESHOLD_PARAM, Double.toString(expectedThreshold));
    BalancedCostFunction function = new BalancedCostFunction(params);

    assertThat(function.getChangeContributionThreshold()).isEqualTo(expectedThreshold);
  }
}
