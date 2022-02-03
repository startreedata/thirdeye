package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils.parseOffset;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.datasource.loader.DefaultAggregationLoader;
import org.apache.pinot.thirdeye.rca.RootCauseAnalysisInfo;
import org.apache.pinot.thirdeye.rca.RootCauseAnalysisInfoFetcher;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.HeatMapResultApi;
import org.apache.pinot.thirdeye.spi.api.HeatMapResultApi.HeatMapBreakdownApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.BaselineAggregate;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.BaselineAggregateType;
import org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>RootCauseMetricResource is a central endpoint for querying different views on metrics as used
 * by the
 * RCA frontend. It delivers metric timeseries, aggregates, and breakdowns (de-aggregations).
 * The endpoint parses metric urns and a unified set of "offsets", i.e. time-warped baseline of the
 * specified metric. It further aligns queried time stamps to sensibly match the raw dataset.</p>
 *
 * @see BaselineParsingUtils#parseOffset(String, DateTimeZone) supported offsets
 */
@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RootCauseMetricResource {

  private static final Logger LOG = LoggerFactory.getLogger(RootCauseMetricResource.class);
  private static final long TIMEOUT = 600000;
  private static final String OFFSET_DEFAULT = "current";
  private static final int LIMIT_DEFAULT = 100;
  private static final String DEFAULT_BASELINE_OFFSET = "P1W";

  private final ExecutorService executor;
  private final AggregationLoader aggregationLoader;
  @Deprecated
  // prefer getting datasetDAO from rootCauseAnalysisInfoFetcher
  private final MetricConfigManager metricDAO;
  @Deprecated
  // prefer getting datasetDAO from rootCauseAnalysisInfoFetcher
  private final DatasetConfigManager datasetDAO;
  private final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher;

  @Inject
  public RootCauseMetricResource(final AggregationLoader aggregationLoader,
      final MetricConfigManager metricDAO,
      final DatasetConfigManager datasetDAO,
      final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher) {
    this.aggregationLoader = aggregationLoader;
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.rootCauseAnalysisInfoFetcher = rootCauseAnalysisInfoFetcher;

    this.executor = Executors.newCachedThreadPool();
  }

  /**
   * Returns an aggregate value for the specified metric and time range, and (optionally) offset.
   * Aligns time stamps if necessary and returns NaN if no data is available for the given time
   * range.
   *
   * @param urn metric urn
   * @param start start time (in millis)
   * @param end end time (in millis)
   * @param offset offset identifier (e.g. "current", "wo2w")
   * @return aggregate value, or NaN if data not available
   * @throws Exception on catch-all execution failure
   * @see BaselineParsingUtils#parseOffset(String, DateTimeZone) supported offsets
   */
  @GET
  @Path("/aggregate")
  @ApiOperation(value = "Returns an aggregate value for the specified metric and time range, and (optionally) offset.")
  public Response getAggregate(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urn", required = true) @QueryParam("urn") @NotNull String urn,
      @ApiParam(value = "start time (in millis)", required = true) @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true) @QueryParam("end") @NotNull long end,
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")") @QueryParam("offset") @DefaultValue(OFFSET_DEFAULT) String offset)
      throws Exception {

    MetricEntity metricEntity = MetricEntity.fromURN(urn);
    long metricId = metricEntity.getId();
    List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());

    double aggregate = computeAggregate(metricId, filters, start, end, offset, DateTimeZone.UTC);

    return Response.ok(aggregate).build();
  }

  /**
   * Returns a list of aggregate value for the specified metric and time range, and a list of
   * offset.
   * Aligns time stamps if necessary and returns NaN if no data is available for the given time
   * range.
   *
   * @param urn metric urn
   * @param start start time (in millis)
   * @param end end time (in millis)
   * @param offsets A list of offset identifier (e.g. "current", "wo2w")
   * @return aggregate value, or NaN if data not available
   * @throws Exception on catch-all execution failure
   * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
   */
  @GET
  @Path("/aggregate/batch")
  @ApiOperation(value = "Returns a list of aggregate value for the specified metric and time range, and (optionally) offset.")
  public Response getAggregateBatch(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urn", required = true) @QueryParam("urn") @NotNull String urn,
      @ApiParam(value = "start time (in millis)", required = true) @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true) @QueryParam("end") @NotNull long end,
      @ApiParam(value = "A list of offset identifier separated by comma (e.g. \"current\", \"wo2w\")") @QueryParam("offsets") List<String> offsets)
      throws Exception {
    MetricEntity metricEntity = MetricEntity.fromURN(urn);
    long metricId = metricEntity.getId();
    List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());

    List<Double> aggregates = computeAggregatesForOffsets(metricId,
        filters,
        start,
        end,
        offsets,
        DateTimeZone.UTC);

    return Response.ok(aggregates).build();
  }

  /**
   * Returns a map of lists of aggregate values for the specified metrics and time range, and a list
   * of offsets.
   * Aligns time stamps if necessary and returns NaN if no data is available for the given time
   * range.
   *
   * @param urns metric urns
   * @param start start time (in millis)
   * @param end end time (in millis)
   * @param offsets A list of offset identifiers (e.g. "current", "wo2w")
   * @return map of lists (keyed by urn) of aggregate values, or NaN if data not available
   * @throws Exception on catch-all execution failure
   * @see BaselineParsingUtils#parseOffset(String, DateTimeZone) supported offsets
   */
  @GET
  @Path("/aggregate/chunk")
  @ApiOperation(value = "Returns a map of lists (keyed by urn) of aggregate value for the specified metrics and time range, and offsets.")
  public Response getAggregateChunk(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urns", required = true) @QueryParam("urns") @NotNull List<String> urns,
      @ApiParam(value = "start time (in millis)", required = true) @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true) @QueryParam("end") @NotNull long end,
      @ApiParam(value = "A list of offset identifier separated by comma (e.g. \"current\", \"wo2w\")") @QueryParam("offsets") List<String> offsets)
      throws Exception {
    Map<String, List<Double>> urnToAggregates = new HashMap<>();
    for (String urn : urns) {
      MetricEntity metricEntity = MetricEntity.fromURN(urn);
      long metricId = metricEntity.getId();
      List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());
      urnToAggregates.put(urn,
          computeAggregatesForOffsets(metricId, filters, start, end, offsets, DateTimeZone.UTC));
    }

    return Response.ok(urnToAggregates).build();
  }

  /**
   * Returns a breakdown (de-aggregation) for the specified anomaly, and (optionally) offset.
   * Aligns time stamps if necessary and omits null values.
   *
   * @param anomalyId anomaly id
   * @param offset offset identifier (e.g. "current", "wo2w")
   * @param limit limit results to the top k elements, plus a rollup element
   * @return aggregate value, or NaN if data not available
   * @throws Exception on catch-all execution failure
   * @see BaselineParsingUtils#parseOffset(String, DateTimeZone) supported offsets
   */
  @GET
  @Path("/breakdown/anomaly/{id}")
  @ApiOperation(value =
      "Returns a breakdown (de-aggregation) for the specified anomaly, and (optionally) offset.\n"
          + "Aligns time stamps if necessary and omits null values.")
  @Deprecated
  public Response getAnomalyBreakdown(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long anomalyId,
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")")
      @QueryParam("offset") @DefaultValue(OFFSET_DEFAULT) String offset,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit) throws Exception {

    if (limit == null) {
      limit = LIMIT_DEFAULT;
    }
    Baseline range = parseOffset(offset, DateTimeZone.UTC);

    RootCauseAnalysisInfo rootCauseAnalysisInfo = rootCauseAnalysisInfoFetcher.getRootCauseAnalysisInfo(
        anomalyId);
    final Interval anomalyInterval = new Interval(
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getStartTime(),
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getEndTime(),
        DateTimeZone.UTC);

    final Map<String, Map<String, Double>> breakdown = computeBreakdown(
        rootCauseAnalysisInfo.getMetricConfigDTO().getId(),
        filters,
        anomalyInterval,
        range,
        limit,
        rootCauseAnalysisInfo.getDatasetConfigDTO().bucketTimeGranularity());
    return Response.ok(breakdown).build();
  }

  @GET
  @Path("/heatmap/anomaly/{id}")
  @ApiOperation(value = "Returns heatmap for the specified anomaly.\n Aligns time stamps if necessary and omits null values.")
  public Response getAnomalyHeatmap(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long anomalyId,
      @ApiParam(value = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").")
      @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit) throws Exception {

    if (limit == null) {
      limit = LIMIT_DEFAULT;
    }
    final RootCauseAnalysisInfo rootCauseAnalysisInfo = rootCauseAnalysisInfoFetcher.getRootCauseAnalysisInfo(
        anomalyId);
    final Interval currentInterval = new Interval(
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getStartTime(),
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getEndTime(),
        DateTimeZone.UTC);

    Period baselineOffsetPeriod = Period.parse(baselineOffset, ISOPeriodFormat.standard());
    final Interval baselineInterval = new Interval(
        currentInterval.getStart().minus(baselineOffsetPeriod),
        currentInterval.getEnd().minus(baselineOffsetPeriod)
    );

    final Map<String, Map<String, Double>> anomalyBreakdown = computeBreakdown(
        rootCauseAnalysisInfo.getMetricConfigDTO().getId(),
        filters,
        currentInterval,
        getSimpleRange(),
        limit,
        rootCauseAnalysisInfo.getDatasetConfigDTO().bucketTimeGranularity());

    final Map<String, Map<String, Double>> baselineBreakdown = computeBreakdown(
        rootCauseAnalysisInfo.getMetricConfigDTO().getId(),
        filters,
        baselineInterval,
        getSimpleRange(),
        limit,
        rootCauseAnalysisInfo.getDatasetConfigDTO().bucketTimeGranularity());

    final HeatMapResultApi resultApi = new HeatMapResultApi()
        .setMetric(new MetricApi()
            .setName(rootCauseAnalysisInfo.getMetricConfigDTO().getName())
            .setDataset(new DatasetApi().setName(rootCauseAnalysisInfo.getDatasetConfigDTO()
                .getName())))
        .setCurrent(new HeatMapBreakdownApi().setBreakdown(anomalyBreakdown))
        .setBaseline(new HeatMapBreakdownApi().setBreakdown(baselineBreakdown));

    return Response.ok(resultApi).build();
  }

  /**
   * Returns a baseline equivalent to "current" in the wo1w format.
   * Ie when used for a scatter/gather operation, this baseline will only generate one slice,
   * on the startTime/endTime provided.
   *
   * Hack to keep the compatibility with complex baselines.
   * May be removed once timeseries filtering and timeseries baseline is implemented.
   */
  private Baseline getSimpleRange() {
    return BaselineAggregate.fromWeekOverWeek(BaselineAggregateType.SUM,
        1,
        0,
        DateTimeZone.UTC);
  }

  private static void logSlices(MetricSlice baseSlice, List<MetricSlice> slices) {
    final DateTimeFormatter formatter = DateTimeFormat.forStyle("LL");
    LOG.info("RCA metric analysis - Base slice: {} - {}",
        formatter.print(baseSlice.getStart()),
        formatter.print(baseSlice.getEnd()));
    for (int i = 0; i < slices.size(); i++) {
      LOG.info("RCA metric analysis - Offset Slice {}:  {} - {}",
          i,
          formatter.print(slices.get(i).getStart()),
          formatter.print(slices.get(i).getEnd()));
    }
  }

  /**
   * Generates expected timestamps for the underlying time series and merges them with the
   * actual time series. This allows the front end to distinguish between un-expected and
   * missing data.
   * This allows to fill missing data points with null values.
   *
   * @param data time series dataframe
   * @param slice metric slice
   * @return time series dataframe with nulls for expected but missing data
   */
  private DataFrame fillIndex(DataFrame data, MetricSlice slice,
      DateTimeZone timezone) {
    if (data.size() <= 1) {
      return data;
    }
    TimeGranularity granularity = slice.getGranularity();

    long startMillis = data.getLongs(DataFrame.COL_TIME).min().longValue();
    long endMillis = slice.getEnd();
    Period stepSize = granularity.toPeriod();
    DateTime startDt = new DateTime(startMillis, timezone);
    List<Long> expectedTimestamps = new ArrayList<>();
    while (startDt.getMillis() < endMillis) {
      expectedTimestamps.add(startDt.getMillis());
      startDt = startDt.plus(stepSize);
    }

    DataFrame expectedTimestampsDf = new DataFrame(DataFrame.COL_TIME,
        LongSeries.buildFrom(expectedTimestamps.stream().mapToLong(l -> l).toArray()));

    return data.joinOuter(expectedTimestampsDf).sortedBy(DataFrame.COL_TIME);
  }

  private Map<String, Map<String, Double>> computeBreakdown(
      final long metricId,
      final List<String> filters,
      final Interval interval,
      final Baseline range,
      final int limit,
      final TimeGranularity timeGranularity) throws Exception {

    MetricSlice baseSlice = MetricSlice.from(metricId,
        interval.getStartMillis(),
        interval.getEndMillis(),
        filters,
        timeGranularity);

    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> dataBreakdown = fetchBreakdowns(slices, limit);
    Map<MetricSlice, DataFrame> dataAggregate = fetchAggregates(slices);

    DataFrame resultBreakdown = range.gather(baseSlice, dataBreakdown);
    DataFrame resultAggregate = range.gather(baseSlice, dataAggregate);

    return DefaultAggregationLoader.makeBreakdownMap(resultBreakdown, resultAggregate);
  }

  private double computeAggregate(final long metricId, final List<String> filters, final long start,
      final long end,
      final String offset,
      final DateTimeZone dateTimeZone) throws Exception {
    MetricSlice baseSlice = MetricSlice.from(metricId,
        start,
        end,
        filters,
        findMetricGranularity(metricId));
    Baseline range = parseOffset(offset, dateTimeZone);
    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);
    Map<MetricSlice, DataFrame> data = fetchAggregates(slices);
    DataFrame result = range.gather(baseSlice, data);
    if (result.isEmpty()) {
      return Double.NaN;
    }
    return result.getDouble(DataFrame.COL_VALUE, 0);
  }

  private List<Double> computeAggregatesForOffsets(final long metricId, final List<String> filters,
      final long start,
      final long end,
      final List<String> offsets, final DateTimeZone dateTimeZone) throws Exception {
    List<Double> aggregateValues = new ArrayList<>();
    for (String offset : offsets) {
      double value = computeAggregate(metricId, filters, start, end, offset, dateTimeZone);
      aggregateValues.add(value);
    }
    return aggregateValues;
  }

  /**
   * Returns aggregates for the given set of metric slices.
   *
   * @param slices metric slices
   * @return map of dataframes (keyed by metric slice, columns: [COL_TIME(1), COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private Map<MetricSlice, DataFrame> fetchAggregates(List<MetricSlice> slices) throws Exception {
    Map<MetricSlice, Future<DataFrame>> futures = new HashMap<>();
    for (final MetricSlice slice : slices) {
      futures.put(slice, this.executor.submit(() -> {
        final DataFrame df = aggregationLoader.loadAggregate(slice, Collections.emptyList(), -1);
        if (df.isEmpty()) {
          return new DataFrame().addSeries(DataFrame.COL_TIME, slice.getStart())
              .addSeries(DataFrame.COL_VALUE, Double.NaN);
        }
        return df;
      }));
    }

    return collectFutures(futures);
  }

  /**
   * Returns breakdowns (de-aggregations) for a given set of metric slices.
   *
   * @param slices metric slices
   * @param limit top k elements limit
   * @return map of dataframes (keyed by metric slice,
   *     columns: [COL_TIME(1), COL_DIMENSION_NAME, COL_DIMENSION_VALUE, COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private Map<MetricSlice, DataFrame> fetchBreakdowns(List<MetricSlice> slices, final int limit)
      throws Exception {
    Map<MetricSlice, Future<DataFrame>> futures = new HashMap<>();
    for (final MetricSlice slice : slices) {
      futures.put(slice, this.executor.submit(() -> aggregationLoader.loadBreakdown(slice, limit)));
    }

    return collectFutures(futures);
  }

  private Map<MetricSlice, DataFrame> collectFutures(
      final Map<MetricSlice, Future<DataFrame>> futures)
      throws Exception {
    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<DataFrame>> entry : futures.entrySet()) {
      output.put(entry.getKey(), entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    return output;
  }

  @Deprecated
  // prefer getting DatasetConfigDTO from RootCauseAnalysisInfo
  private TimeGranularity findMetricGranularity(final Long metricId) {
    final MetricConfigDTO metric = ensureExists(metricDAO.findById(metricId),
        String.format("metric id: %d", metricId));
    final DatasetConfigDTO dataset = ensureExists(datasetDAO.findByDataset(metric.getDataset()),
        String.format("dataset name: %s", metric.getDataset()));

    return dataset.bucketTimeGranularity();
  }
}
