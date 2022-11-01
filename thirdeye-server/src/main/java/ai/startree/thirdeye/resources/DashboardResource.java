package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.core.AppAnalyticsService;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.api.DashboardApi;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Api(tags = "Dashboard", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {

  private final AppAnalyticsService appAnalyticsService;

  @Inject
  public DashboardResource(final AppAnalyticsService appAnalyticsService) {
    this.appAnalyticsService = appAnalyticsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDashboardPayload(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @Context UriInfo uriInfo) throws ExecutionException {
    final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    final Long from = queryParameters.getFirst("from") == null ?
        0L : Long.parseLong(queryParameters.getFirst("from"));
    final Long to = queryParameters.getFirst("to") == null ?
        System.currentTimeMillis() : Long.parseLong(queryParameters.getFirst("to"));
    final AnomalyStatsApi stats = appAnalyticsService.computeAnomalyStats();
    return Response.ok(new DashboardApi()
        .setAnomalyCount(stats.getTotalCount())
        .setAnomalyCountWithoutFeedback(stats.getTotalCount() - stats.getCountWithFeedback())
        .setActiveAlertsCount(appAnalyticsService.countActiveAlerts())
        .setSgCount(appAnalyticsService.sgCount())
        .setPrecision(appAnalyticsService.confusionMatrixSupplier.get().getPrecision())
        .setAnomalyTs(appAnalyticsService.anomalyTsCache.get(from+"-"+to))
    ).build();
  }
}
