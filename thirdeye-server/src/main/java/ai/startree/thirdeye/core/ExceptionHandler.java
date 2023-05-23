/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.core;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ALERT_PIPELINE_EXECUTION;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATA_UNAVAILABLE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_TIMEOUT;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN_RCA_ALGORITHM;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;
import static ai.startree.thirdeye.util.ResourceUtils.statusApi;

import ai.startree.thirdeye.DataProviderException;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler {

  // do not use Map.of. Order is important. ImmutableMap conserves order
  private static final Map<Class<?>, Function<Throwable, StatusApi>> ALERT_HANDLERS = ImmutableMap.<Class<?>, Function<Throwable, StatusApi>>builder()
      .put(TimeoutException.class, e -> statusApi(ERR_TIMEOUT))
      .put(DataProviderException.class, e -> statusApi(ERR_DATA_UNAVAILABLE, e.getMessage()))
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
      .put(TimeoutException.class, e -> statusApi(ERR_TIMEOUT))
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
    final StatusListApi statusListApi = new StatusListApi().setList(new ArrayList<>());

    populateStatusListApi(e, statusListApi, ALERT_HANDLERS);
    throw serverError(statusListApi);
  }

  public static void handleRcaAlgorithmException(final Exception e) {
    LOG.error("Error in RCA algorithm", e);
    final StatusListApi statusListApi = new StatusListApi().setList(new ArrayList<>());

    populateStatusListApi(e, statusListApi, RCA_HANDLERS);
    throw serverError(statusListApi);
  }

  private static void populateStatusListApi(final Throwable e, StatusListApi statusListApi,
      Map<Class<?>, Function<Throwable, StatusApi>> handlers) {
    final List<StatusApi> l = statusListApi.getList();
    for (var h : handlers.entrySet()) {
      final Class<?> handledClass = h.getKey();
      final Function<Throwable, StatusApi> handler = h.getValue();
      if (handledClass.isInstance(e)) {
        l.add(handler.apply(e));
        break;
      }
    }

    if (e.getCause() != null) {
      populateStatusListApi(e.getCause(), statusListApi, handlers);
    }
  }
}
