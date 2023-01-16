/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.util.StringTemplateUtils;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class NotificationDispatcher {

  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationSchemesMigrator notificationSchemesMigrator;
  private final Counter notificationDispatchCounter;
  private final Counter notificationDispatchSuccessCounter;
  private final Counter notificationDispatchExceptionCounter;
  private final Histogram notificationDispatchDuration;

  @Inject
  public NotificationDispatcher(
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationSchemesMigrator notificationSchemesMigrator,
      final MetricRegistry metricRegistry) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationSchemesMigrator = notificationSchemesMigrator;
    this.notificationDispatchCounter = metricRegistry.counter("notificationDispatchCounter");
    this.notificationDispatchSuccessCounter = metricRegistry.counter(
        "notificationDispatchSuccessCounter");
    this.notificationDispatchExceptionCounter = metricRegistry.counter(
        "notificationDispatchExceptionCounter");
    this.notificationDispatchDuration = metricRegistry.histogram(
        "notificationDispatchDuration");
  }

  public void dispatch(final SubscriptionGroupDTO subscriptionGroup,
      final NotificationPayloadApi payload) {
    optional(subscriptionGroup.getSpecs())
        .orElseGet(() -> notificationSchemesMigrator.getSpecsFromNotificationSchemes(
            subscriptionGroup))
        .stream()
        .map(this::substituteEnvironmentVariables)
        .map(this::getNotificationService)
        .forEach(service -> {
          try {
            final long tStart = System.currentTimeMillis();
            service.notify(payload);
            notificationDispatchDuration.update(System.currentTimeMillis() - tStart);
            notificationDispatchSuccessCounter.inc();
          } catch (Exception exception) {
            notificationDispatchExceptionCounter.inc();
            throw exception;
          } finally {
            notificationDispatchCounter.inc();
          }
        });
  }

  private NotificationService getNotificationService(final NotificationSpecDTO spec) {
    return notificationServiceRegistry.get(spec.getType(), spec.getParams());
  }

  private NotificationSpecDTO substituteEnvironmentVariables(final NotificationSpecDTO spec) {
    final Map<String, Object> values = new HashMap<>(System.getenv());
    try {
      return StringTemplateUtils.applyContext(spec, values);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error while replacing env variables in notification spec. spec: " + spec);
    }
  }
}
