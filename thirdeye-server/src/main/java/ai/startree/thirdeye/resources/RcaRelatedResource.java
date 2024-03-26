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

import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.rootcause.events.IntervalSimilarityScoring;
import ai.startree.thirdeye.service.RcaRelatedService;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.api.RelatedAnomaliesAnalysisApi;
import ai.startree.thirdeye.spi.api.RelatedEventsAnalysisApi;
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
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SecurityRequirement(name = "oauth")
@OpenAPIDefinition(security = {@SecurityRequirement(name = "oauth")})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaRelatedResource {

  private static final String DEFAULT_EVENTS_LOOKAROUND = "P7D";
  private static final String DEFAULT_ANOMALIES_LOOKAROUND = "P4D";
  private static final String DEFAULT_LIMIT = "50";
  private static final String DEFAULT_SCORING = "TRIANGULAR";

  private final RcaRelatedService rcaRelatedService;

  @Inject
  public RcaRelatedResource(final RcaRelatedService rcaRelatedService) {
    this.rcaRelatedService = rcaRelatedService;
  }

  @GET
  @Path("/events")
  @Operation(summary = "Returns calendar events related to the anomaly. Events are ordered by the scoring function.")
  public Response getRelatedEvents(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Type of event.") @QueryParam("type") @Nullable String type,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_EVENTS_LOOKAROUND) String lookaround)
      throws IOException, ClassNotFoundException {

    final List<EventApi> relatedEvents = rcaRelatedService.getRelatedEvents(anomalyId, type,
        scoring, limit, isoPeriod(lookaround));
    return Response.ok(relatedEvents).build();
  }

  // TODO experimental - deprecate the endpoint above and add tests
  @GET
  @Path("/events-analysis")
  @Operation(summary = "Returns calendar events related to the anomaly. Events are ordered by the scoring function.")
  public Response getEventsAnalysis(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Type of event.") @QueryParam("type") @Nullable String type,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_EVENTS_LOOKAROUND) String lookaround)
      throws IOException, ClassNotFoundException {

    final RelatedEventsAnalysisApi res = rcaRelatedService.getEventsAnalysis(principal, 
        anomalyId, type,
        scoring, limit, isoPeriod(lookaround));

    return Response.ok(res).build();
  }

  @GET
  @Path("/anomalies")
  @Operation(summary = "Returns anomalies related to the anomaly. Anomalies are ordered by the scoring function.")
  public Response getAnomaliesEvents(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_ANOMALIES_LOOKAROUND) String lookaround)
      throws IOException, ClassNotFoundException {
    final List<AnomalyApi> res = rcaRelatedService.getRelatedAnomalies(anomalyId, scoring, limit,
        isoPeriod(lookaround));
    return Response.ok(res).build();
  }

  // TODO experimental - deprecate the endpoint above and add tests
  @GET
  @Path("/anomalies-analysis")
  @Operation(summary = "Returns anomalies related to the anomaly. Anomalies are ordered by the scoring function.")
  public Response getAnomaliesAnalysis(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_ANOMALIES_LOOKAROUND) String lookaround)
      throws IOException, ClassNotFoundException {

    final RelatedAnomaliesAnalysisApi result = rcaRelatedService.getAnomaliesAnalysis(principal, 
        anomalyId,
        scoring, limit, isoPeriod(lookaround));
    return Response.ok(result).build();
  }
}
