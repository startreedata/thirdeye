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

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.RcaDimensionAnalysisService;
import ai.startree.thirdeye.spi.Constants;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Tag(name = "Root Cause Analysis")
@SecurityRequirement(name = "oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaDimensionAnalysisResource {

  public static final String DEFAULT_HIERARCHIES = "[]";
  public static final String DEFAULT_ONE_SIDE_ERROR = "true";
  public static final String DEFAULT_CUBE_DEPTH_STRING = "3";
  public static final String DEFAULT_CUBE_SUMMARY_SIZE_STRING = "4";

  private static final String DEFAULT_BASELINE_OFFSET = "P1W";

  private final RcaDimensionAnalysisService rcaDimensionAnalysisService;

  @Inject
  public RcaDimensionAnalysisResource(
      final RcaDimensionAnalysisService rcaDimensionAnalysisService) {
    this.rcaDimensionAnalysisService = rcaDimensionAnalysisService;
  }

  @GET
  @Operation(summary = "Retrieve the likely root causes behind an anomaly")
  public Response dataCubeSummary(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Parameter(description = "id of the anomaly") @QueryParam("id") long anomalyId,
      @Parameter(description = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").")
      @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,
      @Parameter(description = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @Parameter(description = "Number of entries to put in the summary.")
      @QueryParam("summarySize") @DefaultValue(DEFAULT_CUBE_SUMMARY_SIZE_STRING) @Min(value = 1) int summarySize,
      @Parameter(description = "Maximum number of dimensions to drill down by.")
      @QueryParam("depth") @DefaultValue(DEFAULT_CUBE_DEPTH_STRING) int depth,
      @Parameter(description = "If true, only returns changes that have the same direction as the global change.")
      @QueryParam("oneSideError") @DefaultValue(DEFAULT_ONE_SIDE_ERROR) boolean doOneSideError,
      @Parameter(description = "List of dimensions to use for the analysis. If empty, all dimensions of the datasets are used.")
      @QueryParam("dimensions") List<String> dimensions,
      @Parameter(description = "List of dimensions to exclude from the analysis.")
      @QueryParam("excludedDimensions") List<String> excludedDimensions,
      @Parameter(description =
          "Hierarchy among some dimensions. The order will be respected in the result. "
              + "An example of a hierarchical group is {continent, country}. "
              + "Parameter format is [[\"continent\",\"country\"], [\"dim1\", \"dim2\", \"dim3\"]]")
      @QueryParam("hierarchies") @DefaultValue(DEFAULT_HIERARCHIES) String hierarchiesPayload
  ) throws Exception {
    return Response.ok(rcaDimensionAnalysisService.dataCubeSummary(
          principal,
          anomalyId,
          baselineOffset,
          filters,
          summarySize,
          depth,
          doOneSideError,
          dimensions,
          excludedDimensions,
          hierarchiesPayload)).build();
  }
}
