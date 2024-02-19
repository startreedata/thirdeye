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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.notification.NotificationDispatcher;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertAssociationApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SubscriptionGroupServiceTest {

  private SubscriptionGroupService subscriptionGroupService;
  private SubscriptionGroupManager subscriptionGroupManager;
  private AuthorizationManager authorizationManager;
  private ThirdEyeServerPrincipal principal;

  private static AlertAssociationDto alertAssociation(final long alertId) {
    return new AlertAssociationDto().setAlert(alertRef(alertId));
  }

  private static AlertAssociationDto alertAssociation(final long alertId, final long eiId) {
    return new AlertAssociationDto()
        .setAlert(alertRef(alertId))
        .setEnumerationItem(new EnumerationItemDTO().setId(eiId));
  }

  private static AlertAssociationApi alertAssociationApi(final long alertId) {
    return new AlertAssociationApi().setAlert(new AlertApi().setId(alertId));
  }

  private static AlertAssociationApi alertAssociationApi(final long alertId, final long eiId) {
    return new AlertAssociationApi()
        .setAlert(new AlertApi().setId(alertId))
        .setEnumerationItem(new EnumerationItemApi().setId(eiId));
  }

  @BeforeMethod
  public void setUp() {
    subscriptionGroupManager = mock(SubscriptionGroupManager.class);
    authorizationManager = mock(AuthorizationManager.class);

    subscriptionGroupService = new SubscriptionGroupService(
        subscriptionGroupManager,
        authorizationManager,
        mock(NotificationDispatcher.class),
        mock(NotificationServiceRegistry.class)
    );
    principal = mock(ThirdEyeServerPrincipal.class);
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

  @Test
  public void testCreateAlertAssociationsWithWatermark() {
    when(subscriptionGroupManager.save(any())).then(ans -> {
      final SubscriptionGroupDTO dto = ans.getArgument(0);
      assertThat(dto.getAlertAssociations()).hasSize(3);
      dto.getAlertAssociations()
          .forEach(aa -> {
            assertThat(aa.getCreateTime()).isNotNull();
            assertThat(aa.getAnomalyCompletionWatermark()).isNull();
          });
      final long id = 1000L;
      dto.setId(id);
      return id;
    });

    final SubscriptionGroupApi sg1 = new SubscriptionGroupApi()
        .setName("sg1")
        .setCron("0 0 0 * * ?")
        .setAlertAssociations(List.of(
            alertAssociationApi(1L),
            alertAssociationApi(2L, 20L),
            alertAssociationApi(3L, 30L)
        ));
    subscriptionGroupService.createMultiple(principal, List.of(sg1));
    verify(subscriptionGroupManager).save(any());
  }

  @Test
  public void testUpdateAlertAssociationWithWatermark() {
    final SubscriptionGroupApi sg1 = new SubscriptionGroupApi()
        .setName("sg1")
        .setCron("0 0 0 * * ?")
        .setAlertAssociations(List.of(
            alertAssociationApi(1L),
            alertAssociationApi(2L, 20L)
        ));

    final long id = 1000L;
    final AtomicReference<SubscriptionGroupDTO> dtoRef = new AtomicReference<>();
    when(subscriptionGroupManager.save(any())).then(ans -> {
      final SubscriptionGroupDTO dto = ans.getArgument(0);
      dto.setId(id);
      dtoRef.set(dto);
      return id;
    });
    subscriptionGroupService.createMultiple(principal, List.of(sg1));
    verify(subscriptionGroupManager).save(any());

    final SubscriptionGroupApi edited = new SubscriptionGroupApi()
        .setId(id)
        .setName("sg1")
        .setCron("0 0 0 * * ?")
        .setAlertAssociations(List.of(
            alertAssociationApi(1L).setCreated(null),
            alertAssociationApi(2L, 20L)
                .setAnomalyCompletionWatermark(new Timestamp(System.currentTimeMillis()))
        ));

    when(subscriptionGroupManager.findById(id)).thenReturn(dtoRef.get());
    when(subscriptionGroupManager.update(any(SubscriptionGroupDTO.class))).then(ans -> {
      final SubscriptionGroupDTO dto = ans.getArgument(0);
      assertThat(dto.getId()).isEqualTo(id);
      assertThat(dto.getAlertAssociations()).hasSize(2);
      dto.getAlertAssociations()
          .forEach(aa -> {
            assertThat(aa.getCreateTime()).isNotNull();
            assertThat(aa.getAnomalyCompletionWatermark()).isNull();
          });
      return (int) id; // TODO spyne fix this: save() returns long as id while update() returns int!
    });
    subscriptionGroupService.editMultiple(principal, List.of(edited));
    verify(subscriptionGroupManager).save(any());
  }
}