/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.rca.contributors.cube.cost;

import static ai.startree.thirdeye.plugins.rca.contributors.cube.cost.BalancedCostFunction.CHANGE_CONTRIBUTION_THRESHOLD_PARAM;
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

  @Test
  public void testCorrectFloatingPointArithmeticError() {
    final double outputForaboveZero = BalancedCostFunction.correctFloatingPointArithmeticError(
        0.000001);
    assertThat(outputForaboveZero).isEqualTo(0);

    final double outputForBelowZero = BalancedCostFunction.correctFloatingPointArithmeticError(-0.000001);
    assertThat(outputForBelowZero).isEqualTo(0);
  }

  @Test
  public void testCorrectFloatingPointArithmeticErrorAroundOne() {
    final double outputForaboveZero = BalancedCostFunction.correctFloatingPointArithmeticError(
        1.000001);
    assertThat(outputForaboveZero).isEqualTo(1);

    final double outputForBelowZero = BalancedCostFunction.correctFloatingPointArithmeticError(0.999999);
    assertThat(outputForBelowZero).isEqualTo(1);
  }
}
