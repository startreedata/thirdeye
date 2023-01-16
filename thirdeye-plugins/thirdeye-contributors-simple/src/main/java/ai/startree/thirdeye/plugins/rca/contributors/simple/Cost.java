/*
 * Copyright 2023 StarTree Inc
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

public enum Cost {
  VALUE_CHANGE {
    @Override
    public double compute(final double valueChangePercentage,
        final double contributionChangePercentage,
        final double contributionToOverallChangePercentage) {
      return Math.abs(valueChangePercentage);
    }
  }, CONTRIBUTION_CHANGE {
    @Override
    public double compute(final double valueChangePercentage,
        final double contributionChangePercentage,
        final double contributionToOverallChangePercentage) {
      return Math.abs(contributionChangePercentage);
    }
  }, CONTRIBUTION_TO_OVERALL_CHANGE {
    @Override
    public double compute(final double valueChangePercentage,
        final double contributionChangePercentage,
        final double contributionToOverallChangePercentage) {
      if (Math.abs(contributionToOverallChangePercentage)
          < MINIMUM_CONTRIBUTION_OF_INTEREST_PERCENTAGE) {
        // users don't care about nodes with small contribution to overall change
        return 0;
      }
      return Math.abs(contributionToOverallChangePercentage);
    }
  }, BAlANCED_SIMPLE {
    @Override
    public double compute(final double valueChangePercentage,
        final double contributionChangePercentage,
        final double contributionToOverallChangePercentage) {
      if (Math.abs(contributionToOverallChangePercentage)
          < MINIMUM_CONTRIBUTION_OF_INTEREST_PERCENTAGE) {
        // users don't care about nodes with small contribution to overall change
        return 0;
      }
      // rule of thumb formula: contributionTo overall change is more important, but take into account dimension contribution change
      return Math.abs(contributionToOverallChangePercentage) + Math.abs(
          contributionChangePercentage);
    }
  };

  public static final int MINIMUM_CONTRIBUTION_OF_INTEREST_PERCENTAGE = 3;

  public abstract double compute(final double valueChangePercentage,
      final double contributionChangePercentage,
      final double contributionToOverallChangePercentage);
}
