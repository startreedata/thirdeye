/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.notification;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;

public interface NotificationService {

  void notify(NotificationPayloadApi api);

  /**
   * Currently used to debug email notification service
   *
   * TODO spyne remove API
   *
   * @param api
   * @return
   */
  @Deprecated
  default Object toHtml(NotificationPayloadApi api) {
    throw new UnsupportedOperationException();
  }
}
