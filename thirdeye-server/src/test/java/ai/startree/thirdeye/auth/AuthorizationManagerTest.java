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
package ai.startree.thirdeye.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.accessControl.ResourceIdentifier;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import org.testng.annotations.Test;

public class AuthorizationManagerTest {

  @Test
  public void testResourceIdForNullDto() {
    final var authorizationManager = new AuthorizationManager(
        null, null, null, null, null);
    final var got = authorizationManager.resourceId(null);
    assertThat(got.getName()).isEqualTo(ResourceIdentifier.DEFAULT_NAME);
    assertThat(got.getNamespace()).isEqualTo(ResourceIdentifier.DEFAULT_NAMESPACE);
    assertThat(got.getEntityType()).isEqualTo(ResourceIdentifier.DEFAULT_ENTITY_TYPE);
  }

  @Test
  public void testResourceIdForAnomalyDtoWithoutEnum() {
    final var alertManager = mock(AlertManager.class);
    final var alertDto = new AlertDTO();
    alertDto.setId(1L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("alert_namespace"));
    when(alertManager.findById(1L)).thenReturn(alertDto);

    final var anomalyDto = new AnomalyDTO();
    anomalyDto.setDetectionConfigId(1L);
    anomalyDto.setId(2L);

    final var authorizationManager = new AuthorizationManager(
        null, null, alertManager, null, null);

    final var got = authorizationManager.resourceId(anomalyDto);
    assertThat(got.getName()).isEqualTo("2");
    assertThat(got.getNamespace()).isEqualTo("alert_namespace");
    assertThat(got.getEntityType()).isEqualTo("ANOMALY");
  }

  @Test
  public void testResourceIdForAnomalyDtoWithEnum() {
    final var alertManager = mock(AlertManager.class);
    final var alertDto = new AlertDTO();
    alertDto.setId(1L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("alert_namespace"));
    when(alertManager.findById(1L)).thenReturn(alertDto);

    final var enumManager = mock(EnumerationItemManager.class);
    final var enumItemDto = new EnumerationItemDTO();
    enumItemDto.setId(2L);
    enumItemDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("enum_namespace"));
    when(enumManager.findById(2L)).thenReturn(enumItemDto);

    final var anomalyDto = new AnomalyDTO();
    anomalyDto.setDetectionConfigId(1L);
    anomalyDto.setEnumerationItem((EnumerationItemDTO) new EnumerationItemDTO().setId(2L));
    anomalyDto.setId(3L);

    final var authorizationManager = new AuthorizationManager(
        null, null, alertManager, enumManager, null);

    final var got = authorizationManager.resourceId(anomalyDto);
    assertThat(got.getName()).isEqualTo("3");
    assertThat(got.getNamespace()).isEqualTo("enum_namespace");
    assertThat(got.getEntityType()).isEqualTo("ANOMALY");
  }
}
