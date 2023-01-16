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

import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_DIMENSION_NAME;
import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_DIMENSION_VALUE;
import static ai.startree.thirdeye.spi.rca.Stats.computeContributionChangePercentage;
import static ai.startree.thirdeye.spi.rca.Stats.computeContributionToOverallChangePercentage;
import static ai.startree.thirdeye.spi.rca.Stats.computeValueChangePercentage;

import ai.startree.thirdeye.spi.api.AnalysisRunInfo;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Series.DoubleConditional;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Breakdown by one dimension, for each dimension. Same as the heatmap.
 * Then uses a cost function to determine which changes are important.
 */
public class SimpleContributorsFinder implements ContributorsFinder {

  private static final int LIMIT_DEFAULT = 100;
  private static final Logger LOG = LoggerFactory.getLogger(SimpleContributorsFinder.class);
  public static final String BASELINE_SUFFIX = "baseline_";
  public static final String COL_BASELINE_VALUE = BASELINE_SUFFIX + COL_VALUE;
  private static final String CURRENT_SUFFIX = "current_";
  public static final String COL_CURRENT_VALUE = CURRENT_SUFFIX + COL_VALUE;
  public static final String COL_COST = "cost";
  public static final String COL_VALUE_CHANGE_PERCENTAGE = "value_change_percentage";
  public static final String COL_CONTRIBUTION_CHANGE_PERCENTAGE = "contribution_change_percentage";
  public static final String COL_CONTRIBUTION_TO_OVERALL_CHANGE_PERCENTAGE = "contribution_to_overall_change_percentage";

  private final SimpleConfiguration simpleConfiguration;
  private final AggregationLoader aggregationLoader;

  public SimpleContributorsFinder(final AggregationLoader aggregationLoader,
      final SimpleConfiguration simpleConfiguration) {
    this.aggregationLoader = aggregationLoader;
    this.simpleConfiguration = simpleConfiguration;
  }

  public ContributorsFinderResult search(final ContributorsSearchConfiguration searchConfiguration)
      throws Exception {
    final MetricSlice baselineSlice = MetricSlice.from(searchConfiguration.getMetricConfigDTO(),
        searchConfiguration.getBaselineInterval(),
        searchConfiguration.getFilters(),
        searchConfiguration.getDatasetConfigDTO());

    final MetricSlice currentSlice = MetricSlice.from(searchConfiguration.getMetricConfigDTO(),
        searchConfiguration.getCurrentInterval(),
        searchConfiguration.getFilters(),
        searchConfiguration.getDatasetConfigDTO());

    final DataFrame baseline = aggregationLoader.loadBreakdown(baselineSlice, LIMIT_DEFAULT);
    if (baseline.size() <= 0) {
      return cannotComputeResult(
          "No data in the baseline timeframe. Cannot compute top contributors. You may try with a different baseline offset.");
    }
    baseline.dropSeries(COL_TIME);
    final double baselineTotal = getTotalFromBreakdown(baseline);

    final DataFrame current = aggregationLoader.loadBreakdown(currentSlice, LIMIT_DEFAULT);
    if (current.size() <= 0) {
      return cannotComputeResult(
          "No data in the current timeframe. Cannot compute top contributors. Data collect is stopped or broken for this metric.");
    }
    current.dropSeries(COL_TIME);
    final double currentTotal = getTotalFromBreakdown(current);

    DataFrame stats = computeStats(baseline,
        baselineTotal,
        current,
        currentTotal);

    if (searchConfiguration.isDoOneSideError()) {
      final ChangeSide changeSide = currentTotal >= baselineTotal ? ChangeSide.UP : ChangeSide.DOWN;
      stats = stats.addSeries("change",
              stats.getDoubles(COL_CURRENT_VALUE).subtract(stats.getDoubles(COL_BASELINE_VALUE)))
          .filter(changeSide.conditional, "change");
    }

    stats = stats.addSeries(COL_COST, computeCost(stats))
        .sortedBy(COL_COST)
        .slice(stats.size() - searchConfiguration.getSummarySize(), stats.size());

    return new SimpleContributorsFinderResult(stats,
        searchConfiguration.getMetricConfigDTO().getName(),
        searchConfiguration.getDatasetConfigDTO().getDataset());
  }

