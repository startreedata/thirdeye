/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.notification.slack;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class SlackNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "slack";
  }

  @Override
  public NotificationService build(final Map<String, Object> params) {
    final SlackConfiguration configuration = new ObjectMapper()
        .convertValue(params, SlackConfiguration.class);

    return new SlackNotificationService(configuration);
  }
}
