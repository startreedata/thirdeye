package org.apache.pinot.thirdeye.cube.entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.data.cube.Cube;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.ratio.RatioDBClient;
import org.apache.pinot.thirdeye.cube.summary.Summary;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.joda.time.Interval;

/**
 * A portal class that is used to trigger the multi-dimensional summary algorithm and to get the
 * summary response on a
 * ratio metric.
 */
public class MultiDimensionalRatioSummary {

  private final RatioDBClient dbClient;
  private final CostFunction costFunction;

  public MultiDimensionalRatioSummary(RatioDBClient dbClient, CostFunction costFunction) {
    Preconditions.checkNotNull(dbClient);
    Preconditions.checkNotNull(costFunction);

    this.dbClient = dbClient;
    this.costFunction = costFunction;
  }

  /**
   * Builds the summary given the given metric information.
   *
   * @param dataset the dataset of the metric.
   * @param numeratorMetric the name of the numerator metric.
   * @param denominatorMetric the name of the denominator metric.
   * @param currentInterval interval of the current data cube.
   * @param baselineInterval interval of the baseline data cube.
   * @param dimensions the dimensions to be considered in the summary. If the variable depth is
   *     zero, then the order
   *     of the dimension is used; otherwise, this method will determine the order of the
   *     dimensions
   *     depending on their cost. After the order is determined, the first 'depth' dimensions are
   *     used
   *     the generated the summary.
   * @param dataFilters the filter to be applied on the data cube.
   * @param summarySize the number of entries to be put in the summary.
   * @param depth the number of dimensions to be drilled down when analyzing the summary.
   * @param hierarchies the hierarchy among the dimensions. The order will always be honored
   *     when determining the order
   *     of dimensions.
   * @param doOneSideError if the summary should only consider one side error.
   * @return the multi-dimensional summary of a ratio metric.
   */
  public DimensionAnalysisResultApi buildRatioSummary(String dataset,
      String numeratorMetric,
      String denominatorMetric,
      Interval currentInterval,
      Interval baselineInterval,
      Dimensions dimensions,
      Multimap<String, String> dataFilters,
      int summarySize,
      int depth,
      List<List<String>> hierarchies,
      boolean doOneSideError) throws Exception {
    // Check arguments
    List<String> metrics = new ArrayList<>();
    metrics.add(numeratorMetric);
    metrics.add(denominatorMetric);
    SummaryUtils.checkArguments(dataset,
      metrics,
       dimensions,
       dataFilters,
       summarySize,
       depth,
       hierarchies);

    dbClient.setDataset(dataset);
    dbClient.setNumeratorMetric(numeratorMetric);
    dbClient.setDenominatorMetric(denominatorMetric);
    dbClient.setCurrentInterval(currentInterval);
    dbClient.setBaselineInterval(baselineInterval);

    Cube cube = new Cube(costFunction);
    DimensionAnalysisResultApi response;
    if (depth > 0) { // depth != 0 means manual dimension order
      cube.buildWithAutoDimensionOrder(dbClient, dimensions, dataFilters, depth, hierarchies);
      Summary summary = new Summary(cube, costFunction);
      response = summary.computeSummary(summarySize, doOneSideError, depth);
    } else { // manual dimension order
      cube.buildWithManualDimensionOrder(dbClient, dimensions, dataFilters);
      Summary summary = new Summary(cube, costFunction);
      response = summary.computeSummary(summarySize, doOneSideError);
    }
    response.setMetric(new MetricApi()
        .setName(numeratorMetric + "/" + denominatorMetric)
        .setDataset(new DatasetApi().setName(dataset))
    );

    return response;
  }
}
