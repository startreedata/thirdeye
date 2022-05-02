package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.rca.RootCauseAnalysisInfoFetcher;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaEventsResource {

  private final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher;

  @Inject
  public RcaEventsResource(
      final RootCauseAnalysisInfoFetcher rootCauseAnalysisInfoFetcher) {
    this.rootCauseAnalysisInfoFetcher = rootCauseAnalysisInfoFetcher;
  }

  // todo cyril I am here
}
