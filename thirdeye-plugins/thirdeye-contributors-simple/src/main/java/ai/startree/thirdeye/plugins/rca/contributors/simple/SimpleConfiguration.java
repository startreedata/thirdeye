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
package ai.startree.thirdeye.plugins.rca.contributors.simple;

import static ai.startree.thirdeye.plugins.rca.contributors.simple.Cost.BAlANCED_SIMPLE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleConfiguration {

  private Cost costFunction = BAlANCED_SIMPLE;

  public Cost getCostFunction() {
    return costFunction;
  }

  public SimpleConfiguration setCostFunction(
      final Cost costFunction) {
    this.costFunction = costFunction;
    return this;
  }
}
