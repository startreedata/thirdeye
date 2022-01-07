package org.apache.pinot.thirdeye.rca;

import static java.util.Collections.singletonList;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

  private static DimensionAnalysisResultApi notAvailable() {
    //fixme cyril return something less confusing
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
        DEFAULT_DIMENSIONS,
        DEFAULT_FILTER_JSON_PAYLOAD,
        Integer.parseInt(DEFAULT_CUBE_SUMMARY_SIZE_STRING),
        Integer.parseInt(DEFAULT_CUBE_DEPTH_STRING),
        DEFAULT_HIERARCHIES,
        false,
        DEFAULT_EXCLUDED_DIMENSIONS,
        DEFAULT_TIMEZONE_ID);
  }

  public DimensionAnalysisResultApi compute(String metricUrn,
      String dataset,
      String metric,
      long currentStartInclusive,
      long currentEndExclusive,
      long baselineStartInclusive,
      long baselineEndExclusive,
      List<String> dimensions,
      String filterJsonPayload,
      int summarySize,
      int depth,
      String hierarchiesPayload,
      boolean doOneSideError,
      List<String> excludedDimensions,
      String timeZone) {

    String metricName = metric;
    String datasetName = dataset;
    MetricConfigDTO metricConfigDTO;
    DateTimeZone dateTimeZone;
    Dimensions filteredDimensions;
    Multimap<String, String> filterSetMap;
    List<List<String>> hierarchies;

    try {
      //fixme cyril relies on dataset if metricUrn is null ...
      metricConfigDTO = fetchMetricConfig(metricUrn, metric, dataset);
      if (metricConfigDTO != null) {
        metricName = metricConfigDTO.getName();
        //...fixme cyril then get datasets !
        datasetName = metricConfigDTO.getDataset();
      }

      List<String> dimensionNames = dimensions.isEmpty() ? getDimensionsFromDataset(datasetName)
          : cleanDimensionStrings(dimensions);
      dimensionNames.removeAll(cleanDimensionStrings(excludedDimensions));
      filteredDimensions = new Dimensions(dimensionNames);

      filterSetMap = parseFilterJsonPayload(filterJsonPayload);
      hierarchies = parseHierarchiesPayload(hierarchiesPayload);
      dateTimeZone = DateTimeZone.forID(timeZone);
    } catch (IOException e) {
      LOG.error("Exception while fetching anomaly info", e);
      return notAvailable().setMetric(new MetricApi()
          .setUrn(metricUrn)
          .setName(metricName)
          .setDataset(new DatasetApi().setName(datasetName)));
    }

    return computeCube(metricName,
        datasetName,
        currentStartInclusive,
        currentEndExclusive,
        baselineStartInclusive,
        baselineEndExclusive,
        summarySize,
        depth,
        doOneSideError,
        metricConfigDTO.getDerivedMetricExpression(),
        dateTimeZone,
        filteredDimensions,
        filterSetMap,
        hierarchies);
  }

  public DimensionAnalysisResultApi computeCube(
      final String metricName, final String datasetName,
      final long currentStartInclusive, final long currentEndExclusive,
      final long baselineStartInclusive, final long baselineEndExclusive, final int summarySize,
      final int depth, final boolean doOneSideError,
      final String derivedMetricExpression,
      final DateTimeZone dateTimeZone, final Dimensions filteredDimensions,
      final Multimap<String, String> filterSetMap, final List<List<String>> hierarchies) {
    CubeAlgorithmRunner cubeAlgorithmRunner = new CubeAlgorithmRunner(
        derivedMetricExpression,
        dateTimeZone,
        datasetName,
        metricName,
        currentStartInclusive,
        currentEndExclusive,
        baselineStartInclusive,
        baselineEndExclusive,
        filteredDimensions,
        filterSetMap,
        summarySize,
        depth,
        hierarchies,
        doOneSideError
    );

    try {
      return cubeAlgorithmRunner.run();
    } catch (Exception e) {
      LOG.error("Exception while fetching running cube algorithm", e);
      return notAvailable().setMetric(new MetricApi()
          .setName(metricName)
          .setDataset(new DatasetApi().setName(datasetName)));
    }
  }

  public static List<String> cleanDimensionStrings(List<String> dimensions) {
    return dimensions.stream().map(String::trim).collect(Collectors.toList());
  }

  private List<String> getDimensionsFromDataset(String datasetName) {
    // fixme cyril need to find by dataset+datasource --> rewrite this in datasetConfigManager interface
    // fixme cyril refacto error management
    DatasetConfigDTO datasetConfigDTO = datasetConfigManager.findByDataset(datasetName);
    if (datasetConfigDTO != null) {
      return datasetConfigDTO.getDimensions();
    }
    throw new IllegalArgumentException(String.format("Unknown dataset %s. Cannot get dimensions.",
        datasetName));
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

  private MetricConfigDTO fetchMetricConfig(String metricUrn, String metric, String dataset) {
    MetricConfigDTO metricConfigDTO;
    if (StringUtils.isNotBlank(metricUrn)) {
      metricConfigDTO = metricConfigManager.findById(MetricEntity.fromURN(metricUrn).getId());
    } else {
      metricConfigDTO = metricConfigManager.findByMetricAndDataset(metric, dataset);
    }
    return metricConfigDTO;
  }

  private class CubeAlgorithmRunner {

    private static final String NUMERATOR_GROUP_NAME = "numerator";
    private static final String DENOMINATOR_GROUP_NAME = "denominator";
    // Only match string like "id123/id456" but not "id123*100/id456"
    // The 1st metric id will be put into numerator group and the 2nd metric id will be in denominator group
    private final Pattern SIMPLE_RATIO_METRIC_EXPRESSION_PARSER = Pattern.compile(
        "^id(?<" + NUMERATOR_GROUP_NAME + ">\\d*)\\/id(?<" + DENOMINATOR_GROUP_NAME + ">\\d*)$");

    private final String derivedMetricExpression;
    private final DateTimeZone dateTimeZone;
    private final String dataset;
    private final String metric;
    private final long currentStartInclusive;
    private final long currentEndExclusive;
    private final long baselineStartInclusive;
    private final long baselineEndExclusive;
    private final Dimensions dimensions;
    private final Multimap<String, String> dataFilters;
    private final int summarySize;
    private final int depth;
    private final List<List<String>> hierarchies;
    private final boolean doOneSideError;

    /**
     * Cube Algorithm Runner. Select the relevant algorithm based on the config and run it.
     *
     * @param dateTimeZone time zone of the data.
     * @param dataset dataset name.
     * @param derivedMetricExpression derivedMetricExpression String from MetricConfigDTO.
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
    public CubeAlgorithmRunner(
        final String derivedMetricExpression, final DateTimeZone dateTimeZone,
        final String dataset,
        final String metric, final long currentStartInclusive, final long currentEndExclusive,
        final long baselineStartInclusive, final long baselineEndExclusive,
        final Dimensions dimensions,
        final Multimap<String, String> dataFilters, final int summarySize, final int depth,
        final List<List<String>> hierarchies, final boolean doOneSideError) {
      this.derivedMetricExpression = derivedMetricExpression;
      this.dateTimeZone = dateTimeZone;
      this.dataset = dataset;
      this.metric = metric;
      this.currentStartInclusive = currentStartInclusive;
      this.currentEndExclusive = currentEndExclusive;
      this.baselineStartInclusive = baselineStartInclusive;
      this.baselineEndExclusive = baselineEndExclusive;
      this.dimensions = dimensions;
      this.dataFilters = dataFilters;
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
          costFunction,
          dateTimeZone);

      return mdSummary.buildSummary(
          dataset,
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
          costFunction, dateTimeZone);

      return mdSummary.buildRatioSummary(
          dataset,
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
