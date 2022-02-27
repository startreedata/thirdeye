/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import java.util.Map;

public class EmailSendgridNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "sendgrid";
  }

  @Override
  public NotificationService build(final Map<String, Object> params) {
    return new EmailSendgridNotificationService();
  }
}
