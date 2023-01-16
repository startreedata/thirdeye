/*
 * Copyright 2023 StarTree Inc
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
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.rca.RcaInfo;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rootcause.ContributorsFinderRunner;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
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
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaDimensionAnalysisResource {

  public static final String DEFAULT_HIERARCHIES = "[]";
  public static final String DEFAULT_ONE_SIDE_ERROR = "true";
  public static final String DEFAULT_CUBE_DEPTH_STRING = "3";
  public static final String DEFAULT_CUBE_SUMMARY_SIZE_STRING = "4";

  private static final Logger LOG = LoggerFactory.getLogger(RcaDimensionAnalysisResource.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String DEFAULT_BASELINE_OFFSET = "P1W";

  private final ContributorsFinderRunner contributorsFinderRunner;
  private final RcaInfoFetcher rcaInfoFetcher;

  @Inject
  public RcaDimensionAnalysisResource(
      final ContributorsFinderRunner contributorsFinderRunner,
      final RcaInfoFetcher rcaInfoFetcher) {
    this.contributorsFinderRunner = contributorsFinderRunner;
    this.rcaInfoFetcher = rcaInfoFetcher;
  }

  @GET
  @ApiOperation("Retrieve the likely root causes behind an anomaly")
  public Response dataCubeSummary(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @QueryParam("id") long anomalyId,
      @ApiParam(value = "baseline offset identifier in ISO 8601 format(e.g. \"P1W\").")
      @QueryParam("baselineOffset") @DefaultValue(DEFAULT_BASELINE_OFFSET) String baselineOffset,
      @ApiParam(value = "dimension filters (e.g. \"dim1=val1\", \"dim2!=val2\")")
      @QueryParam("filters") List<String> filters,
      @ApiParam(value = "Number of entries to put in the summary.")
      @QueryParam("summarySize") @DefaultValue(DEFAULT_CUBE_SUMMARY_SIZE_STRING) @Min(value = 1) int summarySize,
      @ApiParam(value = "Maximum number of dimensions to drill down by.")
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
  ) {
    try {
      final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(
          anomalyId);
      final Interval currentInterval = new Interval(
          rcaInfo.getAnomaly().getStartTime(),
          rcaInfo.getAnomaly().getEndTime(),
          rcaInfo.getTimezone());

      Period baselineOffsetPeriod = isoPeriod(baselineOffset);
      final Interval baselineInterval = new Interval(
          currentInterval.getStart().minus(baselineOffsetPeriod),
          currentInterval.getEnd().minus(baselineOffsetPeriod)
      );

      // override dimensions
      final DatasetConfigDTO datasetConfigDTO = rcaInfo.getDataset();
      List<String> rcaDimensions = getRcaDimensions(dimensions,
          excludedDimensions,
          datasetConfigDTO);
      datasetConfigDTO.setDimensions(Templatable.of(rcaDimensions));

      final List<List<String>> hierarchies = parseHierarchiesPayload(hierarchiesPayload);

      final ContributorsSearchConfiguration searchConfiguration = new ContributorsSearchConfiguration(
          rcaInfo.getMetric(),
          datasetConfigDTO,
          currentInterval,
          baselineInterval,
          summarySize,
          depth,
          doOneSideError,
          Predicate.parseAndCombinePredicates(filters),
          hierarchies);

      final ContributorsFinderResult result = contributorsFinderRunner.run(searchConfiguration);

      return Response.ok(result.getDimensionAnalysisResult()).build();
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleRcaAlgorithmException(e);
    }

    return null;
  }

  private List<List<String>> parseHierarchiesPayload(final String hierarchiesPayload)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(hierarchiesPayload, new TypeReference<>() {});
  }
}
