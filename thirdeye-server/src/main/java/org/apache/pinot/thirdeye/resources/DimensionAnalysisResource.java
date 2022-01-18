package org.apache.pinot.thirdeye.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator;
import org.apache.pinot.thirdeye.rca.RootCauseAnalysisInfo;
import org.apache.pinot.thirdeye.rca.RootCauseAnalysisInfoFetcher;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class DimensionAnalysisResource {

  private static final Logger LOG = LoggerFactory.getLogger(DimensionAnalysisResource.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String DEFAULT_BASELINE_OFFSET = "P1W";
  public static final String DEFAULT_HIERARCHIES = "[]";
  public static final String DEFAULT_ONE_SIDE_ERROR = "false";
  public static final String DEFAULT_CUBE_DEPTH_STRING = "3";
  public static final String DEFAULT_CUBE_SUMMARY_SIZE_STRING = "4";

  private final DataCubeSummaryCalculator dataCubeSummaryCalculator;
  private final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher;

  @Inject
  public DimensionAnalysisResource(
      final DataCubeSummaryCalculator dataCubeSummaryCalculator,
      final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher) {
    this.dataCubeSummaryCalculator = dataCubeSummaryCalculator;
    this.rootCauseAnalysisInfoFetcher = rootCauseAnalysisInfoFetcher;
  }

  @GET
  @Path("anomaly/{id}")
  @ApiOperation("Retrieve the likely root causes behind an anomaly")
  public Response dataCubeSummary(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long anomalyId,
      @ApiParam(value = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").")
      @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @ApiParam(value = "Number of entries to put in the summary.")
      @QueryParam("summarySize") @DefaultValue(DEFAULT_CUBE_SUMMARY_SIZE_STRING) @Min(value = 1) int summarySize,
      @ApiParam(value = "Number of dimensions to drill down by.")
      @QueryParam("depth") @DefaultValue(DEFAULT_CUBE_DEPTH_STRING) int depth,
      @ApiParam(value = "If true, only returns changes that have the same direction as the global change.")
      @QueryParam("oneSideError") @DefaultValue(DEFAULT_ONE_SIDE_ERROR) boolean doOneSideError,
      @ApiParam(value = "List of dimensions to use for the analysis. If empty, all dimensions of the datasets are used.")
      @QueryParam("dimensions") List<String> dimensions,
      @ApiParam(value = "List of dimensions to exclude from the analysis.")
      @QueryParam("excludedDimensions") List<String> excludedDimensions,
      @ApiParam(value =
          "Hierarchy among some dimensions. The order will be respected in the result. "
              + "An example of a hierarchical group is {continent, country}. "
              + "Parameter format is [[\"continent\",\"country\"], [\"dim1\", \"dim2\", \"dim3\"]]")
      @QueryParam("hierarchies") @DefaultValue(DEFAULT_HIERARCHIES) String hierarchiesPayload
  ) throws Exception {
    RootCauseAnalysisInfo rootCauseAnalysisInfo = rootCauseAnalysisInfoFetcher.getRootCauseAnalysisInfo(
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

    dimensions = cleanDimensionStrings(dimensions);
    if (dimensions.isEmpty()) {
      dimensions = rootCauseAnalysisInfo.getDatasetConfigDTO().getDimensions();
    }
    excludedDimensions = cleanDimensionStrings(excludedDimensions);

    final List<List<String>> hierarchies = parseHierarchiesPayload(hierarchiesPayload);

    DimensionAnalysisResultApi resultApi = dataCubeSummaryCalculator.computeCube(
        rootCauseAnalysisInfo.getMetricConfigDTO().getName(),
        rootCauseAnalysisInfo.getDatasetConfigDTO().getName(),
        currentInterval,
        baselineInterval,
        summarySize,
        depth,
        doOneSideError,
        rootCauseAnalysisInfo.getMetricConfigDTO().getDerivedMetricExpression(),
        dimensions,
        excludedDimensions,
        filters,
        hierarchies
    );

    return Response.ok(resultApi).build();
  }

  private static List<String> cleanDimensionStrings(List<String> dimensions) {
    return dimensions.stream().map(String::trim).collect(Collectors.toList());
  }

  private List<List<String>> parseHierarchiesPayload(final String hierarchiesPayload)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(hierarchiesPayload, new TypeReference<>() {});
  }
}
