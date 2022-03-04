/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NotificationDispatcher {

  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationSchemesMigrator notificationSchemesMigrator;

  @Inject
  public NotificationDispatcher(
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationSchemesMigrator notificationSchemesMigrator) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationSchemesMigrator = notificationSchemesMigrator;
  }

  public void dispatch(final SubscriptionGroupDTO subscriptionGroup,
      final NotificationPayloadApi payload) {
    optional(subscriptionGroup.getSpecs())
        .orElseGet(() -> notificationSchemesMigrator.getSpecsFromNotificationSchemes(
            subscriptionGroup))
        .stream()
        .map(this::getNotificationService)
        .forEach(service -> service.notify(payload));
  }

  private NotificationService getNotificationService(final NotificationSpecDTO spec) {
    return notificationServiceRegistry.get(spec.getType(), spec.getParams());
  }
}
