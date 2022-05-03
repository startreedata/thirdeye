package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.rca.RootCauseAnalysisInfo;
import ai.startree.thirdeye.rca.RootCauseAnalysisInfoFetcher;
import ai.startree.thirdeye.rootcause.events.IntervalSimilarityScoring;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
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
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaEventsResource {

  private static final String DEFAULT_LOOKBACK = "P7D";
  private static final String DEFAULT_LIMIT = "50";
  private static final String DEFAULT_SCORING = "TRIANGULAR";
  private final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher;
  private final EventManager eventDAO;
  private final MergedAnomalyResultManager anomalyDAO;

  @Inject
  public RcaEventsResource(
      final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher,
      final EventManager eventDAO,
      final MergedAnomalyResultManager anomalyDAO) {
    this.rootCauseAnalysisInfoFetcher = rootCauseAnalysisInfoFetcher;
    this.eventDAO = eventDAO;
    this.anomalyDAO = anomalyDAO;
  }

  @GET
  @Path("/calendar/anomaly/{id}")
  @ApiOperation(value = "Returns calendar events related to the anomaly. Events are ordered by the scoring function.")
  public Response getCalendarEvents(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long anomalyId,
      @ApiParam(value = "type") @QueryParam("type") @Nullable String type,
      @ApiParam(value = "scoring") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @ApiParam(value = "limit") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @ApiParam(value = "lookaround") @QueryParam("lookaround") @DefaultValue(DEFAULT_LOOKBACK) String lookaround)
      throws IOException, ClassNotFoundException {

    final Period lookaroundPeriod = Period.parse(lookaround, ISOPeriodFormat.standard());
    final RootCauseAnalysisInfo rootCauseAnalysisInfo = rootCauseAnalysisInfoFetcher.getRootCauseAnalysisInfo(
        anomalyId);
    final Interval anomalyInterval = new Interval(
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getStartTime(),
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getEndTime(),
        rootCauseAnalysisInfo.getTimezone()
    );
    final long startWithLookback = anomalyInterval.getStart()
        .minus(lookaroundPeriod)
        .getMillis();
    final long endWithLookahead = Math.max(anomalyInterval.getStart()
        .plus(lookaroundPeriod)
        .getMillis(), anomalyInterval.getEnd().getMillis());

    final List<EventDTO> events = type != null ?
        eventDAO.findEventsBetweenTimeRangeWithType(type, startWithLookback, endWithLookahead) :
        eventDAO.findEventsBetweenTimeRange(startWithLookback, endWithLookahead);

    events.sort(Comparator.comparingDouble(
        (ToDoubleFunction<EventDTO>) dto -> scoring.score(anomalyInterval,
            new Interval(dto.getStartTime(), dto.getEndTime(), anomalyInterval.getChronology()),
            lookaroundPeriod)
    ).reversed());

    final List<EventApi> eventApis = events.stream().limit(limit).map(ApiBeanMapper::toApi).collect(
        Collectors.toList());
    return Response.ok(eventApis).build();
  }

  @GET
  @Path("/anomalies/anomaly/{id}")
  @ApiOperation(value = "Returns anomalies related to the anomaly. Anomalies are ordered by the scoring function.")
  public Response getAnomaliesEvents(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "id of the anomaly") @PathParam("id") long anomalyId,
      @ApiParam(value = "scoring") @QueryParam("scoring") @DefaultValue(DEFAULT_SCORING) IntervalSimilarityScoring scoring,
      @ApiParam(value = "limit") @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) int limit,
      @ApiParam(value = "lookaround") @QueryParam("lookaround") @DefaultValue(DEFAULT_LOOKBACK) String lookaround)
      throws IOException, ClassNotFoundException {

    final Period lookaroundPeriod = Period.parse(lookaround, ISOPeriodFormat.standard());
    final RootCauseAnalysisInfo rootCauseAnalysisInfo = rootCauseAnalysisInfoFetcher.getRootCauseAnalysisInfo(
        anomalyId);
    final Interval anomalyInterval = new Interval(
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getStartTime(),
        rootCauseAnalysisInfo.getMergedAnomalyResultDTO().getEndTime(),
        rootCauseAnalysisInfo.getTimezone()
    );
    final long startWithLookback = anomalyInterval.getStart()
        .minus(lookaroundPeriod)
        .getMillis();
    final long endWithLookahead = Math.max(anomalyInterval.getStart()
        .plus(lookaroundPeriod)
        .getMillis(), anomalyInterval.getEnd().getMillis());

    final List<MergedAnomalyResultDTO> anomalies = anomalyDAO.findByTime(startWithLookback,
        endWithLookahead);

    anomalies.sort(Comparator.comparingDouble(
        (ToDoubleFunction<MergedAnomalyResultDTO>) dto -> scoring.score(anomalyInterval,
            new Interval(dto.getStartTime(), dto.getEndTime(), anomalyInterval.getChronology()),
            lookaroundPeriod)
    ).reversed());

    final List<AnomalyApi> anomalyApis = anomalies.stream()
        .limit(limit)
        .filter(dto -> !dto.getId().equals(anomalyId))
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
    return Response.ok(anomalyApis).build();
  }
}
