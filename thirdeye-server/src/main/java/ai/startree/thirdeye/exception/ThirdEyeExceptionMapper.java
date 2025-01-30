/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.exception;

import static ai.startree.thirdeye.exception.ExceptionUtils.toExceptionApi;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If set, status code of the response follows ThirdEyeStatus.recommendHttpCode.
 */
@Provider
public class ThirdEyeExceptionMapper extends LoggingExceptionMapper<ThirdEyeException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThirdEyeExceptionMapper.class);

  public ThirdEyeExceptionMapper() {
  }

  @Override
  public Response toResponse(final ThirdEyeException exception) {
    if (exception.getStatus() != null && exception.getStatus().getRecommendedStatusCode() != null) {
      LOGGER.debug(
          "Unable to process request - generated a ThirdEye exception of type {} - returning error code {}",
          exception.getStatus().name(), exception.getStatus().getRecommendedStatusCode());
      final StatusApi statusApi = new StatusApi()
          .setCode(exception.getStatus())
          .setMsg(exception.getMessage())
          // TODO cyril put this behind a boolean - in some environments we should not return this
          .setException(toExceptionApi(exception));
      final StatusListApi status = new StatusListApi().setList(List.of(statusApi));
      return Response.status(exception.getStatus().getRecommendedStatusCode())
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(status)
          .build();
    } else {
      return super.toResponse(exception);
    }
  }
}
