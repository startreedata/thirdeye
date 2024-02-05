/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.Constants.METRICS_TIMER_PERCENTILES;
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
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
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
  private final Timer notificationDispatchTimerOfSuccess;
  private final Timer notificationDispatchTimerOfException;

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
    // same metric but different tag - the time measure is assigned manually to the correct tag based on whether there was an exception 
    this.notificationDispatchTimerOfSuccess = Timer.builder("thirdeye_notification_dispatch")
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .tag("exception", "false")
        .description("Start: A notification payload is passed to the NotificationService#notify implementation. End: The method returns. Tag exception=true an exception was thrown by the method call.")
        .register(Metrics.globalRegistry);
    this.notificationDispatchTimerOfException = Timer.builder("thirdeye_notification_dispatch")
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .tag("exception", "true")
        .register(Metrics.globalRegistry);
  }

  public void dispatch(final SubscriptionGroupDTO subscriptionGroup,
      final NotificationPayloadApi payload) {
    optional(subscriptionGroup.getSpecs())
        .orElseGet(() -> notificationSchemesMigrator.getSpecsFromNotificationSchemes(
            subscriptionGroup))
        .stream()
        .map(this::substituteEnvironmentVariables)
        .map(this::getNotificationService)
        .forEach(service -> notifyService(service, payload));
  }

  private void notifyService(final NotificationService service,
      final NotificationPayloadApi payload) {
    final Timer.Sample sample = Timer.start(Metrics.globalRegistry);
    try {
      final long tStart = System.currentTimeMillis();
      service.notify(payload);
      sample.stop(notificationDispatchTimerOfSuccess);
      notificationDispatchDuration.update(System.currentTimeMillis() - tStart);
      notificationDispatchSuccessCounter.inc();
    } catch (Exception exception) {
      notificationDispatchExceptionCounter.inc();
      sample.stop(notificationDispatchTimerOfException);
      throw exception;
    } finally {
      notificationDispatchCounter.inc();
    }
  }

  public void sendTestMessage(final SubscriptionGroupDTO sg) {
    optional(sg.getSpecs())
        .orElseGet(() -> notificationSchemesMigrator.getSpecsFromNotificationSchemes(
            sg))
        .stream()
        .map(this::substituteEnvironmentVariables)
        .map(this::getNotificationService)
        .forEach(NotificationService::sendTestMessage);
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
