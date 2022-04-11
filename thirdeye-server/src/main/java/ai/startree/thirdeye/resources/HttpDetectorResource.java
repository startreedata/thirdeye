package ai.startree.thirdeye.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class HttpDetectorResource {

  @Inject
  public HttpDetectorResource() {
  }

  @POST
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response post(Object o) throws JsonProcessingException {
    System.out.println("payload:" + new ObjectMapper().writeValueAsString(o));
    return Response.ok(o).build();
  }
}
