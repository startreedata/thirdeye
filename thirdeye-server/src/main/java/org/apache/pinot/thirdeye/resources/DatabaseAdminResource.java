package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.resultSetToMap;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.datalayer.DatabaseAdministrator;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseAdminResource {

  private static final Logger log = LoggerFactory.getLogger(DatabaseAdminResource.class);

  private final DatabaseAdministrator databaseAdministrator;

  @Inject
  public DatabaseAdminResource(
      final DatabaseAdministrator databaseAdministrator) {
    this.databaseAdministrator = databaseAdministrator;
  }

  @GET
  @Path("tables")
  public Response getTables(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) throws Exception {
    return Response
        .ok(databaseAdministrator.getTables())
        .build();
  }

  @GET
  @Path("execute-query")
  public Response executeQuery(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("sql") String sql
  ) throws Exception {
    return Response
        .ok(resultSetToMap(databaseAdministrator.executeQuery(sql)))
        .build();
  }

  @POST
  @Path("create-all-tables")
  public Response createAllTables(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) throws Exception {
    databaseAdministrator.createAllTables();
    return Response.ok().build();
  }

  @DELETE
  @Path("truncate-all-tables")
  public Response deleteAllData(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) throws Exception {
    log.warn("DELETING ALL DATABASE DATA!!! TRUNCATING TABLES!!!");
    databaseAdministrator.truncateTables();
    return Response.ok().build();
  }

  @DELETE
  @Path("drop-all-tables")
  public Response deleteAllTables(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) throws Exception {
    log.warn("DELETING ALL DATABASE TABLES!!! DROPPING TABLES!!!");
    databaseAdministrator.dropTables();
    return Response.ok().build();
  }
}
