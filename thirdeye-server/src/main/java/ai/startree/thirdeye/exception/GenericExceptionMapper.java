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

import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This mapper has lesser priority than all others.
 * Most Exceptions thrown implemented in this code base should be wrapped inside a ThirdEyeException
 * and will benefit from the ThirdEyeExceptionMapper
 * Complete this class only for exceptions that are hard to wrap with a ThirdEyeException.
 */
@Provider
public class GenericExceptionMapper extends LoggingExceptionMapper<Throwable> {

  private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

  public GenericExceptionMapper() {
  }

  @Override
  public Response toResponse(final Throwable exception) {
    final ThirdEyeStatus status = statusFor(exception);
    LOG.error(
        "Request failed because of a {}. Returning error code {}",
        exception.getClass().getSimpleName(),
        status.getRecommendedStatusCode(), exception);
    final StatusApi statusApi = new StatusApi()
        .setCode(status)
        .setMsg(exception.getMessage())
        // TODO cyril put this behind a boolean - in some environments we should not return this
        .setException(toExceptionApi(exception));
    final StatusListApi statusList = new StatusListApi().setList(List.of(statusApi));
    return Response.status(status.getRecommendedStatusCode())
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(statusList)
        .build();
  }

  private static ThirdEyeStatus statusFor(final Throwable exception) {
    if (exception instanceof TimeoutException) {
      return ThirdEyeStatus.ERR_TIMEOUT;
    } else {
      return ThirdEyeStatus.ERR_UNKNOWN;
    }
  }
}
