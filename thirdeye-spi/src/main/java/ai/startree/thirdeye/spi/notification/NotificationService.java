/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.notification;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;

public interface NotificationService {

  void notify(NotificationPayloadApi api);
}
