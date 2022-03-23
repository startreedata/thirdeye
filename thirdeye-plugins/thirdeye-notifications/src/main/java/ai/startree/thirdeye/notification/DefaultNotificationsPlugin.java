/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.notification.email.EmailSendgridNotificationServiceFactory;
import ai.startree.thirdeye.notification.email.EmailSmtpNotificationServiceFactory;
import ai.startree.thirdeye.notification.webhook.WebhookNotificationServiceFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.google.auto.service.AutoService;
import java.util.Arrays;

@AutoService(Plugin.class)
public class DefaultNotificationsPlugin implements Plugin {

  @Override
  public Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Arrays.asList(
        new WebhookNotificationServiceFactory(),
        new EmailSmtpNotificationServiceFactory(),
        new EmailSendgridNotificationServiceFactory()
    );
  }
}
