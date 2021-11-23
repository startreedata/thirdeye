package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_DEPTH;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_EXCLUDED_DIMENSIONS;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_HIERARCHIES;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_ONE_SIDE_ERROR;
import static org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator.DEFAULT_TIMEZONE_ID;
import static org.apache.pinot.thirdeye.rootcause.MultiDimensionalSummaryConstants.CUBE_DEPTH;
import static org.apache.pinot.thirdeye.rootcause.MultiDimensionalSummaryConstants.CUBE_DIM_HIERARCHIES;
import static org.apache.pinot.thirdeye.rootcause.MultiDimensionalSummaryConstants.CUBE_EXCLUDED_DIMENSIONS;
import static org.apache.pinot.thirdeye.rootcause.MultiDimensionalSummaryConstants.CUBE_ONE_SIDE_ERROR;
import static org.apache.pinot.thirdeye.rootcause.MultiDimensionalSummaryConstants.CUBE_SUMMARY_SIZE;
import static org.apache.pinot.thirdeye.rootcause.RootCauseResourceConstants.BASELINE_END;
import static org.apache.pinot.thirdeye.rootcause.RootCauseResourceConstants.BASELINE_START;
import static org.apache.pinot.thirdeye.rootcause.RootCauseResourceConstants.CURRENT_END;
import static org.apache.pinot.thirdeye.rootcause.RootCauseResourceConstants.CURRENT_START;
import static org.apache.pinot.thirdeye.rootcause.RootCauseResourceConstants.METRIC_URN;
import static org.apache.pinot.thirdeye.rootcause.RootCauseResourceConstants.TIME_ZONE;
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
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DimensionAnalysisResultApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class DimensionAnalysisResource {

  private static final Logger LOG = LoggerFactory.getLogger(DimensionAnalysisResource.class);

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DataCubeSummaryCalculator dataCubeSummaryCalculator;

  @Inject
  public DimensionAnalysisResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final DataCubeSummaryCalculator dataCubeSummaryCalculator) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.dataCubeSummaryCalculator = dataCubeSummaryCalculator;
  }

  @GET
  @Path("anomaly/{id}")
  @ApiOperation("Retrieve the likely root causes behind an anomaly")
  public Response dataCubeSummary(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "internal id of the anomaly")
      @PathParam("id") long id) {
    final MergedAnomalyResultDTO anomalyDTO = ensureExists(
        mergedAnomalyResultManager.findById(id), String.format("Anomaly ID: %d", id));

    // In the highlights api we retrieve only the top 3 results across 3 dimensions.
    // TODO: polish the results to make it more meaningful
    return Response.ok(dataCubeSummaryCalculator.compute(anomalyDTO)).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildSummary(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam(METRIC_URN) String metricUrn,
      @QueryParam("dataset") String dataset,
      @QueryParam("metric") String metric,
      @QueryParam(CURRENT_START) long currentStartInclusive,
      @QueryParam(CURRENT_END) long currentEndExclusive,
      @QueryParam(BASELINE_START) long baselineStartInclusive,
      @QueryParam(BASELINE_END) long baselineEndExclusive,
      @QueryParam("dimensions") String groupByDimensions,
      @QueryParam("filters") String filterJsonPayload,
      @QueryParam(CUBE_SUMMARY_SIZE) int summarySize,
      @QueryParam(CUBE_DEPTH) @DefaultValue(DEFAULT_DEPTH) int depth,
      @QueryParam(CUBE_DIM_HIERARCHIES) @DefaultValue(DEFAULT_HIERARCHIES) String hierarchiesPayload,
      @QueryParam(CUBE_ONE_SIDE_ERROR) @DefaultValue(DEFAULT_ONE_SIDE_ERROR) boolean doOneSideError,
      @QueryParam(CUBE_EXCLUDED_DIMENSIONS) @DefaultValue(DEFAULT_EXCLUDED_DIMENSIONS) String excludedDimensions,
      @QueryParam(TIME_ZONE) @DefaultValue(DEFAULT_TIMEZONE_ID) String timeZone) {
    final DimensionAnalysisResultApi response = dataCubeSummaryCalculator.compute(metricUrn,
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
    return Response.ok(response).build();
  }
}
