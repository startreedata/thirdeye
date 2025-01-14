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
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GenericExceptionMapper<E extends Throwable> extends LoggingExceptionMapper<E> implements
    ExceptionMapper<E> {

  private final Class<E> clazz;
  private final ThirdEyeStatus status;
  private final Logger logger;

  public GenericExceptionMapper(final Class<E> clazz, final ThirdEyeStatus status) {
    this.clazz = clazz;
    this.status = status;
    this.logger = LoggerFactory.getLogger(GenericExceptionMapper.class.toString() + "." + clazz.getSimpleName());
  }

  @Override
  public Response toResponse(final E exception) {
    logger.debug(
        "Request failed because of a {}. Returning error code {}",
        clazz.getSimpleName(),
        status.getRecommendedStatusCode());
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
  
}
