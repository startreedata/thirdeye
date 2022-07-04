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
package ai.startree.thirdeye.spi.api.cube;

public class SummaryGainerLoserResponseRow extends BaseResponseRow {

  private String dimensionName;
  private String dimensionValue;
  private double cost;

  public String getDimensionName() {
    return dimensionName;
  }

  public SummaryGainerLoserResponseRow setDimensionName(final String dimensionName) {
    this.dimensionName = dimensionName;
    return this;
  }

  public String getDimensionValue() {
    return dimensionValue;
  }

  public SummaryGainerLoserResponseRow setDimensionValue(final String dimensionValue) {
    this.dimensionValue = dimensionValue;
    return this;
  }

  public double getCost() {
    return cost;
  }

  public SummaryGainerLoserResponseRow setCost(final double cost) {
    this.cost = cost;
    return this;
  }
}
