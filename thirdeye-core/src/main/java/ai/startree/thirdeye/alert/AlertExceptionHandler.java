/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATA_UNAVAILABLE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_TIMEOUT;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;
import static ai.startree.thirdeye.util.ResourceUtils.statusApi;

import ai.startree.thirdeye.detection.DataProviderException;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertExceptionHandler {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertExceptionHandler.class);

  public static void handleAlertEvaluationException(final Exception e) {
    LOG.error("Error in Alert Evaluation", e);
    final StatusListApi statusListApi = new StatusListApi().setList(new ArrayList<>());

    populateStatusListApi(e, statusListApi);
    throw serverError(statusListApi);
  }

  private static void populateStatusListApi(final Throwable e, StatusListApi statusListApi) {
    final List<StatusApi> l = statusListApi.getList();
    if (e instanceof TimeoutException) {
      l.add(statusApi(ERR_TIMEOUT));
    } else if (e instanceof DataProviderException) {
      l.add(statusApi(ERR_DATA_UNAVAILABLE, e.getMessage()));
    } else if (e instanceof ThirdEyeException) {
      final ThirdEyeException thirdEyeException = (ThirdEyeException) e;
      l.add(new StatusApi()
          .setCode(thirdEyeException.getStatus())
          .setMsg(thirdEyeException.getMessage()));
    } else  {
      l.add(statusApi(ERR_UNKNOWN, e.getMessage()));
    }
    if (e.getCause() != null) {
      populateStatusListApi(e.getCause(), statusListApi);
    }
  }
}
