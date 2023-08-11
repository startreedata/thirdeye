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
import static ai.startree.thirdeye.util.StringUtils.levenshteinDistance;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.rca.RcaInfo;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rootcause.events.IntervalSimilarityScoring;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.api.RelatedEventsAnalysisApi;
import ai.startree.thirdeye.spi.api.TextualAnalysis;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@SecurityRequirement(name = "oauth")
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaRelatedResource {

  public static final int MAX_SELECTED_EVENTS_PER_TYPE_FOR_TEXT = 3;
  private static final String DEFAULT_LOOKBACK = "P7D";
  private static final String DEFAULT_LIMIT = "50";
  private static final String DEFAULT_SCORING = "TRIANGULAR";
  public static final int SAME_EVENT_LEVENSHTEIN_THRESHOLD = 2;
  public static final int MAX_EVENTS_FOR_TEXT = 6;
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
  public Response getRelatedEvents(
      @Parameter(hidden = true) @Auth ThirdEyePrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Type of event.") @QueryParam("type") @Nullable String type,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_LOOKBACK) String lookaround)
      throws IOException, ClassNotFoundException {

    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyId);
    final List<EventApi> eventApis = getCalendarEvents(rcaInfo, type, scoring, limit, lookaround);
    return Response.ok(eventApis).build();
  }

  @NonNull
  private List<EventApi> getCalendarEvents(final RcaInfo rcaInfo,
      final @org.checkerframework.checker.nullness.qual.Nullable String type,
      final IntervalSimilarityScoring scoring, final int limit, final String lookaround) {
    final Period lookaroundPeriod = isoPeriod(lookaround);
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
    return eventApis;
  }

  // TODO experimental - deprecate the endpoint above and add tests
  @GET
  @Path("/events-analysis")
  @Operation(summary = "Returns calendar events related to the anomaly. Events are ordered by the scoring function.")
  public Response getEventsAnalysis(
      @Parameter(hidden = true) @Auth ThirdEyePrincipal principal,
      @Parameter(description = "id of the anomaly") @NotNull @QueryParam("anomalyId") Long anomalyId,
      @Parameter(description = "Type of event.") @QueryParam("type") @Nullable String type,
      @Parameter(description = "Scoring function") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @Parameter(description = "Limit number of anomalies to return.") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @Parameter(description = "Period, in ISO-8601 format, to look after and before the anomaly start.") @QueryParam("lookaround") @DefaultValue(DEFAULT_LOOKBACK) String lookaround)
      throws IOException, ClassNotFoundException {

    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyId);
    final List<EventApi> events = getCalendarEvents(rcaInfo, type, scoring, limit, lookaround);

    final RelatedEventsAnalysisApi result = new RelatedEventsAnalysisApi();
    result.setEvents(events);
    final String analysisText = generateAnalysisText(events, rcaInfo);
    result.setTextualAnalysis(new TextualAnalysis().setText(analysisText));

    return Response.ok(result).build();
  }

  private String generateAnalysisText(final List<EventApi> events, final RcaInfo rcaInfo) {
    if (events.isEmpty()) {
      return "No events related to this anomaly were found.";
    }
    final StringBuilder text = new StringBuilder();
    text.append("Some events occur close to the anomaly and might have caused it.\n");

    final List<EventApi> selectedEvents = selectEventsForText(events);
    for (final EventApi event : selectedEvents) {
      text.append(generateAnalysisText(event, rcaInfo));
      text.append("\n");
    }

    return text.toString();
  }

  /**
   * Select relevant events for textual analysis.
   * Limit the number of events to 3 per type.
   *
   * naive fuzzy matching cleaning:
   * Filter events that have names with a small levenshtein distance. They are most likely the same
   * events.
   */
  @NonNull
  private static List<EventApi> selectEventsForText(final List<EventApi> events) {
    final Map<String, List<EventApi>> typeToEvents = new HashMap<>();
    final LinkedList<EventApi> selectedEvents = new LinkedList<>();
    for (final EventApi e : events) {
      final List<EventApi> typeEvents = typeToEvents.computeIfAbsent(e.getType(),
          k -> new ArrayList<>());
      if (typeEvents.size() >= MAX_SELECTED_EVENTS_PER_TYPE_FOR_TEXT) {
        continue;
      }
      final boolean isNew = typeEvents.stream()
          .map(el -> el.getName().toLowerCase(Constants.DEFAULT_LOCALE))
          // very naive fuzzy matching
          .filter(el -> levenshteinDistance(el, e.getName().toLowerCase(Constants.DEFAULT_LOCALE))
              < SAME_EVENT_LEVENSHTEIN_THRESHOLD)
          .findFirst()
          .isEmpty();
      if (isNew) {
        if (typeEvents.isEmpty()) {
          // add at the beginning to ensure all different types of events have a chance of appearing in the analysis
          selectedEvents.addFirst(e);
        } else {
          selectedEvents.addLast(e);
        }
        typeEvents.add(e);
      }
    }

    return selectedEvents.subList(0, MAX_EVENTS_FOR_TEXT);
  }

  // TODO add weekend analysis wordings eg: "the previous week-end or the following week-end)
  private static String generateAnalysisText(final EventApi event, final RcaInfo rcaInfo) {
    final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("EEEEE, MMMMMM d")
        .withChronology(rcaInfo.getChronology());
    final DateTime eventStart = new DateTime(event.getStartTime(), rcaInfo.getChronology());

    final String timeRelation;
    if (event.getEndTime() <= rcaInfo.getAnomaly().getStartTime()) {
      timeRelation = "occurred on " + eventStart.toString(FORMATTER);
      // todo add x days before the event
    } else if (event.getStartTime() >= rcaInfo.getAnomaly().getEndTime()) {
      // event happens after the anomaly without overlap
      // todo replace soon after by a more precise
      timeRelation = "happens soon after, on " + eventStart.toString(FORMATTER);
    } else {
      // event happens around the same time
      // todo differentiate left overlap, exact match nd right overlap
      timeRelation = "happened around the same time on " + eventStart.toString(FORMATTER);
    }

    final List<CharSequence> words = List.of("The", event.getType(), "event", event.getName(),
        timeRelation);
    return String.join(" ", words) + ".";
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
