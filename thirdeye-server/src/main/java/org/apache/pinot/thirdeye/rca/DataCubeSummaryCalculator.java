package org.apache.pinot.thirdeye.rca;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.cube.additive.AdditiveCubeMetric;
import org.apache.pinot.thirdeye.cube.cost.BalancedCostFunction;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.cost.RatioCostFunction;
import org.apache.pinot.thirdeye.cube.data.cube.Cube;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeFetcher;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeFetcherImpl;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeMetric;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.data.dbrow.Row;
import org.apache.pinot.thirdeye.cube.ratio.RatioCubeMetric;
import org.apache.pinot.thirdeye.cube.summary.Summary;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils;
import org.apache.pinot.thirdeye.spi.rootcause.util.ParsedUrn;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataCubeSummaryCalculator {

  private static final Logger LOG = LoggerFactory.getLogger(DataCubeSummaryCalculator.class);

  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final MetricConfigManager metricConfigManager;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DataCubeSummaryCalculator(
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final MetricConfigManager metricDAO,
      final DataSourceCache dataSourceCache) {
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.metricConfigManager = metricDAO;
    this.dataSourceCache = dataSourceCache;
  }

  public DimensionAnalysisResultApi computeCube(
      final String metricName, final String datasetName,
      final Interval currentInterval, final Interval currentBaseline, final int summarySize,
      final int depth, final boolean doOneSideError,
      final String derivedMetricExpression, final List<String> dimensions,
      final List<String> excludedDimensions,
      final List<String> filters, final List<List<String>> hierarchies)
      throws Exception {

    Dimensions filteredDimensions = new Dimensions(dimensions.stream()
        .filter(dim -> !excludedDimensions.contains(dim)).collect(Collectors.toUnmodifiableList()));

    CubeAlgorithmRunner cubeAlgorithmRunner = new CubeAlgorithmRunner(
        derivedMetricExpression,
        datasetName,
        metricName,
        currentInterval,
        currentBaseline,
        filteredDimensions,
        filters,
        summarySize,
        depth,
        hierarchies,
        doOneSideError
    );

    return cubeAlgorithmRunner.run();
  }

  private class CubeAlgorithmRunner {

    private static final String NUMERATOR_GROUP_NAME = "numerator";
    private static final String DENOMINATOR_GROUP_NAME = "denominator";
    // Only match string like "id123/id456" but not "id123*100/id456"
    // The 1st metric id will be put into numerator group and the 2nd metric id will be in denominator group
    private final Pattern SIMPLE_RATIO_METRIC_EXPRESSION_PARSER = Pattern.compile(
        "^id(?<" + NUMERATOR_GROUP_NAME + ">\\d*)\\/id(?<" + DENOMINATOR_GROUP_NAME + ">\\d*)$");

    private final String derivedMetricExpression;
    private final String datasetName;
    private final String metricName;
    private final Interval currentInterval;
    private final Interval baselineInterval;
    private final Dimensions dimensions;
    private final Multimap<String, String> dataFilters;
    private final int summarySize;
    private final int depth;
    private final List<List<String>> hierarchies;
    private final boolean doOneSideError;

    /**
     * Cube Algorithm Runner. Select the relevant algorithm based on the config and run it.
     *
     * @param datasetName dataset name.
     * @param metricName metric name
     * @param derivedMetricExpression derivedMetricExpression String from MetricConfigDTO.
     * @param currentInterval current time interval.
     * @param baselineInterval baseline time interval.
     * @param dimensions the dimensions to be considered in the summary. If the variable depth
     *     is zero, then the order of the dimension is used; otherwise, this method will
     *     determine the order of the dimensions depending on their cost.
     *     After the order is determined, the first 'depth' dimensions are used the generated
     *     the summary.
     * @param filters filters in simple string format to apply on the data. Format is dim=value.
     * @param summarySize the number of entries to be put in the summary.
     * @param depth the number of dimensions to be drilled down when analyzing the summary.
     * @param hierarchies the hierarchy among the dimensions. The order will always be honored
     *     when determining the order of dimensions.
     * @param doOneSideError if the summary should only consider one side error. (global change
     *     side)
     */
    public CubeAlgorithmRunner(
        final String derivedMetricExpression,
        final String datasetName,
        final String metricName,
        final Interval currentInterval,
        final Interval baselineInterval,
        final Dimensions dimensions,
        final List<String> filters, final int summarySize, final int depth,
        final List<List<String>> hierarchies, final boolean doOneSideError) {
      this.derivedMetricExpression = derivedMetricExpression;
      this.datasetName = datasetName;
      this.metricName = metricName;
      this.currentInterval = currentInterval;
      this.baselineInterval = baselineInterval;
      Preconditions.checkNotNull(dimensions);
      checkArgument(dimensions.size() > 0);
      this.dimensions = dimensions;
      // fixme cyril prefer parsing to List<Predicate>
      this.dataFilters = ParsedUrn.toFiltersMap(EntityUtils.extractFilterPredicates(filters));
      checkArgument(summarySize > 1);
      this.summarySize = summarySize;
      checkArgument(depth >= 0);
      this.depth = depth;
      Preconditions.checkNotNull(hierarchies);
      this.hierarchies = hierarchies;
      this.doOneSideError = doOneSideError;
    }

    /**
     * @return the summary result of cube algorithm.
     */
    public DimensionAnalysisResultApi run() throws Exception {
      final CubeMetric<? extends Row> cubeMetric;
      final CostFunction costFunction;
      if (isRatioMetric()) {
        cubeMetric = buildRatioCubeMetric();
        costFunction = new RatioCostFunction();
      } else {
        // additive - nominal case
        cubeMetric =
            new AdditiveCubeMetric(datasetName, metricName, currentInterval, baselineInterval);
        costFunction = new BalancedCostFunction();
      }

      final CubeFetcher<? extends Row> cubeFetcher =
          new CubeFetcherImpl<>(dataSourceCache, thirdEyeCacheRegistry, cubeMetric);

      return buildSummary(cubeFetcher, costFunction);
    }

    private CubeMetric<? extends Row> buildRatioCubeMetric() {
      Matcher matcher = SIMPLE_RATIO_METRIC_EXPRESSION_PARSER.matcher(derivedMetricExpression);
      // Extract numerator and denominator id
      long numeratorId = Long.parseLong(matcher.group(NUMERATOR_GROUP_NAME));
      long denominatorId = Long.parseLong(matcher.group(DENOMINATOR_GROUP_NAME));

      // Get numerator and denominator's metric name
      String numeratorMetric = metricConfigManager.findById(numeratorId).getName();
      String denominatorMetric = metricConfigManager.findById(denominatorId).getName();
      // Generate cube result
      return new RatioCubeMetric(datasetName,
          numeratorMetric,
          denominatorMetric,
          currentInterval,
          baselineInterval);
    }

    /**
     * Returns true if the derived metric is a simple ratio metric such as "A/B" where A and B is a
     * metric.
     */
    private boolean isRatioMetric() {
      if (!Strings.isNullOrEmpty(derivedMetricExpression)) {
        Matcher matcher = SIMPLE_RATIO_METRIC_EXPRESSION_PARSER.matcher(derivedMetricExpression);
        return matcher.matches();
      }
      return false;
    }

    public DimensionAnalysisResultApi buildSummary(CubeFetcher<? extends Row> cubeFetcher,
        CostFunction costFunction) throws Exception {
      Cube cube = new Cube(costFunction);
      DimensionAnalysisResultApi response;
      if (depth > 0) { // depth != 0 means auto dimension order
        cube.buildWithAutoDimensionOrder(cubeFetcher, dimensions, dataFilters, depth, hierarchies);
        Summary summary = new Summary(cube, costFunction);
        response = summary.computeSummary(summarySize, doOneSideError, depth);
      } else { // manual dimension order
        cube.buildWithManualDimensionOrder(cubeFetcher, dimensions, dataFilters);
        Summary summary = new Summary(cube, costFunction);
        response = summary.computeSummary(summarySize, doOneSideError);
      }

      response.setMetric(new MetricApi()
          .setName(metricName)
          .setDataset(new DatasetApi().setName(datasetName))
      );

      return response;
    }
  }
}
