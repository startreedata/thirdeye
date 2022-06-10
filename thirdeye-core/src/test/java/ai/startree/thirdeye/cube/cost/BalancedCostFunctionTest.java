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
