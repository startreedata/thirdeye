/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.notification;

import java.util.Map;

public interface NotificationServiceFactory {

  String name();

  NotificationService build(Map<String, String> properties);
}