  private ContributorsFinderResult cannotComputeResult(final String message) {
    return () -> new DimensionAnalysisResultApi()
        .setAnalysisRunInfo(new AnalysisRunInfo().setSuccess(false).setMessage(message));
  }

  /**
   * @return a df with COL_DIMENSION_NAME, COL_DIMENSION_VALUE, COL_BASELINE_VALUE,
   *     COL_CURRENT_VALUE, COL_VALUE_CHANGE_PERCENTAGE, COL_CONTRIBUTION_CHANGE_PERCENTAGE,
   *     COL_CONTRIBUTION_TO_OVERALL_CHANGE_PERCENTAGE
   **/
  @VisibleForTesting
  protected static DataFrame computeStats(final DataFrame baseline,
      final double baselineTotal, final DataFrame current, final double currentTotal) {
    // merge breakdowns - some (dimension, value) tuples are not present in both tables - fill those with zeroes
    baseline.renameSeries(COL_VALUE, COL_BASELINE_VALUE);
    current.renameSeries(COL_VALUE, COL_CURRENT_VALUE);
    final DataFrame stats = baseline.joinOuter(current, COL_DIMENSION_NAME, COL_DIMENSION_VALUE)
        .fillNull(COL_BASELINE_VALUE, COL_CURRENT_VALUE);

    // add stats series
    final DoubleSeries baselineValues = stats.getDoubles(COL_BASELINE_VALUE);
    final DoubleSeries currentValues = stats.getDoubles(COL_CURRENT_VALUE);
    stats
        .addSeries(COL_VALUE_CHANGE_PERCENTAGE,
            computeValueChangePercentage(baselineValues, currentValues))
        .addSeries(COL_CONTRIBUTION_CHANGE_PERCENTAGE,
            computeContributionChangePercentage(baselineValues,
                currentValues,
                baselineTotal,
                currentTotal))
        .addSeries(COL_CONTRIBUTION_TO_OVERALL_CHANGE_PERCENTAGE,
            computeContributionToOverallChangePercentage(baselineValues,
                currentValues,
                baselineTotal,
                currentTotal));
    return stats;
  }

  private DoubleSeries computeCost(final DataFrame stats) {
    final Cost costFunction = simpleConfiguration.getCostFunction();
    DoubleSeries.Builder builder = DoubleSeries.builder();
    for (int i = 0; i < stats.size(); i++) {
      final double valueChangePercentage = stats.getDouble(COL_VALUE_CHANGE_PERCENTAGE, i);
      final double contributionChangePercentage = stats.getDouble(
          COL_CONTRIBUTION_CHANGE_PERCENTAGE,
          i);
      final double contributionToOverallChangePercentage = stats.getDouble(
          COL_CONTRIBUTION_TO_OVERALL_CHANGE_PERCENTAGE,
          i);
      builder.addValues(costFunction.compute(valueChangePercentage,
          contributionChangePercentage,
          contributionToOverallChangePercentage));
    }
    return builder.build();
  }

  @VisibleForTesting
  protected static double getTotalFromBreakdown(final DataFrame breakdownDataframe) {
    // aggregate along dimensions and get any value to get the total
    return breakdownDataframe.groupByValue(COL_DIMENSION_NAME)
        .sum(COL_VALUE)
        .getValues()
        .getDouble(0);
  }

  private enum ChangeSide {
    DOWN(change -> change[0] < 0), UP(change -> change[0] >= 0);

    private final DoubleConditional conditional;

    ChangeSide(DoubleConditional conditional) {
      this.conditional = conditional;
    }
  }
}
