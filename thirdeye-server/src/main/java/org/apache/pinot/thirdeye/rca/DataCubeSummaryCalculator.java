package org.apache.pinot.thirdeye.rca;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.cube.additive.AdditiveDBClient;
import org.apache.pinot.thirdeye.cube.cost.BalancedCostFunction;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.cost.RatioCostFunction;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.entry.MultiDimensionalRatioSummary;
import org.apache.pinot.thirdeye.cube.entry.MultiDimensionalSummary;
import org.apache.pinot.thirdeye.cube.ratio.RatioDBClient;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils;
import org.apache.pinot.thirdeye.spi.rootcause.util.ParsedUrn;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataCubeSummaryCalculator {

  public static final String DEFAULT_HIERARCHIES = "[]";
  public static final String DEFAULT_ONE_SIDE_ERROR = "false";
  public static final String DEFAULT_CUBE_DEPTH_STRING = "3";
  public static final String DEFAULT_CUBE_SUMMARY_SIZE_STRING = "4";
  private static final List<String> DEFAULT_DIMENSIONS = ImmutableList.of();
  private static final List<String> DEFAULT_EXCLUDED_DIMENSIONS = ImmutableList.of();
  private static final String DEFAULT_FILTER_JSON_PAYLOAD = "";

  private static final Logger LOG = LoggerFactory.getLogger(DataCubeSummaryCalculator.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String HTML_STRING_ENCODING = "UTF-8";

  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final MetricConfigManager metricConfigManager;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DataCubeSummaryCalculator(
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final MetricConfigManager metricConfigManager,
      final DataSourceCache dataSourceCache) {
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.metricConfigManager = metricConfigManager;
    this.dataSourceCache = dataSourceCache;
  }

  public DimensionAnalysisResultApi computeCube(
      final String metricName, final String datasetName,
      final Interval currentInterval, final Interval currentBaseline, final int summarySize,
      final int depth, final boolean doOneSideError,
      final String derivedMetricExpression, final Dimensions filteredDimensions,
      final List<String> filters, final List<List<String>> hierarchies)
      throws Exception {

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

  public static List<String> cleanDimensionStrings(List<String> dimensions) {
    return dimensions.stream().map(String::trim).collect(Collectors.toList());
  }

  private List<List<String>> parseHierarchiesPayload(final String hierarchiesPayload)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(hierarchiesPayload, new TypeReference<>() {});
  }

  private Multimap<String, String> parseFilterJsonPayload(String filterJsonPayload)
      throws UnsupportedEncodingException {
    return StringUtils.isBlank(filterJsonPayload) ?
        ArrayListMultimap.create() :
        ThirdEyeUtils.convertToMultiMap(URLDecoder.decode(filterJsonPayload, HTML_STRING_ENCODING));
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
     * @param dimensions ordered dimensions to be drilled down by the algorithm.
     * @param filters a filter in simple string format to be applied on the data. Thus, the algorithm will only
     *     analyze a subset of data. Format is dim=value.
     * @param summarySize the size of the summary result.
     * @param depth the depth of the dimensions to be analyzed.
     * @param hierarchies the hierarchy among the dimensions.
     * @param doOneSideError flag to toggle if we only want one side results.
     * @return the summary result of cube algorithm.
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
      this.dimensions = dimensions;
      this.dataFilters = ParsedUrn.toFiltersMap(EntityUtils.extractFilterPredicates(filters));
      this.summarySize = summarySize;
      this.depth = depth;
      this.hierarchies = hierarchies;
      this.doOneSideError = doOneSideError;
    }

    public DimensionAnalysisResultApi run() throws Exception {
      if (isSimpleRatioMetric()) {
        return runRatioCubeAlgorithm();
      }
      return runAdditiveCubeAlgorithm();
    }

    /**
     * Executes cube algorithm for the given additive metric.
     */
    private DimensionAnalysisResultApi runAdditiveCubeAlgorithm() throws Exception {
      final CostFunction costFunction = new BalancedCostFunction();
      final AdditiveDBClient cubeDbClient = new AdditiveDBClient(
          dataSourceCache,
          thirdEyeCacheRegistry);
      final MultiDimensionalSummary mdSummary = new MultiDimensionalSummary(
          cubeDbClient,
          costFunction);

      return mdSummary.buildSummary(
          datasetName,
          metricName,
          currentInterval,
          baselineInterval,
          dimensions,
          dataFilters,
          summarySize,
          depth,
          hierarchies,
          doOneSideError);
    }

    /**
     * Executes cube algorithm for a given ratio metric.
     */
    private DimensionAnalysisResultApi runRatioCubeAlgorithm()
        throws Exception {
      Preconditions.checkArgument(isSimpleRatioMetric());
      Matcher matcher = SIMPLE_RATIO_METRIC_EXPRESSION_PARSER.matcher(derivedMetricExpression);
      // Extract numerator and denominator id
      long numeratorId = Long.parseLong(matcher.group(NUMERATOR_GROUP_NAME));
      long denominatorId = Long.parseLong(matcher.group(DENOMINATOR_GROUP_NAME));

      // Get numerator and denominator's metric name
      String numeratorMetric = metricConfigManager.findById(numeratorId).getName();
      String denominatorMetric = metricConfigManager.findById(denominatorId).getName();
      // Generate cube result
      CostFunction costFunction = new RatioCostFunction();
      RatioDBClient dbClient = new RatioDBClient(dataSourceCache, thirdEyeCacheRegistry);
      MultiDimensionalRatioSummary mdSummary = new MultiDimensionalRatioSummary(dbClient,
          costFunction);

      return mdSummary.buildRatioSummary(
          datasetName,
          numeratorMetric,
          denominatorMetric,
          currentInterval,
          baselineInterval,
          dimensions,
          dataFilters,
          summarySize,
          depth,
          hierarchies,
          doOneSideError);
    }

    /**
     * Returns true if the derived metric is a simple ratio metric such as "A/B" where A and B is a
     * metric.
     */
    private boolean isSimpleRatioMetric() {
      if (!Strings.isNullOrEmpty(derivedMetricExpression)) {
        Matcher matcher = SIMPLE_RATIO_METRIC_EXPRESSION_PARSER.matcher(derivedMetricExpression);
        return matcher.matches();
      }
      return false;
    }
  }
}
