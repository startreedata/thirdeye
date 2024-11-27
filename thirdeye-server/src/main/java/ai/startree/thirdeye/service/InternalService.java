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
package ai.startree.thirdeye.service;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.notification.NotificationPayloadBuilder;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.notification.NotificationTaskFilter;
import ai.startree.thirdeye.notification.NotificationTaskFilterResult;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.runner.DetectionPipelineTaskRunner;
import ai.startree.thirdeye.worker.task.runner.NotificationTaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Set;

@Singleton
public class InternalService {

  private final NotificationTaskRunner notificationTaskRunner;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final NotificationTaskFilter notificationTaskFilter;
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final DetectionPipelineTaskRunner detectionPipelineTaskRunner;
  private final AuthorizationManager authorizationManager;
  private final AlertManager alertManager;

  @Inject
  public InternalService(final NotificationTaskRunner notificationTaskRunner,
      final SubscriptionGroupManager subscriptionGroupManager,
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final NotificationTaskFilter notificationTaskFilter,
      final NotificationServiceRegistry notificationServiceRegistry,
      final DetectionPipelineTaskRunner detectionPipelineTaskRunner,
      final AuthorizationManager authorizationManager,
      final AlertManager alertManager) {
    this.notificationTaskRunner = notificationTaskRunner;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.notificationTaskFilter = notificationTaskFilter;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.detectionPipelineTaskRunner = detectionPipelineTaskRunner;
    this.authorizationManager = authorizationManager;
    this.alertManager = alertManager;
  }

  public void notify(final ThirdEyePrincipal principal, final Long subscriptionGroupId, final Boolean reset) throws Exception {
    authorizationManager.hasRootAccess(principal);
    final SubscriptionGroupDTO sg = subscriptionGroupManager.findById(subscriptionGroupId);
    if (reset == Boolean.TRUE) {
      sg.setVectorClocks(null);
      subscriptionGroupManager.save(sg);
    }
    notificationTaskRunner.execute(subscriptionGroupId, sg.namespace());
  }

  public String generateHtmlEmail(final ThirdEyePrincipal principal, final Long subscriptionGroupManagerById, final Boolean reset) {
    authorizationManager.hasRootAccess(principal);
    final SubscriptionGroupDTO sg = subscriptionGroupManager.findById(subscriptionGroupManagerById);
    if (reset == Boolean.TRUE) {
      sg.setVectorClocks(null);
      subscriptionGroupManager.save(sg);
    }

    requireNonNull(sg, "subscription Group is null");
    final long endTime = System.currentTimeMillis();
    final NotificationTaskFilterResult result = requireNonNull(
        notificationTaskFilter.filter(sg, endTime), "NotificationTaskFilterResult is null");
    final Set<AnomalyDTO> anomalies = result.getAnomalies();

    final String emailHtml;
    if (anomalies.isEmpty()) {
      emailHtml = "No anomalies!";
    } else {
      final NotificationPayloadApi payload = notificationPayloadBuilder.build(result);

      final NotificationService emailNotificationService = notificationServiceRegistry.get(
          "email-smtp", new HashMap<>());
      emailHtml = emailNotificationService.toHtml(payload).toString();
    }
    return emailHtml;
  }

  public void runDetectionTaskLocally(final ThirdEyePrincipal principal, final long alertId, final long startTime,
      final long endTime) throws Exception {
    authorizationManager.hasRootAccess(principal);
    final DetectionPipelineTaskInfo info = new DetectionPipelineTaskInfo(alertId, startTime,
        endTime);
    final AlertDTO alert = requireNonNull(alertManager.findById(alertId),
        String.format("Could not find alert with id %d for local detection task run", alertId));
    detectionPipelineTaskRunner.execute(info, new TaskContext(), alert.namespace());
  }
}
