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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import javax.inject.Inject;
import org.quartz.CronExpression;

@Singleton
public class SubscriptionGroupService extends
    CrudService<SubscriptionGroupApi, SubscriptionGroupDTO> {

  @Inject
  public SubscriptionGroupService(
      final SubscriptionGroupManager subscriptionGroupManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, subscriptionGroupManager, ImmutableMap.of());
  }

  @Override
  protected void validate(final SubscriptionGroupApi api, final SubscriptionGroupDTO existing) {
    super.validate(api, existing);
    ensureExists(api.getName(), "name value must be set.");
    ensureExists(api.getCron(), "cron value must be set.");
    ensure(CronExpression.isValidExpression(api.getCron()), ERR_CRON_INVALID, api.getCron());

    // For new Subscription Group or existing Subscription Group with different name
    if (existing == null || !existing.getName().equals(api.getName())) {
      ensure(dtoManager.findByName(api.getName()).isEmpty(), ERR_DUPLICATE_NAME, api.getName());
    }
    optional(api.getAlertAssociations())
        .ifPresent(l -> l.forEach(alertAssociation -> {
          final AlertApi alert = alertAssociation.getAlert();
          ensureExists(alert, "alert missing in alert association");
          ensureExists(alert.getId(), "alert.id is missing in alert association");
        }));
  }

  @Override
  protected SubscriptionGroupDTO toDto(final SubscriptionGroupApi api) {
    return ApiBeanMapper.toSubscriptionGroupDTO(api);
  }

  @Override
  protected SubscriptionGroupApi toApi(final SubscriptionGroupDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  public SubscriptionGroupApi reset(Long id) {
    final SubscriptionGroupDTO sg = getDto(id);
    sg.setVectorClocks(null);
    dtoManager.save(sg);

    return toApi(sg);
  }
}

