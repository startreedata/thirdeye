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
package ai.startree.thirdeye.rca;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.cube.cost.BalancedCostFunction;
import ai.startree.thirdeye.cube.cost.CostFunction;
import ai.startree.thirdeye.cube.data.Cube;
import ai.startree.thirdeye.cube.data.CubeFetcher;
import ai.startree.thirdeye.cube.data.Dimensions;
import ai.startree.thirdeye.cube.summary.Summary;
import ai.startree.thirdeye.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataCubeSummaryCalculator implements ContributorsFinder {

  private static final Logger LOG = LoggerFactory.getLogger(DataCubeSummaryCalculator.class);

  private final AggregationLoader aggregationLoader;

  @Inject
  public DataCubeSummaryCalculator(final AggregationLoader aggregationLoader) {
    this.aggregationLoader = aggregationLoader;
  }

  public ContributorsFinderResult search(final ContributorsSearchConfiguration searchConfiguration)
      throws Exception {

    final CubeAlgorithmRunner cubeAlgorithmRunner = new CubeAlgorithmRunner(searchConfiguration.getDatasetConfigDTO(),
        searchConfiguration.getMetricConfigDTO(),
        searchConfiguration.getCurrentInterval(),
        searchConfiguration.getCurrentBaseline(),
        new Dimensions(searchConfiguration.getDatasetConfigDTO().getDimensions()),
        searchConfiguration.getFilters(),
        searchConfiguration.getSummarySize(),
        searchConfiguration.getDepth(),
        searchConfiguration.getHierarchies(),
        searchConfiguration.isDoOneSideError());

    // todo cyril rewrite this part - cube runner does not have to build an API result directly
    final DimensionAnalysisResultApi runResult = cubeAlgorithmRunner.run();
    return () -> runResult;
  }

  private class CubeAlgorithmRunner {

    private static final String NUMERATOR_GROUP_NAME = "numerator";
    private static final String DENOMINATOR_GROUP_NAME = "denominator";
    // Only match string like "id123/id456" but not "id123*100/id456"
    // The 1st metric id will be put into numerator group and the 2nd metric id will be in denominator group
    private final Pattern SIMPLE_RATIO_METRIC_EXPRESSION_PARSER = Pattern.compile(
        "^id(?<" + NUMERATOR_GROUP_NAME + ">\\d*)\\/id(?<" + DENOMINATOR_GROUP_NAME + ">\\d*)$");

    private final DatasetConfigDTO datasetConfigDTO;
    private final MetricConfigDTO metricConfigDTO;
    private final Interval currentInterval;
    private final Interval baselineInterval;
    private final Dimensions dimensions;
    private final List<Predicate> dataFilters;
    private final int summarySize;
    private final int depth;
    private final List<List<String>> hierarchies;
    private final boolean doOneSideError;

    /**
     * Cube Algorithm Runner. Select the relevant algorithm based on the config and run it.
     *
     * @param datasetConfigDTO dataset config.
     * @param metricConfigDTO metric config
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
    public CubeAlgorithmRunner(final DatasetConfigDTO datasetConfigDTO,
        final MetricConfigDTO metricConfigDTO, final Interval currentInterval,
        final Interval baselineInterval, final Dimensions dimensions, final List<String> filters,
        final int summarySize, final int depth, final List<List<String>> hierarchies,
        final boolean doOneSideError) {
      this.datasetConfigDTO = datasetConfigDTO;
      this.metricConfigDTO = metricConfigDTO;
      this.currentInterval = currentInterval;
      this.baselineInterval = baselineInterval;
      Preconditions.checkNotNull(dimensions);
      checkArgument(dimensions.size() > 0);
      this.dimensions = dimensions;
      this.dataFilters = Predicate.parseAndCombinePredicates(filters);
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
      checkArgument(!isRatioMetric(),
          String.format("Metric is a legacy ratio metric: %s It is not supported anymore",
              metricConfigDTO.getDerivedMetricExpression()));

      final MetricSlice currentSlice = MetricSlice.from(metricConfigDTO,
          currentInterval,
          dataFilters,
          datasetConfigDTO);
      final MetricSlice baselineSlice = MetricSlice.from(metricConfigDTO,
          baselineInterval,
          dataFilters,
          datasetConfigDTO);
      final CostFunction costFunction = new BalancedCostFunction();

      final CubeFetcher cubeFetcher = new CubeFetcher(aggregationLoader,
          currentSlice,
          baselineSlice);

      return buildSummary(cubeFetcher, costFunction);
    }

    public DimensionAnalysisResultApi buildSummary(CubeFetcher cubeFetcher,
        CostFunction costFunction) throws Exception {
      Cube cube = new Cube(cubeFetcher, costFunction);
      final DimensionAnalysisResultApi response;
      if (depth > 0) { // depth != 0 means auto dimension order
        cube.buildWithAutoDimensionOrder(dimensions, dataFilters, depth, hierarchies);
        Summary summary = new Summary(cube, costFunction);
        response = summary.computeSummary(summarySize, doOneSideError, depth);
      } else { // manual dimension order
        cube.buildWithManualDimensionOrder(dimensions);
        Summary summary = new Summary(cube, costFunction);
        response = summary.computeSummary(summarySize, doOneSideError);
      }

      response.setMetric(new MetricApi().setName(metricConfigDTO.getName())
          .setDataset(new DatasetApi().setName(datasetConfigDTO.getDataset())));

      return response;
    }

    /**
     * Returns true if the derived metric is a simple ratio metric such as "A/B" where A and B is a
     * metric.
     */
    private boolean isRatioMetric() {
      if (!Strings.isNullOrEmpty(metricConfigDTO.getDerivedMetricExpression())) {
        Matcher matcher = SIMPLE_RATIO_METRIC_EXPRESSION_PARSER.matcher(metricConfigDTO.getDerivedMetricExpression());
        return matcher.matches();
      }
      return false;
    }
  }
}
