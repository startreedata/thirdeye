/*
 * Copyright 2024 StarTree Inc
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
import static ai.startree.thirdeye.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.RcaMetricService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
@SecurityRequirement(name="oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaMetricResource {

  private static final Logger LOG = LoggerFactory.getLogger(RcaMetricResource.class);
  private static final int LIMIT_DEFAULT = 100;
  private static final String DEFAULT_BASELINE_OFFSET = "P1W";

  private final RcaMetricService rcaMetricService;

  @Inject
  public RcaMetricResource(final RcaMetricService rcaMetricService) {
    this.rcaMetricService = rcaMetricService;
  }

  @GET
  @Path("/heatmap")
  @Operation(summary = "Returns heatmap for the specified anomaly.\n Aligns time stamps if necessary and omits null values.")
  public Response getAnomalyHeatmap(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Parameter(description = "id of the anomaly")
      @QueryParam("id") long anomalyId,

      @Parameter(description = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").")
      @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,

      @Parameter(description = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,

      @Parameter(description = "List of dimensions to use for the analysis. If empty, all dimensions of the datasets are used.")
      @QueryParam("dimensions") List<String> dimensions,

      @Parameter(description = "List of dimensions to exclude from the analysis.")
      @QueryParam("excludedDimensions") List<String> excludedDimensions,

      @Parameter(description = "limit results to the top k elements, plus 'OTHER' rollup element")
      @QueryParam("limit") Integer limit
  ) {
    try {
      if (limit == null) {
        limit = LIMIT_DEFAULT;
      }
      final HeatMapResponseApi resultApi = rcaMetricService.computeHeatmap(
          principal,
          anomalyId,
          baselineOffset,
          filters,
          dimensions,
          excludedDimensions,
          limit);

      return respondOk(resultApi);
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleRcaAlgorithmException(e);
    }
    return null;
  }
}
