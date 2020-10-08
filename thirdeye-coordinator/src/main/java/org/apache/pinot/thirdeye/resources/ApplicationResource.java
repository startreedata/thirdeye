package org.apache.pinot.thirdeye.resources;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

  private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);
  private final ApplicationManager applicationManager;

  @Inject
  public ApplicationResource(
      final ApplicationManager applicationManager) {
    this.applicationManager = applicationManager;
  }

  @GET
  public Response getAll() {
    final List<ApplicationDTO> all = applicationManager.findAll();
    return Response
        .ok(all)
        .build();
  }

  @POST
  public Response createMultiple(ApplicationDTO applicationDTO) {
    applicationManager.save(applicationDTO);
    return Response
        .ok("Create multiple applications.")
        .build();
  }

  @PUT
  public Response editMultiple() {
    return Response
        .ok("Edit multiple applications.")
        .build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") Integer id) {
    return Response
        .ok("Get application: " + id)
        .build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Integer id) {
    return Response
        .ok("Delete application: " + id)
        .build();
  }
}
