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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.rca.RcaInfo;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rootcause.events.IntervalSimilarityScoring;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventContextDto;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
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
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Interval;
import org.joda.time.Period;

@SecurityRequirement(name="oauth")
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaRelatedResource {

  private static final String DEFAULT_LOOKBACK = "P7D";
  private static final String DEFAULT_LIMIT = "50";
  private static final String DEFAULT_SCORING = "TRIANGULAR";
  private final RcaInfoFetcher rcaInfoFetcher;
  private final EventManager eventDAO;
  private final AnomalyManager anomalyDAO;

  @Inject
  public RcaRelatedResource(
      final RcaInfoFetcher rcaInfoFetcher,
      final EventManager eventDAO,
      final AnomalyManager anomalyDAO) {
    this.rcaInfoFetcher = rcaInfoFetcher;
    this.eventDAO = eventDAO;
    this.anomalyDAO = anomalyDAO;
  }

  @GET
  @Path("/events")
  @Operation(summary = "Returns calendar events related to the anomaly. Events are ordered by the scoring function.")
  public Response getCalendarEvents(
      @Parameter(hidden = true) @Auth ThirdEyePrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Type of event.") @QueryParam("type") @Nullable String type,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_LOOKBACK) String lookaround)
      throws IOException, ClassNotFoundException {

    final Period lookaroundPeriod = isoPeriod(lookaround);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(
        anomalyId);
    final Interval anomalyInterval = new Interval(
        rcaInfo.getAnomaly().getStartTime(),
        rcaInfo.getAnomaly().getEndTime(),
        rcaInfo.getChronology()
    );
    final long startWithLookback = anomalyInterval.getStart()
        .minus(lookaroundPeriod)
        .getMillis();
    final long endWithLookahead = Math.max(anomalyInterval.getStart()
        .plus(lookaroundPeriod)
        .getMillis(), anomalyInterval.getEnd().getMillis());

    final @NonNull EventContextDto eventContext = rcaInfo.getEventContext();
    // todo cyril make the type parameter a list - ask FrontEnd if it's ok first
    final List<@NonNull String> types = optional(type)
        .map(List::of)
        .orElse(optional(eventContext.getTypes()).map(Templatable::getValue).orElse(List.of()));
    final List<EventDTO> events = eventDAO.findEventsBetweenTimeRange(startWithLookback,
        endWithLookahead,
        types,
        // todo rca dimension filters can be set at call time?
        eventContext.getSqlFilter());

    final Comparator<EventDTO> comparator = Comparator.comparingDouble(
        (ToDoubleFunction<EventDTO>) dto -> scoring.score(anomalyInterval,
            new Interval(dto.getStartTime(), dto.getEndTime(), anomalyInterval.getChronology()),
            lookaroundPeriod)
    ).reversed();
    events.sort(comparator);

    final List<EventApi> eventApis = events.stream().limit(limit).map(ApiBeanMapper::toApi).collect(
        Collectors.toList());
    return Response.ok(eventApis).build();
  }

  @GET
  @Path("/anomalies")
  @Operation(summary = "Returns anomalies related to the anomaly. Anomalies are ordered by the scoring function.")
  public Response getAnomaliesEvents(
      @Parameter(hidden = true) @Auth ThirdEyePrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_LOOKBACK) String lookaround)
      throws IOException, ClassNotFoundException {

    final Period lookaroundPeriod = isoPeriod(lookaround);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(
        anomalyId);
    final Interval anomalyInterval = new Interval(
        rcaInfo.getAnomaly().getStartTime(),
        rcaInfo.getAnomaly().getEndTime(),
        rcaInfo.getChronology()
    );
    final long startWithLookback = anomalyInterval.getStart()
        .minus(lookaroundPeriod)
        .getMillis();
    final long endWithLookahead = Math.max(anomalyInterval.getStart()
        .plus(lookaroundPeriod)
        .getMillis(), anomalyInterval.getEnd().getMillis());

    final List<AnomalyDTO> anomalies = anomalyDAO
        .findByTime(startWithLookback, endWithLookahead)
        .stream()
        // todo cyril - filter at the db level - not in the app
        .filter(dto -> !dto.isChild())
        .collect(Collectors.toList());

    final Comparator<AnomalyDTO> comparator = Comparator.comparingDouble(
        (ToDoubleFunction<AnomalyDTO>) dto -> scoring.score(anomalyInterval,
            new Interval(dto.getStartTime(), dto.getEndTime(), anomalyInterval.getChronology()),
            lookaroundPeriod)
    ).reversed();
    anomalies.sort(comparator);

    final List<AnomalyApi> anomalyApis = anomalies.stream()
        .limit(limit)
        .filter(dto -> !dto.getId().equals(anomalyId))
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
    return Response.ok(anomalyApis).build();
  }
}
