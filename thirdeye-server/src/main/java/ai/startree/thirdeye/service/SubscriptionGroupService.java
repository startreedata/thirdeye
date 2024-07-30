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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_FREQUENCY_TOO_HIGH;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.maximumTriggersPerMinute;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.notification.NotificationDispatcher;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertAssociationApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.NotificationSpecApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.quartz.CronExpression;

@Singleton
public class SubscriptionGroupService extends
    CrudService<SubscriptionGroupApi, SubscriptionGroupDTO> {

  private static final int SUBSCRIPTION_CRON_MAX_TRIGGERS_PER_MINUTE = 6;
  private final NotificationDispatcher notificationDispatcher;
  private final NotificationServiceRegistry notificationServiceRegistry;

  @Inject
  public SubscriptionGroupService(
      final SubscriptionGroupManager subscriptionGroupManager,
      final AuthorizationManager authorizationManager,
      final NotificationDispatcher notificationDispatcher,
      NotificationServiceRegistry notificationServiceRegistry) {
    super(authorizationManager, subscriptionGroupManager, ImmutableMap.of());
    this.notificationDispatcher = notificationDispatcher;
    this.notificationServiceRegistry = notificationServiceRegistry;
  }

  private static void validateCron(final SubscriptionGroupApi api) {
    ensureExists(api.getCron(), "cron value must be set.");
    ensure(CronExpression.isValidExpression(api.getCron()), ERR_CRON_INVALID, api.getCron());

    final int maxTriggersPerMinute = maximumTriggersPerMinute(api.getCron());
    ensure(maxTriggersPerMinute <= SUBSCRIPTION_CRON_MAX_TRIGGERS_PER_MINUTE,
        ERR_CRON_FREQUENCY_TOO_HIGH,
        api.getCron(),
        maxTriggersPerMinute,
        SUBSCRIPTION_CRON_MAX_TRIGGERS_PER_MINUTE);
  }

  private static void validateAlertAssociation(final AlertAssociationApi alertAssociation) {
    final AlertApi alert = alertAssociation.getAlert();
    ensureExists(alert, "alert missing in alert association");
    ensureExists(alert.getId(), "alert.id is missing in alert association");
  }

  private static void initSystemFields(final AlertAssociationDto aa, final Timestamp created) {
    // override system field values
    aa
        .setCreateTime(created)
        .setAnomalyCompletionWatermark(null);
  }

  private void validateSpec(NotificationSpecApi spec) {
    ensureExists(spec.getType(), "type value must be set.");
    ensure(notificationServiceRegistry.isRegistered(spec.getType()),
        "Notification service not registered: %s. Available: %s".formatted(spec.getType(),
            notificationServiceRegistry.getRegisteredNotificationServices()));
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final SubscriptionGroupApi api, final SubscriptionGroupDTO existing) {
    super.validate(principal, api, existing);
    ensureExists(api.getName(), "name value must be set.");
    validateCron(api);

    optional(api.getSpecs())
        .ifPresent(l -> l.forEach(this::validateSpec));

    // For new Subscription Group or existing Subscription Group with different name
    if (existing == null || !existing.getName().equals(api.getName())) {
      final SubscriptionGroupDTO sameNameSameNamespace = dtoManager.findUniqueByNameAndNamespace(api.getName(),
          optional(api.getAuth()).map(AuthorizationConfigurationApi::getNamespace)
              .orElse(authorizationManager.currentNamespace(principal))
          );
      ensure(sameNameSameNamespace == null, ERR_DUPLICATE_NAME, api.getName());
    }
    optional(api.getAlertAssociations())
        .ifPresent(l -> l.forEach(SubscriptionGroupService::validateAlertAssociation));
  }

  @Override
  protected void prepareCreatedDto(final ThirdEyePrincipal principal,
      final SubscriptionGroupDTO dto) {
    final Timestamp createTime = requireNonNull(dto.getCreateTime(), "created");
    optional(dto.getAlertAssociations())
        .ifPresent(l -> l.forEach(aa -> initSystemFields(aa, createTime)));
  }

  @Override
  protected void prepareUpdatedDto(final ThirdEyeServerPrincipal principal,
      final SubscriptionGroupDTO existing,
      final SubscriptionGroupDTO updated) {
    /* This is a system field which is managed by the notification pipeline */
    updated.setVectorClocks(existing.getVectorClocks());
    updateAlertAssociationsIfReqd(existing.getAlertAssociations(), updated.getAlertAssociations());
  }

  @VisibleForTesting
  void updateAlertAssociationsIfReqd(final List<AlertAssociationDto> existing,
      final List<AlertAssociationDto> updated) {
    if (updated == null || updated.isEmpty()) {
      // if updated is null/empty. nothing to update. all associations are being removed
      return;
    }

    // Collect existing alert associations into a map for easy lookup
    final Map<AlertAssociationId, AlertAssociationDto> m = optional(existing)
        .orElse(emptyList())
        .stream()
        .collect(HashMap::new,
            (map, aa) -> map.put(AlertAssociationId.from(aa), aa),
            HashMap::putAll);

    // Restore system fields from existing alert associations or initialize them
    updated.forEach(aa -> restoreSystemFields(aa, m.get(AlertAssociationId.from(aa))));
  }

  private void restoreSystemFields(final AlertAssociationDto dest, final AlertAssociationDto src) {
    if (src != null) {
      dest.setCreateTime(src.getCreateTime())
          .setAnomalyCompletionWatermark(src.getAnomalyCompletionWatermark());
    } else {
      final Timestamp created = new Timestamp(System.currentTimeMillis());
      initSystemFields(dest, created);
    }
  }

  @Override
  protected SubscriptionGroupDTO toDto(final SubscriptionGroupApi api) {
    return ApiBeanMapper.toSubscriptionGroupDTO(api);
  }

  @Override
  protected SubscriptionGroupApi toApi(final SubscriptionGroupDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  public SubscriptionGroupApi reset(final ThirdEyePrincipal principal, Long id) {
    final SubscriptionGroupDTO sg = getDto(id);
    sg.setVectorClocks(null);
    // todo authz ensureCanEdit is used to also go through related entities - but it's not a great design - consider related entities should be done in the Service? 
    authorizationManager.ensureCanEdit(principal, sg, sg);
    dtoManager.save(sg);

    return toApi(sg);
  }

  public void sendTestMessage(final ThirdEyePrincipal principal, final Long id) {
    final SubscriptionGroupDTO sg = getDto(id);
    // no need to check read access of all associated alerts, because the test message should not use alerts. 
    // todo authz change role?
    authorizationManager.ensureCanRead(principal, sg);
    notificationDispatcher.sendTestMessage(sg);
  }
  
  private record AlertAssociationId(Long  alertId, Long enumerationItemId) {

    private static AlertAssociationId from(AlertAssociationDto aa) {
      return new AlertAssociationId(
          requireNonNull(aa.getAlert().getId()),
          optional(aa.getEnumerationItem())
              .map(AbstractDTO::getId)
              .orElse(null)
      );
    }
    
  }
}

