package org.apache.pinot.thirdeye.rca;

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.cube.additive.AdditiveDBClient;
import org.apache.pinot.thirdeye.cube.cost.BalancedCostFunction;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.cost.RatioCostFunction;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.entry.MultiDimensionalRatioSummary;
import org.apache.pinot.thirdeye.cube.entry.MultiDimensionalSummary;
import org.apache.pinot.thirdeye.cube.entry.MultiDimensionalSummaryCLITool;
import org.apache.pinot.thirdeye.cube.ratio.RatioDBClient;
import org.apache.pinot.thirdeye.cube.summary.Summary;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataCubeSummaryCalculator {

  public static final String DEFAULT_TIMEZONE_ID = "UTC";
  public static final String DEFAULT_DEPTH = "3";
  public static final String DEFAULT_HIERARCHIES = "[]";
  public static final String DEFAULT_ONE_SIDE_ERROR = "false";
  public static final String DEFAULT_EXCLUDED_DIMENSIONS = "";
  private static final int DEFAULT_HIGHLIGHT_CUBE_SUMMARY_SIZE = 4;
  private static final int DEFAULT_HIGHLIGHT_CUBE_DEPTH = 3;

  private static final Logger LOG = LoggerFactory.getLogger(DataCubeSummaryCalculator.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String JAVASCRIPT_NULL_STRING = "undefined";
  private static final String HTML_STRING_ENCODING = "UTF-8";

  private static final String NUMERATOR_GROUP_NAME = "numerator";
  private static final String DENOMINATOR_GROUP_NAME = "denominator";
  // Only match string like "id123/id456" but not "id123*100/id456"
  // The 1st metric id will be put into numerator group and the 2nd metric id will be in denominator group
  private static final String SIMPLE_RATIO_METRIC_EXPRESSION_ID_PARSER =
      "^id(?<" + NUMERATOR_GROUP_NAME + ">\\d*)\\/id(?<" + DENOMINATOR_GROUP_NAME + ">\\d*)$";

  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DataCubeSummaryCalculator(
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final DataSourceCache dataSourceCache) {
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourceCache = dataSourceCache;
  }

  /**
   * Returns if the given metric is a simple ratio metric such as "A/B" where A and B is a metric.
   *
   * @param metricConfigDTO the config of a metric.
   * @return true if the given metric is a simple ratio metric.
   */
  static boolean isSimpleRatioMetric(MetricConfigDTO metricConfigDTO) {
    if (metricConfigDTO != null) {
      String metricExpression = metricConfigDTO.getDerivedMetricExpression();
      if (!Strings.isNullOrEmpty(metricExpression)) {
        Pattern pattern = Pattern.compile(SIMPLE_RATIO_METRIC_EXPRESSION_ID_PARSER);
        Matcher matcher = pattern.matcher(metricExpression);
        return matcher.matches();
      }
    }
    return false;
  }

  /**
   * Parse numerator and denominator id from a given metric expression string.
   *
   * @param derivedMetricExpression the given metric expression.
   * @return the parsed result, which is stored in MatchedRatioMetricsResult.
   */
  static MatchedRatioMetricsResult parseNumeratorDenominatorId(String derivedMetricExpression) {
    if (Strings.isNullOrEmpty(derivedMetricExpression)) {
      return new MatchedRatioMetricsResult(false, -1, -1);
    }

    Pattern pattern = Pattern.compile(SIMPLE_RATIO_METRIC_EXPRESSION_ID_PARSER);
    Matcher matcher = pattern.matcher(derivedMetricExpression);
    if (matcher.find()) {
      // Extract numerator and denominator id
      long numeratorId = Long.parseLong(matcher.group(NUMERATOR_GROUP_NAME));
      long denominatorId = Long.parseLong(matcher.group(DENOMINATOR_GROUP_NAME));
      return new MatchedRatioMetricsResult(true, numeratorId, denominatorId);
    } else {
      return new MatchedRatioMetricsResult(false, -1, -1);
    }
  }

  private static DimensionAnalysisResultApi notAvailable() {
    return new DimensionAnalysisResultApi()
        .setBaselineTotal(0d)
        .setCurrentTotal(0d)
        .setBaselineTotalSize(0d)
        .setCurrentTotalSize(0d)
        .setDimensions(singletonList(Summary.NOT_AVAILABLE));
  }

  public DimensionAnalysisResultApi compute(final MergedAnomalyResultDTO anomalyDTO) {
    return compute(
        anomalyDTO.getMetricUrn(),
        anomalyDTO.getCollection(),
        anomalyDTO.getMetric(),
        anomalyDTO.getStartTime(),
        anomalyDTO.getEndTime(),
        anomalyDTO.getStartTime() - TimeUnit.DAYS.toMillis(7),
        anomalyDTO.getEndTime() - TimeUnit.DAYS.toMillis(7),
        joptsimple.internal.Strings.EMPTY,
        joptsimple.internal.Strings.EMPTY,
        DEFAULT_HIGHLIGHT_CUBE_SUMMARY_SIZE,
        DEFAULT_HIGHLIGHT_CUBE_DEPTH,
        DEFAULT_HIERARCHIES,
        false,
        DEFAULT_EXCLUDED_DIMENSIONS,
        DEFAULT_TIMEZONE_ID);
  }

  public DimensionAnalysisResultApi compute(
      String metricUrn,
      String dataset,
      String metric,
      long currentStartInclusive,
      long currentEndExclusive,
      long baselineStartInclusive,
      long baselineEndExclusive,
      String groupByDimensions,
      String filterJsonPayload,
      int summarySize,
      int depth,
      String hierarchiesPayload,
      boolean doOneSideError,
      String excludedDimensions,
      String timeZone
  ) {
    return buildDataCubeSummary(metricUrn,
        metric,
        dataset,
        currentStartInclusive,
        currentEndExclusive,
        baselineStartInclusive,
        baselineEndExclusive,
        groupByDimensions,
        filterJsonPayload,
        summarySize,
        depth,
        hierarchiesPayload,
        doOneSideError,
        excludedDimensions,
        timeZone);
  }

  private DimensionAnalysisResultApi buildDataCubeSummary(String metricUrn,
      String dataset,
      String metric,
      long currentStartInclusive,
      long currentEndExclusive,
      long baselineStartInclusive,
      long baselineEndExclusive,
      String groupByDimensions,
      String filterJsonPayload,
      int summarySize,
      int depth,
      String hierarchiesPayload,
      boolean doOneSideError,
      String excludedDimensions,
      String timeZone) {
    if (summarySize < 1) {
      summarySize = 1;
    }

    String metricName = metric;
    String datasetName = dataset;
    DimensionAnalysisResultApi response = null;
    try {
      MetricConfigDTO metricConfigDTO = fetchMetricConfig(metricUrn, metric, dataset);
      if (metricConfigDTO != null) {
        metricName = metricConfigDTO.getName();
        datasetName = metricConfigDTO.getDataset();
      }

      Dimensions dimensions;
      if (StringUtils.isBlank(groupByDimensions) || JAVASCRIPT_NULL_STRING
          .equals(groupByDimensions)) {
        DatasetConfigDTO datasetConfigDTO = datasetConfigManager.findByDataset(datasetName);
        List<String> dimensionNames = new ArrayList<>();
        if (datasetConfigDTO != null) {
          dimensionNames = datasetConfigDTO.getDimensions();
        }
        dimensions = MultiDimensionalSummaryCLITool
            .sanitizeDimensions(new Dimensions(dimensionNames));
      } else {
        dimensions = new Dimensions(Arrays.asList(groupByDimensions.trim().split(",")));
      }

      if (!Strings.isNullOrEmpty(excludedDimensions)) {
        List<String> dimensionsToBeRemoved = Arrays.asList(excludedDimensions.trim().split(","));
        dimensions = MultiDimensionalSummaryCLITool
            .removeDimensions(dimensions, dimensionsToBeRemoved);
      }

      Multimap<String, String> filterSetMap;
      if (StringUtils.isBlank(filterJsonPayload) || JAVASCRIPT_NULL_STRING
          .equals(filterJsonPayload)) {
        filterSetMap = ArrayListMultimap.create();
      } else {
        filterJsonPayload = URLDecoder.decode(filterJsonPayload, HTML_STRING_ENCODING);
        filterSetMap = ThirdEyeUtils.convertToMultiMap(filterJsonPayload);
      }

      List<List<String>> hierarchies = OBJECT_MAPPER.readValue(hierarchiesPayload,
          new TypeReference<List<List<String>>>() {
          });

      DateTimeZone dateTimeZone = DateTimeZone.forID(timeZone);

      // Non simple ratio metrics
      if (!isSimpleRatioMetric(metricConfigDTO)) {
        response = runAdditiveCubeAlgorithm(dateTimeZone,
            datasetName,
            metricName,
            currentStartInclusive,
            currentEndExclusive,
            baselineStartInclusive,
            baselineEndExclusive,
            dimensions,
            filterSetMap,
            summarySize,
            depth,
            hierarchies,
            doOneSideError);
      } else {  // Simple ratio metric such as "A/B". On the contrary, "A*100/B" is not a simple ratio metric.
        response = runRatioCubeAlgorithm(dateTimeZone,
            datasetName,
            metricConfigDTO,
            currentStartInclusive,
            currentEndExclusive,
            baselineStartInclusive,
            baselineEndExclusive,
            dimensions,
            filterSetMap,
            summarySize,
            depth,
            hierarchies,
            doOneSideError);
      }
    } catch (Exception e) {
      LOG.error("Exception while generating difference summary", e);
      if (metricUrn != null) {
        response = notAvailable()
            .setMetric(new MetricApi().setUrn(metricUrn));
      } else {
        response = notAvailable()
        .setMetric(new MetricApi()
            .setName(metricName)
            .setDataset(new DatasetApi().setName(datasetName))
        );

      }
    }

    return response;
  }

  private MetricConfigDTO fetchMetricConfig(String metricUrn, String metric, String dataset) {
    MetricConfigDTO metricConfigDTO;
    if (StringUtils.isNotBlank(metricUrn)) {
      metricConfigDTO = metricConfigManager.findById(MetricEntity.fromURN(metricUrn).getId());
    } else {
      metricConfigDTO = metricConfigManager.findByMetricAndDataset(metric, dataset);
    }
    return metricConfigDTO;
  }

  /**
   * Executes cube algorithm for the given additive metric.
   *
   * @param dateTimeZone time zone of the data.
   * @param dataset dataset name.
   * @param metric metric name.
   * @param currentStartInclusive timestamp of current start.
   * @param currentEndExclusive timestamp of current end.
   * @param baselineStartInclusive timestamp of baseline start.
   * @param baselineEndExclusive timestamp of baseline end.
   * @param dimensions ordered dimensions to be drilled down by the algorithm.
   * @param dataFilters the filter to be applied on the data. Thus, the algorithm will only
   *     analyze a subset of data.
   * @param summarySize the size of the summary result.
   * @param depth the depth of the dimensions to be analyzed.
   * @param hierarchies the hierarchy among the dimensions.
   * @param doOneSideError flag to toggle if we only want one side results.
   * @return the summary result of cube algorithm.
   */
  private DimensionAnalysisResultApi runAdditiveCubeAlgorithm(DateTimeZone dateTimeZone,
      String dataset,
      String metric,
      long currentStartInclusive,
      long currentEndExclusive,
      long baselineStartInclusive,
      long baselineEndExclusive,
      Dimensions dimensions,
      Multimap<String, String> dataFilters,
      int summarySize,
      int depth,
      List<List<String>> hierarchies,
      boolean doOneSideError) throws Exception {

    final CostFunction costFunction = new BalancedCostFunction();
    final AdditiveDBClient cubeDbClient = new AdditiveDBClient(
        dataSourceCache,
        thirdEyeCacheRegistry);
    final MultiDimensionalSummary mdSummary = new MultiDimensionalSummary(cubeDbClient,
        costFunction,
        dateTimeZone);

    return mdSummary.buildSummary(dataset,
        metric,
        currentStartInclusive,
        currentEndExclusive,
        baselineStartInclusive,
        baselineEndExclusive,
        dimensions,
        dataFilters,
        summarySize,
        depth,
        hierarchies,
        doOneSideError);
  }

  /**
   * Executes cube algorithm for the given ratio metric.
   *
   * @param dateTimeZone time zone of the data.
   * @param dataset dataset name.
   * @param metricConfigDTO the metric config of the ratio metric.
   * @param currentStartInclusive timestamp of current start.
   * @param currentEndExclusive timestamp of current end.
   * @param baselineStartInclusive timestamp of baseline start.
   * @param baselineEndExclusive timestamp of baseline end.
   * @param dimensions ordered dimensions to be drilled down by the algorithm.
   * @param dataFilters the filter to be applied on the data. Thus, the algorithm will only
   *     analyze a subset of data.
   * @param summarySize the size of the summary result.
   * @param depth the depth of the dimensions to be analyzed.
   * @param hierarchies the hierarchy among the dimensions.
   * @param doOneSideError flag to toggle if we only want one side results.
   * @return the summary result of cube algorithm.
   */
  private DimensionAnalysisResultApi runRatioCubeAlgorithm(DateTimeZone dateTimeZone,
      String dataset,
      MetricConfigDTO metricConfigDTO,
      long currentStartInclusive,
      long currentEndExclusive,
      long baselineStartInclusive,
      long baselineEndExclusive,
      Dimensions dimensions,
      Multimap<String, String> dataFilters,
      int summarySize,
      int depth,
      List<List<String>> hierarchies,
      boolean doOneSideError) throws Exception {
    Preconditions.checkNotNull(metricConfigDTO);

    // Construct regular expression parser
    String derivedMetricExpression = metricConfigDTO.getDerivedMetricExpression();
    MatchedRatioMetricsResult matchedRatioMetricsResult = parseNumeratorDenominatorId(
        derivedMetricExpression);

    if (matchedRatioMetricsResult.hasFound) {
      // Extract numerator and denominator id
      long numeratorId = matchedRatioMetricsResult.numeratorId;
      long denominatorId = matchedRatioMetricsResult.denominatorId;
      // Get numerator and denominator's metric name
      String numeratorMetric = metricConfigManager.findById(numeratorId).getName();
      String denominatorMetric = metricConfigManager.findById(denominatorId).getName();
      // Generate cube result
      CostFunction costFunction = new RatioCostFunction();
      RatioDBClient dbClient = new RatioDBClient(dataSourceCache,
          thirdEyeCacheRegistry);
      MultiDimensionalRatioSummary mdSummary = new MultiDimensionalRatioSummary(dbClient,
          costFunction, dateTimeZone);

      return mdSummary.buildRatioSummary(dataset,
          numeratorMetric,
          denominatorMetric,
          currentStartInclusive,
          currentEndExclusive,
          baselineStartInclusive,
          baselineEndExclusive,
          dimensions,
          dataFilters,
          summarySize,
          depth,
          hierarchies,
          doOneSideError);
    } else { // parser should find ids because of the guard of the if-condition.
      LOG.error("Unable to parser numerator and denominator metric for metric" + metricConfigDTO
          .getName());
      return notAvailable()
      .setMetric(new MetricApi()
          .setName(metricConfigDTO.getName())
          .setDataset(new DatasetApi().setName(dataset))
      );
    }
  }
}
