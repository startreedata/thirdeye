package ai.startree.thirdeye.json;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_INVALID_JSON_FORMAT;

import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return error details in ThirdEye format when json format is incorrect.
 *
 * Inspired from io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper.
 * */
@Provider
public class ThirdEyeJsonProcessingExceptionMapper extends
    LoggingExceptionMapper<JsonProcessingException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

  public ThirdEyeJsonProcessingExceptionMapper() {}

  public Response toResponse(JsonProcessingException exception) {
    if (!(exception instanceof JsonGenerationException) && !(exception instanceof InvalidDefinitionException)) {
      LOGGER.debug("Unable to process JSON", exception);
      String message = exception.getOriginalMessage();
      StatusListApi status = new StatusListApi().setList(List.of(new StatusApi().setCode(ERR_INVALID_JSON_FORMAT).setMsg(message)));
      return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(status).build();
    } else {
      return super.toResponse(exception);
    }
  }
}
