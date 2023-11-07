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

package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.alert.AlertInsightsProvider.currentMaximumPossibleEndTime;
import static ai.startree.thirdeye.mapper.ApiBeanMapper.toEnumerationItemDTO;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertCreater;
import ai.startree.thirdeye.alert.AlertDeleter;
import ai.startree.thirdeye.alert.AlertEvaluator;
import ai.startree.thirdeye.alert.AlertInsightsProvider;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.api.AlertInsightsRequestApi;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertService extends CrudService<AlertApi, AlertDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(AlertService.class);

  private static final String CRON_EVERY_HOUR = "0 0 * * * ? *";

  private final AlertCreater alertCreater;
  private final AlertDeleter alertDeleter;
  private final AlertEvaluator alertEvaluator;
  private final AppAnalyticsService analyticsService;
  private final AlertInsightsProvider alertInsightsProvider;

  @Inject
  public AlertService(final AlertCreater alertCreater,
      final AlertDeleter alertDeleter,
      final AlertEvaluator alertEvaluator,
      final AlertManager alertManager,
      final AppAnalyticsService analyticsService,
      final AlertInsightsProvider alertInsightsProvider,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, alertManager, ImmutableMap.of());
    this.alertCreater = alertCreater;
    this.alertDeleter = alertDeleter;
    this.alertEvaluator = alertEvaluator;
    this.analyticsService = analyticsService;
    this.alertInsightsProvider = alertInsightsProvider;
  }

  @Override
  protected void deleteDto(final AlertDTO dto) {
    alertDeleter.delete(dto);
  }

  @Override
  protected AlertDTO createDto(final ThirdEyePrincipal principal, final AlertApi api) {
    if (api.getCron() == null) {
      api.setCron(CRON_EVERY_HOUR);
    }

    // TODO spyne: Slight bug here. Alert is saved twice! once here and once in CrudResource
    return alertCreater.create(api
        .setOwner(new UserApi().setPrincipal(principal.getName()))
    );
  }

  @Override
  protected AlertDTO toDto(final AlertApi api) {
    return ApiBeanMapper.toAlertDto(api);
  }

  @Override
  protected void validate(final AlertApi api, final AlertDTO existing) {
    super.validate(api, existing);
    ensureExists(api.getName(), "Name must be present");
    optional(api.getCron()).ifPresent(cron ->
        ensure(CronExpression.isValidExpression(cron), ERR_CRON_INVALID, api.getCron()));

    /* new entity creation or name change in existing entity */
    if (existing == null || !existing.getName().equals(api.getName())) {
      ensure(dtoManager.findByName(api.getName()).size() == 0, ERR_DUPLICATE_NAME, api.getName());
    }
  }

  @Override
  protected void prepareUpdatedDto(final ThirdEyePrincipal principal,
      final AlertDTO existing,
      final AlertDTO updated) {
    // prevent manual update of lastTimestamp
    updated.setLastTimestamp(existing.getLastTimestamp());

    // Always set a default cron if not present.
    if (updated.getCron() == null) {
      updated.setCron(CRON_EVERY_HOUR);
    }
  }

  @Override
  protected void postUpdate(final AlertDTO dto) {
    /*
     * Running the detection task after updating an alert ensures that enumeration items if
     * updated are reflected in the dtos as well. Enumeration Items are updated after executing
     * the Enumerator Operator node.
     *
     * In this case the start and end timestamp is the same to ensure that we update the enumeration
     * items but we don't actually run the detection task.
     */
    alertCreater.createDetectionTask(dto.getId(),
        dto.getLastTimestamp(),
        dto.getLastTimestamp());
  }

  @Override
  protected AlertApi toApi(final AlertDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  public AlertInsightsApi getInsightsById(final ThirdEyePrincipal principal, final Long id) {
    final AlertDTO dto = getDto(id);
    authorizationManager.ensureHasAccess(principal, dto, AccessType.READ);
    return alertInsightsProvider.getInsights(dto);
  }

  public AlertInsightsApi getInsights(
      final AlertInsightsRequestApi request) {
    return alertInsightsProvider.getInsights(request);
  }

  public void runTask(
      final ThirdEyePrincipal principal,
      final Long id,
      final Long startTime,
      final Long endTime
  ) {
    final AlertDTO dto = getDto(id);
    ensureExists(dto);
    ensureExists(startTime, "start");
    authorizationManager.ensureHasAccess(principal, dto, AccessType.WRITE);

    alertCreater.createDetectionTask(id, startTime, safeEndTime(endTime));
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

  public void validateMultiple(final ThirdEyePrincipal principal, final List<AlertApi> list) {
    for (final AlertApi api : list) {
      final AlertDTO existing =
          api.getId() == null ? null : ensureExists(dtoManager.findById(api.getId()));
      validate(api, existing);
      authorizationManager.ensureCanValidate(principal, optional(existing).orElse(toDto(api)));
    }
  }

  public AlertEvaluationApi evaluate(
      final ThirdEyePrincipal principal,
      final AlertEvaluationApi request
  ) throws ExecutionException {
    final long safeEndTime = safeEndTime(request.getEnd().getTime());
    request.setEnd(new Date(safeEndTime));

    final AlertApi alertApi = request.getAlert()
        .setOwner(new UserApi().setPrincipal(principal.getName()));

    if (alertApi.getId() != null) {
      final AlertDTO alertDto = ensureExists(dtoManager.findById(alertApi.getId()));
      authorizationManager.ensureCanRead(principal, alertDto);
    } else {
      authorizationManager.ensureCanCreate(principal, toDto(alertApi));
    }

    final AlertEvaluationApi results = alertEvaluator.evaluate(request);
    final Map<String, DetectionEvaluationApi> filtered = allowedEvaluations(principal,
        results.getDetectionEvaluations());
    return results.setDetectionEvaluations(filtered);
  }

  private Map<String, DetectionEvaluationApi> allowedEvaluations(
      final ThirdEyePrincipal principal, final Map<String, DetectionEvaluationApi> gotEvals) {
    final Map<String, DetectionEvaluationApi> allowedEvals = new HashMap<>();

    // Assume entries without an enumeration item are allowed because the evaluation was executed.
    gotEvals.entrySet()
        .stream()
        .filter(entry -> entry.getValue().getEnumerationItem() == null)
        .forEach(entry -> allowedEvals.put(entry.getKey(), entry.getValue()));

    // Check read access for entries with an enumeration item.
    gotEvals.entrySet()
        .stream()
        .filter(entry -> entry.getValue().getEnumerationItem() != null)
        .filter(entry -> authorizationManager.canRead(principal,
            toEnumerationItemDTO(entry.getValue().getEnumerationItem())))
        .forEach(entry -> allowedEvals.put(entry.getKey(), entry.getValue()));
    return allowedEvals;
  }

  public AlertApi reset(
      final ThirdEyePrincipal principal,
      final Long id) {
    final AlertDTO dto = getDto(id);
    authorizationManager.ensureHasAccess(principal, dto, AccessType.WRITE);
    LOG.warn(String.format("Resetting alert id: %d by principal: %s", id, principal.getName()));

    /*
     * We don't want to delete subscription groups associated with the alert. Therefore, we don't
     * also want to delete enumeration items associated with the alert since they are associated
     * with subscription groups. This is taken care automatically after the reset when the pipeline
     * is executed and the enumerator operator cleans the existing enumeration items
     */
    alertDeleter.deleteAssociatedAnomalies(dto.getId());
    final AlertDTO resetAlert = alertCreater.reset(dto);

    return toApi(resetAlert);
  }

  public AnomalyStatsApi stats(
      final Long id,
      final Long enumerationId,
      final Long startTime,
      final Long endTime
  ) {
    final List<Predicate> predicates = new ArrayList<>();
    predicates.add(Predicate.EQ("detectionConfigId", id));

    // optional filters
    optional(enumerationId)
        .ifPresent(enumId -> predicates.add(Predicate.EQ("enumerationItemId", enumerationId)));
    optional(startTime)
        .ifPresent(start -> predicates.add(Predicate.GE("startTime", startTime)));
    optional(endTime)
        .ifPresent(end -> predicates.add(Predicate.LE("endTime", endTime)));

    return analyticsService.computeAnomalyStats(
        Predicate.AND(predicates.toArray(Predicate[]::new)));
  }
}
