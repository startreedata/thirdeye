package org.apache.pinot.thirdeye.notification.email;

import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationService implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);
  private final EmailConfiguration configuration;

  public EmailNotificationService(final EmailConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void notify(final NotificationPayloadApi api) {

  }
}
