package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils.parseOffset;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pinot.thirdeye.alert.AlertTemplateRenderer;
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
import org.apache.pinot.thirdeye.util.ResourceUtils;
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
 * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
 */
@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RootCauseMetricResource {

  private static final Logger LOG = LoggerFactory.getLogger(RootCauseMetricResource.class);

  private static final String COL_TIME = TimeSeriesLoader.COL_TIME;
  private static final String COL_VALUE = TimeSeriesLoader.COL_VALUE;
  private static final String COL_DIMENSION_NAME = AggregationLoader.COL_DIMENSION_NAME;
  private static final String COL_DIMENSION_VALUE = AggregationLoader.COL_DIMENSION_VALUE;

  private static final String ROLLUP_NAME = "OTHER";

  private static final long TIMEOUT = 600000;

  private static final String OFFSET_DEFAULT = "current";
  private static final String TIMEZONE_DEFAULT = "UTC";
  private static final String GRANULARITY_DEFAULT = MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString();
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
   * Returns a map of time series (keyed by series name) derived from the timeseries results
   * dataframe.
   *
   * @param data (transformed) query results
   * @return map of lists of double or long (keyed by series name)
   */
  private static Map<String, List<? extends Number>> makeTimeSeriesMap(DataFrame data) {
    Map<String, List<? extends Number>> output = new HashMap<>();
    output.put(COL_TIME, data.getLongs(COL_TIME).toList());
    output.put(COL_VALUE, data.getDoubles(COL_VALUE).toList());
    return output;
  }

  /**
   * Returns a map of maps (keyed by dimension name, keyed by dimension value) derived from the
   * breakdown results dataframe.
   *
   * @param dataBreakdown (transformed) breakdown query results
   * @param dataAggregate (transformed) aggregate query results
   * @return map of maps of value (keyed by dimension name, keyed by dimension value)
   */
  private static Map<String, Map<String, Double>> makeBreakdownMap(DataFrame dataBreakdown,
      DataFrame dataAggregate) {
    Map<String, Map<String, Double>> output = new TreeMap<>();

    dataBreakdown = dataBreakdown.dropNull();
    dataAggregate = dataAggregate.dropNull();

    Map<String, Double> dimensionTotals = new HashMap<>();

    for (int i = 0; i < dataBreakdown.size(); i++) {
      final String dimName = dataBreakdown.getString(COL_DIMENSION_NAME, i);
      final String dimValue = dataBreakdown.getString(COL_DIMENSION_VALUE, i);
      final double value = dataBreakdown.getDouble(COL_VALUE, i);

      // cell
      if (!output.containsKey(dimName)) {
        output.put(dimName, new HashMap<>());
      }
      output.get(dimName).put(dimValue, value);

      // total
      dimensionTotals.put(dimName, MapUtils.getDoubleValue(dimensionTotals, dimName, 0) + value);
    }

    // add rollup column
    if (!dataAggregate.isEmpty()) {
      double total = dataAggregate.getDouble(COL_VALUE, 0);
      for (Map.Entry<String, Double> entry : dimensionTotals.entrySet()) {
        if (entry.getValue() < total) {
          output.get(entry.getKey()).put(ROLLUP_NAME, total - entry.getValue());
        }
      }
    }

    return output;
  }

  private static void logSlices(MetricSlice baseSlice, List<MetricSlice> slices) {
    final DateTimeFormatter formatter = DateTimeFormat.forStyle("LL");
    LOG.info("{} - {} (base)",
        formatter.print(baseSlice.getStart()),
        formatter.print(baseSlice.getEnd()));
    for (MetricSlice slice : slices) {
      LOG.info("{} - {}", formatter.print(slice.getStart()), formatter.print(slice.getEnd()));
    }
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
   * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
   */
  @GET
  @Path("/aggregate")
  @ApiOperation(value = "Returns an aggregate value for the specified metric and time range, and (optionally) offset.")
  public double getAggregate(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urn", required = true) @QueryParam("urn") @NotNull String urn,
      @ApiParam(value = "start time (in millis)", required = true) @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true) @QueryParam("end") @NotNull long end,
      @ApiParam(value = "offset identifier (e.g. \"current\", \"wo2w\")") @QueryParam("offset") String offset,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")") @QueryParam("timezone") String timezone)
      throws Exception {
    if (StringUtils.isBlank(offset)) {
      offset = OFFSET_DEFAULT;
    }

    if (StringUtils.isBlank(timezone)) {
      timezone = TIMEZONE_DEFAULT;
    }

    MetricSlice baseSlice = alignSlice(makeSlice(urn, start, end), timezone);
    Baseline range = parseOffset(offset, timezone);

    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> data = fetchAggregates(slices);

    DataFrame result = range.gather(baseSlice, data);

    if (result.isEmpty()) {
      return Double.NaN;
    }
    return result.getDouble(COL_VALUE, 0);
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
  public List<Double> getAggregateBatch(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urn", required = true) @QueryParam("urn") @NotNull String urn,
      @ApiParam(value = "start time (in millis)", required = true) @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true) @QueryParam("end") @NotNull long end,
      @ApiParam(value = "A list of offset identifier separated by comma (e.g. \"current\", \"wo2w\")") @QueryParam("offsets") List<String> offsets,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")") @QueryParam("timezone") String timezone)
      throws Exception {
    List<Double> aggregateValues = new ArrayList<>();

    if (StringUtils.isBlank(timezone)) {
      timezone = TIMEZONE_DEFAULT;
    }

    offsets = ResourceUtils.parseListParams(offsets);
    List<MetricSlice> slices = new ArrayList<>();

    Map<String, MetricSlice> offsetToBaseSlice = new HashMap<>();
    Map<String, Baseline> offsetToRange = new HashMap<>();
    for (String offset : offsets) {
      MetricSlice baseSlice = alignSlice(makeSlice(urn, start, end), timezone);
      offsetToBaseSlice.put(offset, baseSlice);

      Baseline range = parseOffset(offset, timezone);
      offsetToRange.put(offset, range);

      List<MetricSlice> currentSlices = range.scatter(baseSlice);

      slices.addAll(currentSlices);
      logSlices(baseSlice, currentSlices);
    }

    // Fetch all aggregates
    Map<MetricSlice, DataFrame> data = fetchAggregates(slices);

    // Pick the results
    for (String offset : offsets) {
      DataFrame result = offsetToRange.get(offset).gather(offsetToBaseSlice.get(offset), data);
      if (result.isEmpty()) {
        aggregateValues.add(Double.NaN);
      } else {
        aggregateValues.add(result.getDouble(COL_VALUE, 0));
      }
    }
    return aggregateValues;
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
   * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
   */
  @GET
  @Path("/aggregate/chunk")
  @ApiOperation(value = "Returns a map of lists (keyed by urn) of aggregate value for the specified metrics and time range, and offsets.")
  public Map<String, Collection<Double>> getAggregateChunk(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "metric urns", required = true) @QueryParam("urns") @NotNull List<String> urns,
      @ApiParam(value = "start time (in millis)", required = true) @QueryParam("start") @NotNull long start,
      @ApiParam(value = "end time (in millis)", required = true) @QueryParam("end") @NotNull long end,
      @ApiParam(value = "A list of offset identifier separated by comma (e.g. \"current\", \"wo2w\")") @QueryParam("offsets") List<String> offsets,
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")") @QueryParam("timezone") String timezone)
      throws Exception {
    ListMultimap<String, Double> aggregateValues = ArrayListMultimap.create();

    if (StringUtils.isBlank(timezone)) {
      timezone = TIMEZONE_DEFAULT;
    }

    urns = ResourceUtils.parseListParams(urns);
    offsets = ResourceUtils.parseListParams(offsets);
    List<MetricSlice> slices = new ArrayList<>();

    Map<Pair<String, String>, MetricSlice> offsetToBaseSlice = new HashMap<>();
    Map<Pair<String, String>, Baseline> tupleToRange = new HashMap<>();
    for (String urn : urns) {
      for (String offset : offsets) {
        Pair<String, String> key = Pair.of(urn, offset);

        MetricSlice baseSlice = alignSlice(makeSlice(urn, start, end), timezone);
        offsetToBaseSlice.put(key, baseSlice);

        Baseline range = parseOffset(offset, timezone);
        tupleToRange.put(key, range);

        List<MetricSlice> currentSlices = range.scatter(baseSlice);

        slices.addAll(currentSlices);
        logSlices(baseSlice, currentSlices);
      }
    }

    // Fetch all aggregates
    Map<MetricSlice, DataFrame> data = fetchAggregates(slices);

    // Pick the results
    for (String urn : urns) {
      for (String offset : offsets) {
        Pair<String, String> key = Pair.of(urn, offset);
        DataFrame result = tupleToRange.get(key).gather(offsetToBaseSlice.get(key), data);
        if (result.isEmpty()) {
          aggregateValues.put(urn, Double.NaN);
        } else {
          aggregateValues.put(urn, result.getDouble(COL_VALUE, 0));
        }
      }
    }

    return aggregateValues.asMap();
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
   * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
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
      @ApiParam(value = "timezone identifier (e.g. \"America/Los_Angeles\")")
      @QueryParam("timezone") @DefaultValue(TIMEZONE_DEFAULT) String timezone,
      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit) throws Exception {

    if (limit == null) {
      limit = LIMIT_DEFAULT;
    }

    final MergedAnomalyResultDTO anomalyDTO = ensureExists(mergedAnomalyDAO.findById(id),
        String.format("Anomaly ID: %d", id));
    long detectionConfigId = anomalyDTO.getDetectionConfigId();
    AlertDTO alertDTO = alertDAO.findById(detectionConfigId);
    //startTime/endTime not important
    AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO, 0L, 0L);
    RcaMetadataDTO rcaMetadataDTO = templateWithProperties.getRca();
    String metric = Objects.requireNonNull(rcaMetadataDTO.getMetric(),
        "rca$metric not found in alert config.");
    String dataset = Objects.requireNonNull(rcaMetadataDTO.getDataset(),
        "rca$dataset not found in alert config.");
    // fixme cyril add datasource constraints
    MetricConfigDTO metricConfigDTO = metricDAO.findByMetricAndDataset(metric, dataset);
    String urn = MetricEntity.TYPE.formatURN(metricConfigDTO.getId());

    final Map<String, Map<String, Double>> breakdown = computeBreakdown(urn,
        anomalyDTO.getStartTime(),
        anomalyDTO.getEndTime(),
        offset,
        timezone,
        limit,
        //todo cyril deprecate native granularity?
        MetricSlice.NATIVE_GRANULARITY);
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
   * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
   */
  @GET
  @Path("/breakdown")
  @ApiOperation(value =
      "Returns a breakdown (de-aggregation) of the specified metric and time range, and (optionally) offset.\n"
          + "Aligns time stamps if necessary and omits null values.")
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
    final TimeGranularity granularity = MetricSlice.NATIVE_GRANULARITY;

    return Response.ok(computeBreakdown(urn, start, end, offset, timezone, limit, granularity))
        .build();
  }

  private Map<String, Map<String, Double>> computeBreakdown(
      final String urn,
      final long start,
      final long end,
      final String offset,
      final String timezone,
      final int limit,
      final TimeGranularity granularity) throws Exception {
    MetricSlice baseSlice = alignSlice(makeSlice(urn, start, end, granularity), timezone);
    Baseline range = parseOffset(offset, timezone);

    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> dataBreakdown = fetchBreakdowns(slices, limit);
    Map<MetricSlice, DataFrame> dataAggregate = fetchAggregates(slices);

    DataFrame resultBreakdown = range.gather(baseSlice, dataBreakdown);
    DataFrame resultAggregate = range.gather(baseSlice, dataAggregate);

    return makeBreakdownMap(resultBreakdown, resultAggregate);
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
   * @see BaselineParsingUtils#parseOffset(String, String) supported offsets
   */
  @GET
  @Path("/timeseries")
  @ApiOperation(value =
      "Returns a time series for the specified metric and time range, and (optionally) offset at an (optional)\n"
          + "time granularity. Aligns time stamps if necessary.")
  public Map<String, List<? extends Number>> getTimeSeries(
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

    if (StringUtils.isBlank(granularityString)) {
      granularityString = GRANULARITY_DEFAULT;
    }

    TimeGranularity granularity = TimeGranularity.fromString(granularityString);
    MetricSlice baseSlice = alignSlice(makeSlice(urn, start, end, granularity), timezone);
    Baseline range = parseOffset(offset, timezone);

    List<MetricSlice> slices = new ArrayList<>(range.scatter(baseSlice));
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> data = fetchTimeSeries(slices);
    DataFrame rawResult = range.gather(baseSlice, data);

    DataFrame imputedResult = this.imputeExpectedTimestamps(rawResult, baseSlice, timezone);

    return makeTimeSeriesMap(imputedResult);
  }

  /**
   * Generates expected timestamps for the underlying time series and merges them with the
   * actual time series. This allows the front end to distinguish between un-expected and
   * missing data.
   *
   * @param data time series dataframe
   * @param slice metric slice
   * @return time series dataframe with nulls for expected but missing data
   */
  private DataFrame imputeExpectedTimestamps(DataFrame data, MetricSlice slice, String timezone) {
    if (data.size() <= 1) {
      return data;
    }
    final MetricConfigDTO metric = ensureExists(metricDAO.findById(slice.getMetricId()),
        String.format("metric id: %d", slice.getMetricId()));

    final DatasetConfigDTO dataset = ensureExists(datasetDAO.findByDataset(metric.getDataset()),
        String.format("dataset name: %s", metric.getDataset()));

    TimeGranularity granularity = dataset.bucketTimeGranularity();
    if (!MetricSlice.NATIVE_GRANULARITY.equals(slice.getGranularity())
        && slice.getGranularity().toMillis() >= granularity.toMillis()) {
      granularity = slice.getGranularity();
    }

    DateTimeZone tz = DateTimeZone.forID(timezone);
    long start = data.getLongs(COL_TIME).min().longValue();
    long end = slice.getEnd();
    Period stepSize = granularity.toPeriod();

    DateTime current = new DateTime(start, tz);
    List<Long> timestamps = new ArrayList<>();
    while (current.getMillis() < end) {
      timestamps.add(current.getMillis());
      current = current.plus(stepSize);
    }

    LongSeries sExpected = LongSeries.buildFrom(ArrayUtils.toPrimitive(timestamps.toArray(new Long[timestamps
        .size()])));
    DataFrame dfExpected = new DataFrame(COL_TIME, sExpected);

    return data.joinOuter(dfExpected).sortedBy(COL_TIME);
  }

  /**
   * Returns aggregates for the given set of metric slices.
   *
   * @param slices metric slices
   * @return map of dataframes (keyed by metric slice, columns: [COL_TIME(1), COL_VALUE])
   * @throws Exception on catch-all execution failure
   */
  private Map<MetricSlice, DataFrame> fetchAggregates(List<MetricSlice> slices) throws Exception {
    Map<MetricSlice, Future<Double>> futures = new HashMap<>();

    for (final MetricSlice slice : slices) {
      futures.put(slice, this.executor.submit(() -> {
        DataFrame df = aggregationLoader.loadAggregate(slice, Collections.emptyList(), -1);
        if (df.isEmpty()) {
          return Double.NaN;
        }
        return df.getDouble(COL_VALUE, 0);
      }));
    }

    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<Double>> entry : futures.entrySet()) {
      MetricSlice slice = entry.getKey();
      double value = entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS);

      DataFrame data = new DataFrame(COL_TIME, LongSeries.buildFrom(slice.getStart()))
          .addSeries(COL_VALUE, value);
      output.put(slice, data);
    }

    return output;
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

    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<DataFrame>> entry : futures.entrySet()) {
      MetricSlice slice = entry.getKey();
      DataFrame value = entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS);

      DataFrame data = new DataFrame(value)
          .addSeries(COL_TIME, LongSeries.fillValues(value.size(), slice.getStart()))
          .setIndex(COL_TIME, COL_DIMENSION_NAME, COL_DIMENSION_VALUE);
      output.put(slice, data);
    }

    return output;
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

    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<DataFrame>> entry : futures.entrySet()) {
      output.put(entry.getKey(), entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    return output;
  }

  /**
   * Aligns a metric slice based on its granularity, or the dataset granularity.
   *
   * @param slice metric slice
   * @return aligned metric slice
   */
  // TODO refactor as util. similar to dataframe utils
  private MetricSlice alignSlice(MetricSlice slice, String timezone) {
    final MetricConfigDTO metric = ensureExists(metricDAO.findById(slice.getMetricId()),
        String.format("metric id: %d", slice.getMetricId()));

    final DatasetConfigDTO dataset = ensureExists(datasetDAO.findByDataset(metric.getDataset()),
        String.format("dataset name: %s", metric.getDataset()));

    TimeGranularity granularity = dataset.bucketTimeGranularity();
    if (!MetricSlice.NATIVE_GRANULARITY.equals(slice.getGranularity())) {
      granularity = slice.getGranularity();
    }

    // align to time buckets and request time zone
    final long offset = DateTimeZone.forID(timezone).getOffset(slice.getStart());
    final long granularityMillis = granularity.toMillis();
    final long start = ((slice.getStart() + offset + granularityMillis - 1) / granularityMillis)
        * granularityMillis
        - offset; // round up the start time to time granularity boundary of the requested time zone
    final long end = start + (slice.getEnd() - slice.getStart());

    return slice.withStart(start).withEnd(end).withGranularity(granularity);
  }

  private MetricSlice makeSlice(String urn, long start, long end) {
    return makeSlice(urn, start, end, MetricSlice.NATIVE_GRANULARITY);
  }

  private MetricSlice makeSlice(String urn, long start, long end, TimeGranularity granularity) {
    MetricEntity metric = MetricEntity.fromURN(urn);
    return MetricSlice.from(metric.getId(), start, end, metric.getFilters(), granularity);
  }
}
