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
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.rca.CohortComputation;
import ai.startree.thirdeye.rca.HeatmapCalculator;
import ai.startree.thirdeye.spi.api.BreakdownApi;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
  private static final int LIMIT_DEFAULT = 100;
  private static final String DEFAULT_BASELINE_OFFSET = "P1W";

  private final HeatmapCalculator heatmapCalculator;
  private final CohortComputation cohortComputation;

  @Inject
  public RcaMetricResource(final HeatmapCalculator heatmapCalculator,
      final CohortComputation cohortComputation) {
    this.heatmapCalculator = heatmapCalculator;
    this.cohortComputation = cohortComputation;
  }

  @GET
  @Path("/heatmap")
  @ApiOperation(value = "Returns heatmap for the specified anomaly.\n Aligns time stamps if necessary and omits null values.")
  public Response getAnomalyHeatmap(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly")
      @QueryParam("id") long anomalyId,

      @ApiParam(value = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").")
      @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,

      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,

      @ApiParam(value = "List of dimensions to use for the analysis. If empty, all dimensions of the datasets are used.")
      @QueryParam("dimensions") List<String> dimensions,

      @ApiParam(value = "List of dimensions to exclude from the analysis.")
      @QueryParam("excludedDimensions") List<String> excludedDimensions,

      @ApiParam(value = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit
  ) {
    try {
      if (limit == null) {
        limit = LIMIT_DEFAULT;
      }
      final HeatMapResponseApi resultApi = heatmapCalculator.compute(anomalyId,
          baselineOffset,
          filters,
          limit,
          dimensions,
          excludedDimensions);

      return respondOk(resultApi);
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleRcaAlgorithmException(e);
    }
    return null;
  }

  @POST
  @Path("/breakdown")
  @ApiOperation(value = "Builds cohorts based on threshold")
  public Response getAnomalyHeatmap(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      final BreakdownApi request) throws Exception {
    ensure(request.getThreshold() != null ^ request.getPercentage() != null,
        "Either threshold or percentage should be set but not both");
    final BreakdownApi resultApi = cohortComputation.computeBreakdown(request,
        List.of(),
        LIMIT_DEFAULT);
    return respondOk(resultApi);
  }
}
