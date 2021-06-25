package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.resultSetToMap;

import com.google.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.DatabaseAdministrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public Response getTables() throws Exception {
    return Response
        .ok(databaseAdministrator.getTables())
        .build();
  }

  @GET
  @Path("execute-query")
  public Response executeQuery(@QueryParam("sql") String sql) throws Exception {
    return Response
        .ok(resultSetToMap(databaseAdministrator.executeQuery(sql)))
        .build();
  }

  @POST
  @Path("create-all-tables")
  public Response createAllTables() throws Exception {
    databaseAdministrator.createAllTables();
    return Response.ok().build();
  }

  @DELETE
  @Path("truncate-all-tables")
  public Response deleteAllData() throws Exception {
    log.warn("DELETING ALL DATABASE DATA!!! TRUNCATING TABLES!!!");
    databaseAdministrator.truncateTables();
    return Response.ok().build();
  }

  @DELETE
  @Path("drop-all-tables")
  public Response deleteAllTables() throws Exception {
    log.warn("DELETING ALL DATABASE TABLES!!! DROPPING TABLES!!!");
    databaseAdministrator.dropTables();
    return Response.ok().build();
  }
}
