package org.apache.pinot.thirdeye.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private final static ObjectMapper J = new ObjectMapper();

  private final DatabaseAdministrator databaseAdministrator;

  @Inject
  public DatabaseAdminResource(
      final DatabaseAdministrator databaseAdministrator) {
    this.databaseAdministrator = databaseAdministrator;
  }

  private static List<Map<String, Object>> resultSetToMap(final ResultSet rs) throws SQLException {
    List<Map<String, Object>> list = new ArrayList<>();
    ResultSetMetaData rsmd = rs.getMetaData();
    while (rs.next()) {
      final Map<String, Object> map = new HashMap<>();
      for (int i = 1; i <= rsmd.getColumnCount(); i++) {
        final String columnName = rsmd.getColumnName(i);
        map.put(columnName, handleObject(rs.getObject(columnName)));
      }
      list.add(map);
    }
    return list;
  }

  private static Object handleObject(final Object object) {
    if (object instanceof String) {
      try {
        return J.readValue(object.toString(), Map.class);
      } catch (JsonProcessingException e) {
        return object;
      }
    }
    return object;
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
