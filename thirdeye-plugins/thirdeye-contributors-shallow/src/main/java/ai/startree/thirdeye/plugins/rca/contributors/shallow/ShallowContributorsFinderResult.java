package ai.startree.thirdeye.plugins.rca.contributors.shallow;

import static ai.startree.thirdeye.plugins.rca.contributors.shallow.ShallowContributorsFinder.COL_BASELINE_VALUE;
import static ai.startree.thirdeye.plugins.rca.contributors.shallow.ShallowContributorsFinder.COL_CONTRIBUTION_CHANGE_PERCENTAGE;
import static ai.startree.thirdeye.plugins.rca.contributors.shallow.ShallowContributorsFinder.COL_CONTRIBUTION_TO_OVERALL_CHANGE_PERCENTAGE;
import static ai.startree.thirdeye.plugins.rca.contributors.shallow.ShallowContributorsFinder.COL_COST;
import static ai.startree.thirdeye.plugins.rca.contributors.shallow.ShallowContributorsFinder.COL_CURRENT_VALUE;
import static ai.startree.thirdeye.plugins.rca.contributors.shallow.ShallowContributorsFinder.COL_VALUE_CHANGE_PERCENTAGE;
import static ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi.ALL;
import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_DIMENSION_NAME;
import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_DIMENSION_VALUE;

import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.cube.SummaryResponseRow;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShallowContributorsFinderResult implements ContributorsFinderResult {

  private final DataFrame stats;
  private final String metricName;
  private final String datasetName;

  public ShallowContributorsFinderResult(final DataFrame stats, final String metricName,
      final String datasetName) {
    this.stats = stats;
    this.metricName = metricName;
    this.datasetName = datasetName;
  }

  @Override
  public DimensionAnalysisResultApi getDimensionAnalysisResult() {
    final DimensionAnalysisResultApi dimensionAnalysisResultApi = new DimensionAnalysisResultApi();
    final List<SummaryResponseRow> responseRows = new ArrayList<>();

    // build an index of distinct dimensions
    final Map<String, Integer> dimensionToIndex = new HashMap<>();
    final List<String> dimensions = new ArrayList<>();
    int index = 0;
    for (String dimensionName : stats.getStrings(COL_DIMENSION_NAME).values()) {
      if (!dimensionToIndex.containsKey(dimensionName)) {
        dimensionToIndex.put(dimensionName, index++);
        dimensions.add(dimensionName);
      }
    }
    dimensionAnalysisResultApi.setDimensions(new ArrayList<>(dimensionToIndex.keySet()));

    for (int i = 0; i < stats.size(); i++) {
      final SummaryResponseRow row = new SummaryResponseRow();
      final List<String> names = new ArrayList<>(Collections.nCopies(dimensions.size(),ALL));
      final String dimensionName = stats.getString(COL_DIMENSION_NAME, i);
      final int dimensionIndex = dimensionToIndex.get(dimensionName);
      final String dimensionValue = stats.getString(COL_DIMENSION_VALUE, i);
      names.set(dimensionIndex, dimensionValue);

      row.setNames(names)
          .setCost(stats.getDouble(COL_COST, i))
          .setBaselineValue(stats.getDouble(COL_BASELINE_VALUE, i))
          .setCurrentValue(stats.getDouble(COL_CURRENT_VALUE, i))
          .setChangePercentage(stats.getDouble(COL_VALUE_CHANGE_PERCENTAGE, i))
          .setContributionChangePercentage(stats.getDouble(COL_CONTRIBUTION_CHANGE_PERCENTAGE, i))
          .setContributionToOverallChangePercentage(stats.getDouble(
              COL_CONTRIBUTION_TO_OVERALL_CHANGE_PERCENTAGE,
              i));
      responseRows.add(row);
    }
    dimensionAnalysisResultApi.setResponseRows(responseRows);

    dimensionAnalysisResultApi.setMetric(new MetricApi().setDataset(new DatasetApi().setName(
        datasetName)).setName(metricName));

    return dimensionAnalysisResultApi;
  }
}
