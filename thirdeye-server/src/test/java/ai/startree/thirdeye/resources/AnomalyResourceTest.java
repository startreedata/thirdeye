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
package ai.startree.thirdeye.resources;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.auth.AccessControlProvider;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.service.AnomalyService;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import java.util.List;
import javax.ws.rs.BadRequestException;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

public class AnomalyResourceTest {

  private static AnomalyResource newAnomalyResource(
      final AnomalyManager anomalyManager,
      final AlertManager alertManager
      ) {
    return new AnomalyResource(new AnomalyService(
        anomalyManager,
        alertManager,
        null,
        new AuthorizationManager(
            null,
            AccessControlProvider.ALWAYS_ALLOW,
            alertManager,
            null,
            anomalyManager
        )
    ));
  }

  static ThirdEyePrincipal nobody() {
    return new ThirdEyePrincipal("nobody", "");
  }

  @Test
  void testGet() {
    final var alertManager = mock(AlertManager.class);
    final var alertDto = new AlertDTO();
    alertDto.setId(1L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("test_namespace"));
    when(alertManager.findById(1L)).thenReturn(alertDto);

    final var anomalyManager = mock(AnomalyManager.class);
    final var anomalyDto = new AnomalyDTO();
    anomalyDto.setDetectionConfigId(1L);
    anomalyDto.setId(2L);
    when(anomalyManager.findById(2L)).thenReturn(anomalyDto);

    final var resource = newAnomalyResource(anomalyManager, alertManager);
    final var resp = resource.get(nobody(), 2L);
    assertThat(resp.getStatus()).isEqualTo(200);
    final var result = (AnomalyApi) resp.getEntity();
    assertThat(result.getId()).isEqualTo(2L);
    assertThat(result.getAuth().getNamespace()).isEqualTo("test_namespace");
  }

  @Test(expectedExceptions = BadRequestException.class)
  void testCreateMultiple_withAuthConfig() {
    final var alertManager = mock(AlertManager.class);
    final var alertDto = new AlertDTO();
    alertDto.setId(1L);
    when(alertManager.findById(1L)).thenReturn(alertDto);

    final var anomalyManager = mock(AnomalyManager.class);
    when(anomalyManager.save(any(AnomalyDTO.class))).thenAnswer((Answer<Long>) invocationOnMock -> {
      ((AnomalyDTO) invocationOnMock.getArgument(0)).setId(2L);
      return 2L;
    });

    final var resource = newAnomalyResource(anomalyManager, alertManager);
    final var anomalyApi = new AnomalyApi()
        .setAlert(new AlertApi().setId(1L))
        .setAuth(new AuthorizationConfigurationApi().setNamespace("test_namespace"));
    resource.createMultiple(nobody(), List.of(anomalyApi));
  }
}
