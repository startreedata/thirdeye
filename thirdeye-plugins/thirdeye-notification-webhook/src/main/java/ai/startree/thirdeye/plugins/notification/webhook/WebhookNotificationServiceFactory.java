/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.notification.webhook;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class WebhookNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "webhook";
  }

  @Override
  public NotificationService build(final Map<String, Object> params) {
    final WebhookConfiguration configuration = new ObjectMapper()
        .convertValue(params, WebhookConfiguration.class);

    return new WebhookNotificationService(configuration);
  }
}
