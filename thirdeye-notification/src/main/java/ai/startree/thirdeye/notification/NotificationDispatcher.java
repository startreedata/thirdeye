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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Builder;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationDispatcher {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationDispatcher.class);

  private final static String NAMESPACE_TAG = "namespace";
  private final static String NULL_NAMESPACE_TAG_VALUE = "__null__";
  private final static String NOTIFICATION_DISPATCH_TIMER_NAME = "thirdeye_notification_dispatch";
  private final static String NOTIFICATION_DISPATCH_TIMER_DESCRIPTION = "Start: A notification payload is passed to the NotificationService#notify implementation. End: The method returns. Tag exception=true means an exception was thrown by the method call.";

  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationSchemesMigrator notificationSchemesMigrator;

  @Inject
  public NotificationDispatcher(
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationSchemesMigrator notificationSchemesMigrator) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationSchemesMigrator = notificationSchemesMigrator;
  }

  // todo cyril the map output is pretty bad - not doing more for the moment because NotificationDispatcher and NotificationTaskPostProcessor may be merged - see todo below
  public Map<NotificationSpecDTO, Exception>  dispatch(final SubscriptionGroupDTO subscriptionGroup,
      final NotificationPayloadApi payload) {
    final List<NotificationSpecDTO> notificationSpecDTOs = optional(
        subscriptionGroup.getSpecs())
        .orElseGet(() -> notificationSchemesMigrator.getSpecsFromNotificationSchemes(
            subscriptionGroup))
        .stream()
        .map(this::substituteEnvironmentVariables).toList();
    
    // TODO cyril - re-design managing errors and potential notification duplications - see TE-2339
    final Map<NotificationSpecDTO, Exception> specToException = new HashMap<>(); 
    for (final NotificationSpecDTO notificationSpec: notificationSpecDTOs) {
      final NotificationService service = getNotificationService(notificationSpec);
      try {
        timedNotify(service, payload, subscriptionGroup);
        specToException.put(notificationSpec, null);
      } catch (Exception e) {
        LOG.error("Notification failed for channel of type {}.", notificationSpec.getType(), e);
        specToException.put(notificationSpec, e);
      }
    }
    
    return specToException;
  }

  /**
   * 
   * @throws Exception notification to external system can fail for many reason.
   * */
  private void timedNotify(final NotificationService service,
      final NotificationPayloadApi payload, final SubscriptionGroupDTO sg)
      throws Exception {
    final Timer notificationDispatchTimerOfSuccess = getNotificationDispatchSuccessTimer(sg);
    final Timer notificationDispatchTimerOfException = getNotificationDispatchExceptionTimer(sg);
    final Timer.Sample sample = Timer.start(Metrics.globalRegistry);
    try {
      service.notify(payload);
      sample.stop(notificationDispatchTimerOfSuccess);
    } catch (Exception exception) {
      sample.stop(notificationDispatchTimerOfException);
      throw exception;
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
    } catch (IOException e) {
      throw new RuntimeException("Error while replacing env variables in notification spec. spec: " + spec);
    }
  }

  private @NonNull String getNamespaceTag(final SubscriptionGroupDTO dto) {
    return Optional.ofNullable(dto.namespace()).orElse(NULL_NAMESPACE_TAG_VALUE);
  }

  private Timer getNotificationDispatchSuccessTimer(final SubscriptionGroupDTO dto) {
    return Timer.builder(NOTIFICATION_DISPATCH_TIMER_NAME)
        .description(NOTIFICATION_DISPATCH_TIMER_DESCRIPTION)
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .tag("exception", "false")
        .tag(NAMESPACE_TAG, getNamespaceTag(dto))
        .register(Metrics.globalRegistry);
  }

  private Timer getNotificationDispatchExceptionTimer(final SubscriptionGroupDTO dto) {
    return Timer.builder(NOTIFICATION_DISPATCH_TIMER_NAME)
        .description(NOTIFICATION_DISPATCH_TIMER_DESCRIPTION)
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .tag("exception", "true")
        .tag(NAMESPACE_TAG, getNamespaceTag(dto))
        .register(Metrics.globalRegistry);
  }
}
