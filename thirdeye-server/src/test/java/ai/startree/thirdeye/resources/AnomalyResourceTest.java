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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.AccessControlProvider;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.core.AppAnalyticsService;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import javax.ws.rs.ForbiddenException;
import org.testng.annotations.Test;

public class AnomalyResourceTest {

  @Test(expectedExceptions = ForbiddenException.class)
  public void testSetFeedback_withNoAccess() {
    final AnomalyManager anomalyManager = mock(AnomalyManager.class);
    when(anomalyManager.findById(1L))
        .thenReturn((AnomalyDTO) new AnomalyDTO().setId(1L));
    new AnomalyResource(
        anomalyManager,
        mock(AlertManager.class),
        mock(AppAnalyticsService.class),
        new AuthorizationManager(
            mock(AlertTemplateRenderer.class),
            AccessControlProvider.alwaysDeny
        )
    ).setFeedback(new ThirdEyePrincipal("nobody", ""), 1L, new AnomalyFeedbackApi());
  }
}
