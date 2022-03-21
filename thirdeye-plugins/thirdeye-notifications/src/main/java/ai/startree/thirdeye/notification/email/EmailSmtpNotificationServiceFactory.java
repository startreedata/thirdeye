/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class EmailSmtpNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "email-smtp";
  }

  @Override
  public NotificationService build(final Map<String, Object> params) {
    final EmailSmtpConfiguration configuration = new ObjectMapper()
        .convertValue(params, EmailSmtpConfiguration.class);

    return new EmailSmtpNotificationService(configuration);
  }
}
