/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ALERT_PIPELINE_EXECUTION;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATA_UNAVAILABLE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_TEMPLATE_MISSING_PROPERTY;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_TIMEOUT;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN_RCA_ALGORITHM;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;
import static ai.startree.thirdeye.util.ResourceUtils.statusApi;

import ai.startree.thirdeye.detection.DataProviderException;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import com.google.common.collect.ImmutableMap;
import groovy.lang.MissingPropertyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler {

  // do not use Map.of. order is important and ImmutableList conserves order
  private static final Map<Class<?>, Function<Throwable, StatusApi>> ALERT_HANDLERS = ImmutableMap.<Class<?>, Function<Throwable, StatusApi>>builder()
      .put(TimeoutException.class, e -> statusApi(ERR_TIMEOUT))
      .put(DataProviderException.class, e -> statusApi(ERR_DATA_UNAVAILABLE, e.getMessage()))
      .put(MissingPropertyException.class,
          e -> statusApi(ERR_TEMPLATE_MISSING_PROPERTY,
              ((MissingPropertyException) e).getProperty()))
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
      .put(MissingPropertyException.class,
          e -> statusApi(ERR_TEMPLATE_MISSING_PROPERTY,
              ((MissingPropertyException) e).getProperty()))
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
