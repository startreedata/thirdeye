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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.core.ExceptionHandler.handleRcaAlgorithmException;
import static ai.startree.thirdeye.rca.RcaDimensionFilterHelper.getRcaDimensions;
import static ai.startree.thirdeye.spi.datalayer.Predicate.parseAndCombinePredicates;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rca.RootCauseAnalysisInfo;
import ai.startree.thirdeye.rootcause.BaselineAggregate;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.HeatMapResultApi;
import ai.startree.thirdeye.spi.api.HeatMapResultApi.HeatMapBreakdownApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.detection.Baseline;
import ai.startree.thirdeye.spi.detection.BaselineAggregateType;
import ai.startree.thirdeye.spi.metric.MetricSlice;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>RcaMetricResource is a central endpoint for querying different views on metrics as used
 * by the
 * RCA frontend. It delivers metric timeseries, aggregates, and breakdowns (de-aggregations).</p>
 */
@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(
        apiKeyAuthDefinitions = @ApiKeyAuthDefinition(
            name = HttpHeaders.AUTHORIZATION,
            in = ApiKeyLocation.HEADER,
            key = "oauth"
        )))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaMetricResource {

  private static final Logger LOG = LoggerFactory.getLogger(RcaMetricResource.class);
  private static final long TIMEOUT = 600000;
  private static final int LIMIT_DEFAULT = 100;
  private static final String DEFAULT_BASELINE_OFFSET = "P1W";

  private final ExecutorService executor;
  private final AggregationLoader aggregationLoader;
  private final RcaInfoFetcher rcaInfoFetcher;

  @Inject
  public RcaMetricResource(final AggregationLoader aggregationLoader,
      final RcaInfoFetcher rcaInfoFetcher) {
    this.aggregationLoader = aggregationLoader;
    this.rcaInfoFetcher = rcaInfoFetcher;

    this.executor = Executors.newCachedThreadPool();
  }

  private static void logSlices(MetricSlice baseSlice, List<MetricSlice> slices) {
    final DateTimeFormatter formatter = DateTimeFormat.forStyle("LL");
    LOG.info("RCA metric analysis - Base slice: {} - {}",
        formatter.print(baseSlice.getStartMillis()),
        formatter.print(baseSlice.getEndMillis()));
    for (int i = 0; i < slices.size(); i++) {
      LOG.info("RCA metric analysis - Offset Slice {}:  {} - {}",
          i,
          formatter.print(slices.get(i).getStartMillis()),
          formatter.print(slices.get(i).getEndMillis()));
    }
  }

  @GET
  @Path("/heatmap")
  @ApiOperation(value = "Returns heatmap for the specified anomaly.\n Aligns time stamps if necessary and omits null values.")
  public Response getAnomalyHeatmap(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @QueryParam("id") long anomalyId,
      @ApiParam(value = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").") @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")") @QueryParam("filters") List<String> filters,
      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element") @QueryParam("limit") Integer limit,
      @ApiParam(value = "List of dimensions to use for the analysis. If empty, all dimensions of the datasets are used.") @QueryParam("dimensions") List<String> dimensions,
      @ApiParam(value = "List of dimensions to exclude from the analysis.") @QueryParam("excludedDimensions") List<String> excludedDimensions) {
    try {
      if (limit == null) {
        limit = LIMIT_DEFAULT;
      }
      final RootCauseAnalysisInfo rootCauseAnalysisInfo = rcaInfoFetcher.getRootCauseAnalysisInfo(
          anomalyId);
      final Interval currentInterval = new Interval(rootCauseAnalysisInfo.getMergedAnomalyResultDTO()
          .getStartTime(),
          rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getEndTime(),
          rootCauseAnalysisInfo.getTimezone());

      Period baselineOffsetPeriod = Period.parse(baselineOffset, ISOPeriodFormat.standard());
      final Interval baselineInterval = new Interval(currentInterval.getStart()
          .minus(baselineOffsetPeriod), currentInterval.getEnd().minus(baselineOffsetPeriod));

      // override dimensions
      final DatasetConfigDTO datasetConfigDTO = rootCauseAnalysisInfo.getDatasetConfigDTO();
      List<String> rcaDimensions = getRcaDimensions(dimensions,
          excludedDimensions,
          datasetConfigDTO);
      datasetConfigDTO.setDimensions(Templatable.of(rcaDimensions));

      final Map<String, Map<String, Double>> anomalyBreakdown = computeBreakdown(
          rootCauseAnalysisInfo.getMetricConfigDTO(),
          parseAndCombinePredicates(filters),
          currentInterval,
          getSimpleRange(),
          limit,
          datasetConfigDTO);

      final Map<String, Map<String, Double>> baselineBreakdown = computeBreakdown(
          rootCauseAnalysisInfo.getMetricConfigDTO(),
          parseAndCombinePredicates(filters),
          baselineInterval,
          getSimpleRange(),
          limit,
          datasetConfigDTO);

      // if a dimension value is not observed in a breakdown but observed in the other, add it with a count of 0
      fillMissingKeysWithZeroes(baselineBreakdown, anomalyBreakdown);
      fillMissingKeysWithZeroes(anomalyBreakdown, baselineBreakdown);

      final HeatMapResultApi resultApi = new HeatMapResultApi().setMetric(new MetricApi().setName(
                  rootCauseAnalysisInfo.getMetricConfigDTO().getName())
              .setDataset(new DatasetApi().setName(datasetConfigDTO.getDataset())))
          .setCurrent(new HeatMapBreakdownApi().setBreakdown(anomalyBreakdown))
          .setBaseline(new HeatMapBreakdownApi().setBreakdown(baselineBreakdown));

      return Response.ok(resultApi).build();
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleRcaAlgorithmException(e);
    }

    return null;
  }

  /**
   * Inserts all keys that are present in fromBreakdown but absent in toBreakdown, with a value of
   * 0.
   */
  private void fillMissingKeysWithZeroes(final Map<String, Map<String, Double>> fromBreakdown,
      final Map<String, Map<String, Double>> toBreakdown) {
    // all keys that are present in fromBreakdown but not in toBreakdown are inserted with a value of 0
    for (String dimensionName : fromBreakdown.keySet()) {
      if (!toBreakdown.containsKey(dimensionName)) {
        toBreakdown.put(dimensionName, new HashMap<>());
      }
      Map<String, Double> fromCounts = fromBreakdown.get(dimensionName);
      Map<String, Double> toCounts = toBreakdown.get(dimensionName);
      for (String dimensionValue : fromCounts.keySet()) {
        if (!toCounts.containsKey(dimensionValue)) {
          toCounts.put(dimensionValue, 0.);
        }
      }
    }
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
    return BaselineAggregate.fromWeekOverWeek(BaselineAggregateType.SUM, 1, 0, DateTimeZone.UTC);
  }

  private Map<String, Map<String, Double>> computeBreakdown(final MetricConfigDTO metricConfigDTO,
      final List<Predicate> predicates, final Interval interval, final Baseline range,
      final int limit, final DatasetConfigDTO datasetConfigDTO) throws Exception {

    MetricSlice baseSlice = MetricSlice.from(metricConfigDTO,
        interval,
        predicates,
        datasetConfigDTO);

    List<MetricSlice> slices = range.scatter(baseSlice);
    logSlices(baseSlice, slices);

    Map<MetricSlice, DataFrame> dataBreakdown = fetchBreakdowns(slices, limit);
    Map<MetricSlice, DataFrame> dataAggregate = fetchAggregates(slices);

    DataFrame resultBreakdown = range.gather(baseSlice, dataBreakdown);
    DataFrame resultAggregate = range.gather(baseSlice, dataAggregate);

    return DefaultAggregationLoader.makeBreakdownMap(resultBreakdown, resultAggregate);
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
      futures.put(slice, aggregationLoader.loadAggregateAsync(slice, Collections.emptyList(), 2));
    }

    Map<MetricSlice, DataFrame> output = new HashMap<>();
    for (Map.Entry<MetricSlice, Future<DataFrame>> entry : futures.entrySet()) {
      final MetricSlice slice = entry.getKey();
      DataFrame df = entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS);
      if (df.isEmpty()) {
        df = new DataFrame().addSeries(Constants.COL_VALUE, Double.NaN);
      } else if (df.size() > 1) {
        throw new RuntimeException("Aggregation returned more than 1 line.");
      }
      // fill in timestamps
      df.addSeries(Constants.COL_TIME,
              LongSeries.fillValues(df.size(), slice.getInterval().getStartMillis()))
          .setIndex(Constants.COL_TIME);

      output.put(entry.getKey(), df);
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
      output.put(entry.getKey(), entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    return output;
  }
}
