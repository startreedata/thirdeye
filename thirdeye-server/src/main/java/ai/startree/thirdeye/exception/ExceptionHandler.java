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

import static ai.startree.thirdeye.ResourceUtils.serverError;
import static ai.startree.thirdeye.ResourceUtils.statusApi;
import static ai.startree.thirdeye.ResourceUtils.statusListApi;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ALERT_PIPELINE_EXECUTION;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_EXECUTION_RCA_ALGORITHM;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN_RCA_ALGORITHM;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.ExceptionApi;
import ai.startree.thirdeye.spi.api.StackTraceElementApi;
import ai.startree.thirdeye.spi.api.StatusApi;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated // rely on ThirdEyeExceptionMapper instead. TODO CYRIL write the necessary ExceptionMappers and get rid of this class
public class ExceptionHandler {

  // do not use Map.of. Order is important. ImmutableMap conserves order
  private static final Map<Class<?>, Function<Throwable, StatusApi>> ALERT_HANDLERS = ImmutableMap.<Class<?>, Function<Throwable, StatusApi>>builder()
      .put(ExecutionException.class,
          e -> statusApi(ERR_ALERT_PIPELINE_EXECUTION, e.getCause().getMessage()))
      .put(ThirdEyeException.class,
          e -> {
            final ThirdEyeException thirdEyeException = (ThirdEyeException) e;
            return new StatusApi().setCode(thirdEyeException.getStatus())
                .setMsg(thirdEyeException.getMessage());
          })
      .put(Throwable.class, e -> statusApi(ERR_UNKNOWN, e.getMessage()))
      .build();

  private static final Map<Class<?>, Function<Throwable, StatusApi>> RCA_HANDLERS = ImmutableMap.<Class<?>, Function<Throwable, StatusApi>>builder()
      .put(ExecutionException.class,
          e -> {
            Throwable cause = e.getCause();
            if (cause instanceof final ThirdEyeException thirdEyeException) {
              return new StatusApi().setCode(thirdEyeException.getStatus())
                  .setMsg(thirdEyeException.getMessage());
            } else {
              return statusApi(ERR_EXECUTION_RCA_ALGORITHM, e.getCause().getMessage());
            }
          })
      .put(ThirdEyeException.class,
          e -> {
            final ThirdEyeException thirdEyeException = (ThirdEyeException) e;
            return new StatusApi().setCode(thirdEyeException.getStatus())
                .setMsg(thirdEyeException.getMessage());
          })
      .put(Throwable.class, e -> statusApi(ERR_UNKNOWN_RCA_ALGORITHM, e.getMessage()))
      .build();

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

  public static void handleAlertEvaluationException(final Exception e) {
    LOG.error("Error in Alert Evaluation", e);
    handleException(e, ALERT_HANDLERS);
  }

  public static void handleRcaAlgorithmException(final Exception e) {
    LOG.error("Error in RCA algorithm", e);
    handleException(e, RCA_HANDLERS);
  }

  private static void handleException(final Exception e,
      final Map<Class<?>, Function<Throwable, StatusApi>> handlers) {
    final StatusApi statusApi = requireNonNull(toStatusApi(e, handlers))
        .setException(toExceptionApi(e));
    throw serverError(statusListApi(statusApi));
  }

  private static StatusApi toStatusApi(final Throwable e,
      final Map<Class<?>, Function<Throwable, StatusApi>> handlers) {
    for (final var h : handlers.entrySet()) {
      final Class<?> handledClass = h.getKey();
      final Function<Throwable, StatusApi> handler = h.getValue();
      if (handledClass.isInstance(e)) {
        return handler.apply(e);
      }
    }
    return null;
  }

  public static ExceptionApi toExceptionApi(final Throwable t) {
    if (t == null) {
      return null;
    }

    final List<StackTraceElementApi> stackTrace = Arrays.stream(t.getStackTrace())
        .map(ExceptionHandler::stackTraceElementApi)
        .collect(Collectors.toList());

    return new ExceptionApi()
        .setMessage(t.getMessage())
        .setCause(toExceptionApi(t.getCause()))
        .setStackTrace(stackTrace);
  }

  private static StackTraceElementApi stackTraceElementApi(final StackTraceElement ste) {
    return new StackTraceElementApi()
        .setClassName(ste.getClassName())
        .setMethodName(ste.getMethodName())
        .setFileName(ste.getFileName())
        .setLineNumber(ste.getLineNumber());
  }
}
