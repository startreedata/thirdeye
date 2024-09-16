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

import static ai.startree.thirdeye.ResourceUtils.ensure;
import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.scheduler.JobUtils.FAILED_TASK_CREATION_COUNTERS;
import static ai.startree.thirdeye.service.alert.AlertInsightsProvider.currentMaximumPossibleEndTime;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_FREQUENCY_TOO_HIGH;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.spi.task.TaskType.DETECTION;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.maximumTriggersPerMinute;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singleton;

import ai.startree.thirdeye.alert.AlertEvaluator;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.config.TimeConfiguration;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.service.alert.AlertInsightsProvider;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.api.AlertInsightsRequestApi;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertService extends CrudService<AlertApi, AlertDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(AlertService.class);
  private static final int ALERT_CRON_MAX_TRIGGERS_PER_MINUTE = 6;

  private final TaskManager taskManager;
  private final AnomalyManager anomalyManager;
  private final AlertEvaluator alertEvaluator;
  private final AlertInsightsProvider alertInsightsProvider;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final EnumerationItemManager enumerationItemManager;

  private final long minimumOnboardingStartTime;

  @Inject
  public AlertService(
      final AlertManager alertManager,
      final AnomalyManager anomalyManager,
      final AlertEvaluator alertEvaluator,
      final AlertInsightsProvider alertInsightsProvider,
      final SubscriptionGroupManager subscriptionGroupManager,
      final EnumerationItemManager enumerationItemManager,
      final TaskManager taskManager,
      final TimeConfiguration timeConfiguration,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, alertManager, ImmutableMap.of());
    this.alertEvaluator = alertEvaluator;
    this.alertInsightsProvider = alertInsightsProvider;
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.enumerationItemManager = enumerationItemManager;
    this.taskManager = taskManager;

    minimumOnboardingStartTime = timeConfiguration.getMinimumOnboardingStartTime();
  }

  @Override
  protected void deleteDto(final AlertDTO dto) {
    final Long alertId = dto.getId();

    disassociateFromSubscriptionGroups(alertId);
    deleteAssociatedAnomalies(alertId);
    deleteAssociatedEnumerationItems(alertId);

    dtoManager.delete(dto);
  }

  @Override
  protected void prepareCreatedDto(final ThirdEyePrincipal principal, final AlertDTO dto) {
    if (dto.getLastTimestamp() < minimumOnboardingStartTime) {
      dto.setLastTimestamp(minimumLastTimestamp(principal, dto));
    }
  }

  @Override
  protected AlertDTO toDto(final AlertApi api) {
    return ApiBeanMapper.toAlertDto(api);
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final AlertApi api,
      final AlertDTO existing) {
    super.validate(principal, api, existing);
    ensureExists(api.getName(), "name value must be set.");
    ensureExists(api.getCron(), "cron value must be set.");
    ensure(CronExpression.isValidExpression(api.getCron()), ERR_CRON_INVALID, api.getCron());
    final int maxTriggersPerMinute = maximumTriggersPerMinute(api.getCron());
    ensure(maxTriggersPerMinute <= ALERT_CRON_MAX_TRIGGERS_PER_MINUTE,
        ERR_CRON_FREQUENCY_TOO_HIGH,
        api.getCron(),
        maxTriggersPerMinute,
        ALERT_CRON_MAX_TRIGGERS_PER_MINUTE);
    /* new entity creation or name change in existing entity */
    if (existing == null || !existing.getName().equals(api.getName())) {
      final AlertDTO sameNameSameNamespace = dtoManager.findUniqueByNameAndNamespace(api.getName(),
          optional(api.getAuth()).map(AuthorizationConfigurationApi::getNamespace)
              .orElse(authorizationManager.currentNamespace(principal))
      );
      ensure(sameNameSameNamespace == null, ERR_DUPLICATE_NAME, api.getName());
    }
  }

  @Override
  protected void prepareUpdatedDto(final ThirdEyeServerPrincipal principal,
      final AlertDTO existing,
      final AlertDTO updated) {
    // prevent manual update of lastTimestamp
    updated.setLastTimestamp(existing.getLastTimestamp());
  }

  @Override
  protected void postCreate(final AlertDTO dto) {
    if (dto.isActive()) {
      // run the detection task on the historical data
      // note: the alert will not be initialized if it has isActive to false
      // FIXME cyril - should we run a dummy task like in postupdate to refresh enumeration items quickly? 
      createDetectionTask(dto, dto.getLastTimestamp(), System.currentTimeMillis());  
    }
  }

  @Override
  protected void postUpdate(final ThirdEyePrincipal principal, final AlertDTO dto) {
    if (dto.isActive()) {
      /*
       * Running the detection task after updating an alert ensures that enumeration items if
       * updated are reflected in the dtos as well. Enumeration Items are updated after executing
       * the Enumerator Operator node.
       *
       * In this case the start and end timestamp is the same to ensure that we update the enumeration
       * items but we don't actually run the detection task.
       */
      createDetectionTask(dto, dto.getLastTimestamp(), dto.getLastTimestamp());
      // perform a soft-reset - rerun the detection on the whole historical data - existing and new anomalies will be merged
      // note: the 2 detection tasks can run concurrently, the order does not matter because the last timestamp after the run of the 2 tasks is the same
      //   we could remove the first one but this would make the UI feel less snappy, because a new enumeration would not appear until the full historical replay is finished
      createDetectionTask(dto, minimumLastTimestamp(principal, dto), dto.getLastTimestamp()); 
    }
  }

  @Override
  protected AlertApi toApi(final AlertDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  public AlertInsightsApi getInsightsById(final ThirdEyeServerPrincipal principal, final Long id) {
    final AlertDTO dto = getDto(id);
    authorizationManager.ensureNamespace(principal, dto);
    authorizationManager.ensureCanRead(principal, dto);
    return alertInsightsProvider.getInsights(principal, dto);
  }

  public AlertInsightsApi getInsights(final ThirdEyeServerPrincipal principal,
      final AlertInsightsRequestApi request) {
    return alertInsightsProvider.getInsights(principal, request);
  }

  public void runTask(
      final ThirdEyeServerPrincipal principal,
      final Long id,
      final Long startTime,
      final Long endTime
  ) {
    final AlertDTO dto = getDto(id);
    ensureExists(dto);
    ensureExists(startTime, "start");
    authorizationManager.ensureNamespace(principal, dto);
    authorizationManager.ensureCanEdit(principal, dto, dto);

    createDetectionTask(dto, startTime, safeEndTime(endTime));
  }

  private long safeEndTime(final @Nullable Long endTime) {
    if (endTime == null) {
      return System.currentTimeMillis();
    }
    final long currentMaximumPossibleEndTime = currentMaximumPossibleEndTime();
    if (endTime > currentMaximumPossibleEndTime) {
      LOG.warn(
          "Evaluate endTime is too big: {}. Current system time: {}. Replacing with a smaller safe endTime: {}.",
          endTime,
          System.currentTimeMillis(),
          currentMaximumPossibleEndTime);
      return currentMaximumPossibleEndTime;
    }
    return endTime;
  }

  public void validateMultiple(final ThirdEyeServerPrincipal principal, final List<AlertApi> list) {
    for (final AlertApi api : list) {
      final AlertDTO alertDto;
      if (api.getId() != null) {
        alertDto = ensureExists(dtoManager.findById(api.getId()));
      } else {
        alertDto = toDto(api);
        authorizationManager.enrichNamespace(principal, alertDto);
      }
      authorizationManager.ensureCanCreate(principal, alertDto);
      validate(principal, api, alertDto);
    }
  }

  public AlertEvaluationApi evaluate(
      final ThirdEyeServerPrincipal principal,
      final AlertEvaluationApi request
  ) throws ExecutionException {
    final long safeEndTime = safeEndTime(request.getEnd().getTime());
    request.setEnd(new Date(safeEndTime));

    final AlertApi alertApi = request.getAlert()
        .setOwner(new UserApi().setPrincipal(principal.getName()));

    if (alertApi.getId() != null) {
      final AlertDTO alertDto = ensureExists(dtoManager.findById(alertApi.getId()));
      authorizationManager.ensureCanRead(principal, alertDto);
      // inject namespace in the request 
      alertApi.setAuth(new AuthorizationConfigurationApi().setNamespace(alertDto.namespace()));
    } else {
      final AlertDTO alertDto = toDto(alertApi);
      authorizationManager.enrichNamespace(principal, alertDto);
      authorizationManager.ensureCanCreate(principal, alertDto);
      // inject namespace in the request
      alertApi.setAuth(new AuthorizationConfigurationApi().setNamespace(alertDto.namespace()));
    }
    return alertEvaluator.evaluate(request);
  }

  // note cyril: currently the reset is used after a call to update
  // but the update triggers a soft-reset already - so there may be some concurrency between
  // the soft-reset detection task and the reset detection task. I suspect there are edge cases
  // but in most cases it should work fine.
  // The only correct design is to:
  // 1. expose an atomic update and reset(soft=hard/true)
  // OR
  // 2.have update not triggering a soft reset.
  // Solution 2. was the first design but puts too much work on the client side and already resulted
  // in an important regression so is strongly discouraged
  public AlertApi reset(
      final ThirdEyeServerPrincipal principal,
      final Long id) {
    final AlertDTO dto = getDto(id);
    authorizationManager.ensureNamespace(principal, dto);
    authorizationManager.ensureCanEdit(principal, dto, dto);
    LOG.warn("Resetting alert id: {} by principal: {}", id, principal.getName());

    /*
     * We don't want to delete subscription groups associated with the alert. Therefore, we don't
     * also want to delete enumeration items associated with the alert since they are associated
     * with subscription groups. This is taken care automatically after the reset when the pipeline
     * is executed and the enumerator operator cleans the existing enumeration items
     */
    deleteAssociatedAnomalies(dto.getId());
    // reset lastTimestamp
    dto.setLastTimestamp(minimumLastTimestamp(principal, dto));
    dtoManager.update(dto);
    postCreate(dto);

    return toApi(dto);
  }

  public AnomalyStatsApi stats(
      final ThirdEyeServerPrincipal principal,
      final Long id,
      final Long enumerationId,
      final Long startTime,
      final Long endTime
  ) {
    final AlertDTO dto = ensureExists(getDto(id));
    authorizationManager.ensureNamespace(principal, dto);
    authorizationManager.ensureCanRead(principal, dto);
    // no need to check authz for the enumerationItem - in the new workspace system, if the user has access to the alert then he has access to the enumerationItem
    // no explicit need for namespace filter given alert id is passed - todo cyril authz - still pass one
    final AnomalyFilter filter = new AnomalyFilter()
        .setAlertId(id)
        .setEnumerationItemId(enumerationId)
        .setStartTimeIsGte(startTime)
        .setEndTimeIsLte(endTime)
        ;
    return anomalyManager.anomalyStats(dto.namespace(), filter);
  }

  private void deleteAssociatedAnomalies(final Long alertId) {
    anomalyManager.deleteByPredicate(Predicate.EQ("detectionConfigId", alertId));
  }

  @SuppressWarnings("unchecked")
  private void disassociateFromSubscriptionGroups(final Long alertId) {
    final List<SubscriptionGroupDTO> allSubscriptionGroups = subscriptionGroupManager.findAll();

    final Set<SubscriptionGroupDTO> updated = new HashSet<>();
    for (final SubscriptionGroupDTO sg : allSubscriptionGroups) {
      final List<Long> alertIds = (List<Long>) sg.getProperties().get("detectionConfigIds");
      if (alertIds.contains(alertId)) {
        alertIds.removeAll(singleton(alertId));
        updated.add(sg);
      }
      optional(sg.getAlertAssociations())
          .map(aas -> aas.removeIf(aa -> alertId.equals(aa.getAlert().getId())))
          .filter(b -> b)
          .ifPresent(b -> updated.add(sg));
    }
    subscriptionGroupManager.update(new ArrayList<>(updated));
  }

  private void deleteAssociatedEnumerationItems(final Long alertId) {
    final List<Long> ids = enumerationItemManager.filter(new DaoFilter()
            .setPredicate(Predicate.EQ("alertId", alertId)))
        .stream()
        .map(AbstractDTO::getId)
        .collect(Collectors.toList());
    enumerationItemManager.deleteByIds(ids);
  }

  private long minimumLastTimestamp(final ThirdEyePrincipal principal, final AlertDTO dto) {
    try {
      final AlertInsightsApi insights = alertInsightsProvider.getInsights(principal, dto);
      final Long datasetStartTime = insights.getDatasetStartTime();
      if (datasetStartTime < minimumOnboardingStartTime) {
        LOG.warn(
            "Dataset start time {} is smaller than the minimum onboarding time allowed {}. Using the minimum time allowed.",
            datasetStartTime,
            minimumOnboardingStartTime);
        return minimumOnboardingStartTime;
      }
      return datasetStartTime;
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      // replay from JAN 1 2000 because replaying from 1970 is too slow with small granularity
      LOG.error("Could not fetch insights for alert {}. Using the minimum time allowed. {}",
          dto,
          minimumOnboardingStartTime);
      return minimumOnboardingStartTime;
    }
  }

  private void createDetectionTask(final AlertDTO alertDto, final long start, final long end) {
    checkArgument(alertDto.getId() != null && alertDto.getId() >= 0);
    checkArgument(start <= end);
    final DetectionPipelineTaskInfo info = new DetectionPipelineTaskInfo(alertDto.getId(), start,
        end);

    try {
      final TaskDTO t = taskManager.createTaskDto(info, DETECTION, alertDto.getAuth());
      LOG.info("Created {} task {} with settings {}", DETECTION, t.getId(), t);
    } catch (final Exception e) {
      FAILED_TASK_CREATION_COUNTERS.get(DETECTION).increment();
      throw new RuntimeException(e);
    }
  }
}
