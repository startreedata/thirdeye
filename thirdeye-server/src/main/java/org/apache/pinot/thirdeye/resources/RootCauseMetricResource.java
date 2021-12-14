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
import java.util.Objects;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.alert.AlertTemplateRenderer;
import org.apache.pinot.thirdeye.datasource.loader.DefaultAggregationLoader;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RcaMetadataDTO;
import org.apache.pinot.thirdeye.spi.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.spi.datasource.loader.TimeSeriesLoader;
import org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
  private static final String TIMEZONE_DEFAULT = "UTC";
  private static final int LIMIT_DEFAULT = 100;

  private final ExecutorService executor;
  private final AggregationLoader aggregationLoader;
  private final TimeSeriesLoader timeSeriesLoader;
  private final MetricConfigManager metricDAO;
  private final DatasetConfigManager datasetDAO;
  private final MergedAnomalyResultManager mergedAnomalyDAO;
  private final AlertManager alertDAO;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public RootCauseMetricResource(final AggregationLoader aggregationLoader,
      final TimeSeriesLoader timeSeriesLoader,
      final MetricConfigManager metricDAO,
      final DatasetConfigManager datasetDAO,
      final MergedAnomalyResultManager mergedAnomalyDAO,
      final AlertManager alertDAO,
      final AlertTemplateRenderer alertTemplateRenderer) {
    this.aggregationLoader = aggregationLoader;
    this.timeSeriesLoader = timeSeriesLoader;
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.mergedAnomalyDAO = mergedAnomalyDAO;
    this.alertDAO = alertDAO;
    this.alertTemplateRenderer = alertTemplateRenderer;

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
   * @param timezone timezone identifier (e.g. "America/Los_Angeles")
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
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")") @QueryParam("offset") @DefaultValue(OFFSET_DEFAULT) String offset,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")") @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone)
      throws Exception {

    DateTimeZone dateTimeZone = parseTimeZone(timezone);
    MetricEntity metricEntity = MetricEntity.fromURN(urn);
    long metricId = metricEntity.getId();
    List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());

    double aggregate = computeAggregate(metricId, filters, start, end, offset, dateTimeZone);

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
   * @param timezone timezone identifier (e.g. "America/Los_Angeles")
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
      @ApiParam(value = "A list of offset identifier separated by comma (e.g. \"current\", \"wo2w\")") @QueryParam("offsets") List<String> offsets,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")") @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone)
      throws Exception {
    DateTimeZone dateTimeZone = parseTimeZone(timezone);
    MetricEntity metricEntity = MetricEntity.fromURN(urn);
    long metricId = metricEntity.getId();
    List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());

    List<Double> aggregates = computeAggregatesForOffsets(metricId,
        filters,
        start,
        end,
        offsets,
        dateTimeZone);

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
   * @param timezone timezone identifier (e.g. "America/Los_Angeles")
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
      @ApiParam(value = "A list of offset identifier separated by comma (e.g. \"current\", \"wo2w\")") @QueryParam("offsets") List<String> offsets,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")") @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone)
      throws Exception {
    DateTimeZone dateTimeZone = parseTimeZone(timezone);

    Map<String, List<Double>> urnToAggregates = new HashMap<>();
    for (String urn : urns) {
      MetricEntity metricEntity = MetricEntity.fromURN(urn);
      long metricId = metricEntity.getId();
      List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());
      urnToAggregates.put(urn,
          computeAggregatesForOffsets(metricId, filters, start, end, offsets, dateTimeZone));
    }

    return Response.ok(urnToAggregates).build();
  }

  /**
   * Returns a breakdown (de-aggregation) for the specified anomaly, and (optionally) offset.
   * Aligns time stamps if necessary and omits null values.
   *
   * @param id anomaly id
   * @param offset offset identifier (e.g. "current", "wo2w")
   * @param timezone timezone identifier (e.g. "America/Los_Angeles")
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
  public Response getAnomalyBreakdown(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long id,
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")")
      @QueryParam("offset") @DefaultValue(OFFSET_DEFAULT) String offset,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")")
      @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone,
      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit) throws Exception {

    if (limit == null) {
      limit = LIMIT_DEFAULT;
    }
    DateTimeZone dateTimeZone = parseTimeZone(timezone);

    final MergedAnomalyResultDTO anomalyDTO = ensureExists(mergedAnomalyDAO.findById(id),
        String.format("Anomaly ID: %d", id));
    long detectionConfigId = anomalyDTO.getDetectionConfigId();
    AlertDTO alertDTO = alertDAO.findById(detectionConfigId);
    //startTime/endTime not important
    AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO, 0L, 0L);
    RcaMetadataDTO rcaMetadataDTO = Objects.requireNonNull(templateWithProperties.getRca(),
        "rca not found in alert config.");
    String metric = Objects.requireNonNull(rcaMetadataDTO.getMetric(),
        "rca$metric not found in alert config.");
    String dataset = Objects.requireNonNull(rcaMetadataDTO.getDataset(),
        "rca$dataset not found in alert config.");
    MetricConfigDTO metricConfigDTO = metricDAO.findByMetricAndDataset(metric, dataset);

    final Map<String, Map<String, Double>> breakdown = computeBreakdown(metricConfigDTO.getId(),
        filters,
        anomalyDTO.getStartTime(),
        anomalyDTO.getEndTime(),
        offset,
        dateTimeZone,
        limit);
    return Response.ok(breakdown).build();
  }

  /**
   * Returns a breakdown (de-aggregation) of the specified metric and time range, and (optionally)
   * offset.
   * Aligns time stamps if necessary and omits null values.
   *
   * @param urn metric urn
   * @param start start time (in millis)
   * @param end end time (in millis)
   * @param offset offset identifier (e.g. "current", "wo2w")
   * @param timezone timezone identifier (e.g. "America/Los_Angeles")
   * @param limit limit results to the top k elements, plus a rollup element
   * @return aggregate value, or NaN if data not available
   * @throws Exception on catch-all execution failure
   * @see BaselineParsingUtils#parseOffset(String, DateTimeZone) supported offsets
   */
  @GET
  @Path("/breakdown")
  @ApiOperation(value =
      "Returns a breakdown (de-aggregation) of the specified metric and time range, and (optionally) offset.\n"
          + "Aligns time stamps if necessary and omits null values.")
  @Deprecated
  public Response getBreakdown(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urn", required = true)
      @QueryParam("urn") @NotNull String urn,
      @ApiParam(value = "start time (in millis)", required = true)
      @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true)
      @QueryParam("end") @NotNull long end,
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")")
      @QueryParam("offset") @DefaultValue(OFFSET_DEFAULT) String offset,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")")
      @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone,
      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit) throws Exception {

    if (limit == null) {
      limit = LIMIT_DEFAULT;
    }
    DateTimeZone dateTimeZone = parseTimeZone(timezone);
    MetricEntity metricEntity = MetricEntity.fromURN(urn);
    long metricId = metricEntity.getId();
    List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());

    final Map<String, Map<String, Double>> breakdown = computeBreakdown(metricId,
        filters,
        start,
        end,
        offset,
        dateTimeZone,
        limit);

    return Response.ok(breakdown).build();
  }

  /**
   * Returns a time series for the specified metric and time range, and (optionally) offset at an
   * (optional)
   * time granularity. Aligns time stamps if necessary.
   *
   * @param urn metric urn
   * @param start start time (in millis)
   * @param end end time (in millis)
   * @param offset offset identifier (e.g. "current", "wo2w")
   * @param timezone timezone identifier (e.g. "America/Los_Angeles")
   * @param granularityString time granularity (e.g. "5_MINUTES", "1_HOURS")
   * @return aggregate value, or NaN if data not available
   * @throws Exception on catch-all execution failure
   * @see BaselineParsingUtils#parseOffset(String, DateTimeZone) supported offsets
   */
  @GET
  @Path("/timeseries")
  @ApiOperation(value =
      "Returns a time series for the specified metric and time range, and (optionally) offset at an (optional)\n"
          + "time granularity. Aligns time stamps if necessary.")
  public Response getTimeSeries(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urn", required = true)
      @QueryParam("urn") @NotNull String urn,
      @ApiParam(value = "start time (in millis)", required = true)
      @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true)
      @QueryParam("end") @NotNull long end,
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")")
      @QueryParam("offset") @DefaultValue(OFFSET_DEFAULT) String offset,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")")
      @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone,
      @ApiParam(value = "limit results to the top k elements, plus an 'OTHER' rollup element")
      @QueryParam("granularity") String granularityString) throws Exception {

    DateTimeZone dateTimeZone = parseTimeZone(timezone);
    MetricEntity metricEntity = MetricEntity.fromURN(urn);
    long metricId = metricEntity.getId();
    List<String> filters = EntityUtils.encodeDimensions(metricEntity.getFilters());

    final Map<String, List<? extends Number>> timeseries = computeTimeseries(metricId,
        filters,
        start,
        end,
        offset,
        granularityString,
        dateTimeZone);

    return Response.ok(timeseries).build();
  }

  private DateTimeZone parseTimeZone(final String timezone) {
    return DateTimeZone.forID(timezone);
  }

  /**
   * Returns a map of time series (keyed by series name) derived from the timeseries results
   * dataframe.
   *
   * @param data (transformed) query results
   * @return map of lists of double or long (keyed by series name)
   */
  private static Map<String, List<? extends Number>> makeTimeSeriesMap(DataFrame data) {
    Map<String, List<? extends Number>> output = new HashMap<>();
    output.put(DataFrame.COL_TIME, data.getLongs(DataFrame.COL_TIME).toList());
    output.put(DataFrame.COL_VALUE, data.getDoubles(DataFrame.COL_VALUE).toList());
    return output;
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

  private Map<String, List<? extends Number>> computeTimeseries(final long metricId,
      final List<String> filters, final long start, final long end, final String offset,
      final String granularityString, final DateTimeZone dateTimeZone
  ) throws Exception {
    TimeGranularity granularity = StringUtils.isBlank(granularityString) ?
        findMetricGranularity(metricId) :
        TimeGranularity.fromString(granularityString);
    MetricSlice baseSlice = MetricSlice.from(metricId, start, end, filters, granularity)
        .alignedOn(dateTimeZone);
    Baseline range = parseOffset(offset, dateTimeZone);
    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> data = fetchTimeSeries(slices);
    DataFrame rawResult = range.gather(baseSlice, data);
    DataFrame imputedResult = fillIndex(rawResult, baseSlice, dateTimeZone);

    return makeTimeSeriesMap(imputedResult);
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
      final long start,
      final long end,
      final String offset,
      final DateTimeZone timezone,
      final int limit) throws Exception {

    MetricSlice baseSlice = MetricSlice.from(metricId,
            start,
            end,
            filters,
            findMetricGranularity(metricId))
        .alignedOn(timezone);
    Baseline range = parseOffset(offset, timezone);

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
            findMetricGranularity(metricId))
        .alignedOn(dateTimeZone);
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

  /**
   * Returns timeseries for a given set of metric slices.
   *
   * @param slices metric slices
   * @return map of dataframes (keyed by metric slice, columns: [COL_TIME(N), COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private Map<MetricSlice, DataFrame> fetchTimeSeries(List<MetricSlice> slices) throws Exception {
    Map<MetricSlice, Future<DataFrame>> futures = new HashMap<>();
    for (final MetricSlice slice : slices) {
      futures.put(slice, this.executor.submit(() -> timeSeriesLoader.load(slice)));
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

  private TimeGranularity findMetricGranularity(final Long metricId) {
    final MetricConfigDTO metric = ensureExists(metricDAO.findById(metricId),
        String.format("metric id: %d", metricId));
    final DatasetConfigDTO dataset = ensureExists(datasetDAO.findByDataset(metric.getDataset()),
        String.format("dataset name: %s", metric.getDataset()));

    return dataset.bucketTimeGranularity();
  }
}
