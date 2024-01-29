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

import static ai.startree.thirdeye.spi.util.SpiUtils.alertRef;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.notification.NotificationDispatcher;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import java.sql.Timestamp;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SubscriptionGroupServiceTest {

  private SubscriptionGroupService subscriptionGroupService;

  private static AlertAssociationDto alertAssociation(final long alertId) {
    return new AlertAssociationDto().setAlert(alertRef(alertId));
  }

  private static AlertAssociationDto alertAssociation(final long alertId, final long eiId) {
    return new AlertAssociationDto()
        .setAlert(alertRef(alertId))
        .setEnumerationItem(new EnumerationItemDTO().setId(eiId));
  }

  @BeforeMethod
  public void setUp() {
    subscriptionGroupService = new SubscriptionGroupService(
        mock(SubscriptionGroupManager.class),
        mock(AuthorizationManager.class),
        mock(NotificationDispatcher.class),
        mock(NotificationServiceRegistry.class)
    );
  }

  @Test
  public void testUpdateAlertAssociationsIfReqd() {
    subscriptionGroupService.updateAlertAssociationsIfReqd(null, null);

    final List<AlertAssociationDto> updated = List.of(
        alertAssociation(1L),
        alertAssociation(2L, 20L)
    );
    updated.forEach(a -> assertThat(a.getCreateTime()).isNull());

    subscriptionGroupService.updateAlertAssociationsIfReqd(null, updated);
    updated.forEach(a -> assertThat(a.getCreateTime()).isNotNull());

    final List<AlertAssociationDto> existing = List.of(
        alertAssociation(1L),
        alertAssociation(2L, 20L),
        alertAssociation(3L, 30L)
    );

    final long createTime = System.currentTimeMillis();
    existing.forEach(aa -> aa.setCreateTime(new Timestamp(createTime)));

    final List<AlertAssociationDto> updated2 = List.of(
        alertAssociation(2L, 20L),
        alertAssociation(3L, 30L),
        alertAssociation(4L)
    );
    subscriptionGroupService.updateAlertAssociationsIfReqd(existing, updated2);
    assertThat(updated2.get(0).getCreateTime().getTime()).isEqualTo(createTime);
    assertThat(updated2.get(1).getCreateTime().getTime()).isEqualTo(createTime);
    assertThat(updated2.get(2).getCreateTime()).isNotNull();
    assertThat(updated2.get(2).getCreateTime()).isNotEqualTo(createTime);
  }
}