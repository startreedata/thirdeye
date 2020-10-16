package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ApiBeanMapper.toApplicationApi;
import static org.apache.pinot.thirdeye.util.ApiBeanMapper.toApplicationDto;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(
        apiKeyAuthDefinitions = {
            @ApiKeyAuthDefinition(key = "user", name = "Authorization", in = ApiKeyLocation.HEADER)
        }
    )
)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

  private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

  private final ApplicationManager applicationManager;
  private final AuthService authService;

  @Inject
  public ApplicationResource(
      final ApplicationManager applicationManager,
      final AuthService authService) {
    this.applicationManager = applicationManager;
    this.authService = authService;
  }

  @GET
  public Response getAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader
  ) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    final List<ApplicationDTO> all = applicationManager.findAll();
    return Response
        .ok(all.stream().map(ApiBeanMapper::toApplicationApi))
        .build();
  }

  @POST
  public Response createMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApplicationApi> applicationApiList) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(applicationApiList, "Invalid request");
    ensure(applicationApiList.size() == 1, "Only 1 insert supported at this time.");

    final ApplicationApi applicationApi = applicationApiList.get(0);
    final Long saved = applicationManager.save(toApplicationDto(applicationApi));
    return Response
        .ok(saved)
        .build();
  }

  @PUT
  public Response editMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApplicationApi> applicationApiList) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(applicationApiList, "Invalid request");
    ensure(applicationApiList.size() == 1, "Only 1 insert supported at this time.");

    final ApplicationApi applicationApi = applicationApiList.get(0);
    final Long id = applicationApi.getId();
    final ApplicationDTO applicationDTO = applicationManager
        .findById(ensureExists(id, "Invalid id"));
    ensureExists(applicationDTO, "Invalid id");

    optional(applicationApi.getName())
        .ifPresent(applicationDTO::setApplication);

    applicationManager.update(applicationDTO);
    return Response
        .ok(toApplicationApi(applicationDTO))
        .build();
  }

  @GET
  @Path("{id}")
  public Response get(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final ApplicationDTO applicationDTO = applicationManager.findById(id);
    ensureExists(applicationDTO, "Invalid id");

    return Response
        .ok(toApplicationApi(applicationDTO))
        .build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final ApplicationDTO applicationDTO = applicationManager.findById(id);
    if (applicationDTO != null) {
      applicationManager.delete(applicationDTO);
      return Response.ok(toApplicationApi(applicationDTO)).build();
    }
    return Response.ok("Not found").build();
  }
}
