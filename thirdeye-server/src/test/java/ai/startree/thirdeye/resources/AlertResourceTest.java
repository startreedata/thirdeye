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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.alert.AlertCreater;
import ai.startree.thirdeye.alert.AlertDeleter;
import ai.startree.thirdeye.alert.AlertEvaluator;
import ai.startree.thirdeye.alert.AlertInsightsProvider;
import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.accessControl.AccessControl;
import ai.startree.thirdeye.auth.AccessControlProvider;
import ai.startree.thirdeye.spi.accessControl.AccessType;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.spi.accessControl.ResourceIdentifier;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.core.AppAnalyticsService;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.PlanNodeApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import ai.startree.thirdeye.util.StringTemplateUtils;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.ForbiddenException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertResourceTest {

  @Test
  public void testAlertEvaluationPlan() throws IOException, ClassNotFoundException {
    final ClassLoader classLoader = AlertResourceTest.class.getClassLoader();
    URL resource = requireNonNull(classLoader.getResource("alertEvaluation.json"));
    final String jsonString = Resources.toString(resource, StandardCharsets.UTF_8);
    resource = classLoader.getResource("alertEvaluation-context.json");
    final Map<String, Object> alertEvaluationPlanApiContext = ThirdEyeSerialization.getObjectMapper()
        .readValue(resource.openStream(), Map.class);

    final AlertEvaluationApi api = ThirdEyeSerialization.getObjectMapper()
        .readValue(StringTemplateUtils.renderTemplate(
            jsonString,
            alertEvaluationPlanApiContext), AlertEvaluationApi.class);

    Assert.assertEquals(api.getAlert().getName(), "percentage-change-template");
    Assert.assertEquals(api.getAlert().getDescription(),
        "Percentage drop template");
    Assert.assertEquals(api.getAlert().getCron(), "0 0/1 * 1/1 * ? *");
    Assert.assertEquals(api.getStart(), new Date(1621300000));
    Assert.assertEquals(api.getEnd(), new Date(1621200000));

    assertThat(api.getAlert()).isNotNull();
    assertThat(api.getAlert().getTemplate()).isNotNull();

    final List<PlanNodeApi> nodes = api.getAlert().getTemplate().getNodes();
    Assert.assertEquals(nodes.size(), 5);
    Assert.assertEquals(nodes.get(0).getName(), "root");
    Assert.assertEquals(nodes.get(1).getName(),
        "percentageChangeDetector");
    Assert.assertEquals(nodes.get(2).getName(),
        "baselineDataFetcher");
    Assert.assertEquals(nodes.get(3).getName(),
        "currentDataFetcher");
    Assert.assertEquals(nodes.get(4).getName(), "sqlJoin");
    Assert.assertEquals(nodes.get(0).getType(), "ChildKeepingMerge");
    Assert.assertEquals(nodes.get(1).getType(), "AnomalyDetector");
    Assert.assertEquals(nodes.get(2).getType(), "DataFetcher");
    Assert.assertEquals(nodes.get(3).getType(), "DataFetcher");
    Assert.assertEquals(nodes.get(4).getType(), "SqlQueryExecutor");
  }

  static ThirdEyePrincipal nobody() {
    return new ThirdEyePrincipal("nobody", "");
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testCreateMultiple_withNoAccessToTemplate() {
    final AlertTemplateManager alertTemplateManager = mock(AlertTemplateManager.class);
    when(alertTemplateManager.findById(2L))
        .thenReturn(((AlertTemplateDTO) new AlertTemplateDTO().setId(2L)).setName("template1"));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(
        mock(AlertManager.class), alertTemplateManager);

    final AccessControl accessControl = (String token, ResourceIdentifier identifier, AccessType accessType)
        -> identifier.name.equals("0");

    new AlertResource(
        mock(AlertManager.class),
        mock(AlertCreater.class),
        mock(AlertDeleter.class),
        mock(AlertEvaluator.class),
        mock(AppAnalyticsService.class),
        mock(AlertInsightsProvider.class),
        new AuthorizationManager(
            alertTemplateRenderer,
            accessControl
        )
    ).createMultiple(nobody(), Collections.singletonList(
        new AlertApi().setName("alert1").setTemplate(new AlertTemplateApi().setId(2L))
    ));
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testRunTask_withNoAccess() {
    final AlertManager alertManager = mock(AlertManager.class);
    when(alertManager.findById(1L)).thenReturn((AlertDTO) new AlertDTO().setId(1L));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(alertManager,
        mock(AlertTemplateManager.class));

    new AlertResource(
        alertManager,
        mock(AlertCreater.class),
        mock(AlertDeleter.class),
        mock(AlertEvaluator.class),
        mock(AppAnalyticsService.class),
        mock(AlertInsightsProvider.class),
        new AuthorizationManager(
            alertTemplateRenderer,
            AccessControlProvider.alwaysDeny
        )
    ).runTask(nobody(), 1L, 0L, 1L);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testValidate_withNoAccess() {
    new AlertResource(
        mock(AlertManager.class),
        mock(AlertCreater.class),
        mock(AlertDeleter.class),
        mock(AlertEvaluator.class),
        mock(AppAnalyticsService.class),
        mock(AlertInsightsProvider.class),
        new AuthorizationManager(
            mock(AlertTemplateRenderer.class),
            AccessControlProvider.alwaysDeny
        )
    ).validateMultiple(
        nobody(),
        Collections.singletonList(
            new AlertApi().setTemplate(new AlertTemplateApi().setId(1L)).setName("alert1")
        )
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testValidate_withNoAccessToTemplate() {
    final AlertTemplateManager alertTemplateManager = mock(AlertTemplateManager.class);
    when(alertTemplateManager.findById(1L))
        .thenReturn(((AlertTemplateDTO) new AlertTemplateDTO().setId(1L)).setName("template1"));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(mock(AlertManager.class),alertTemplateManager);

    final AccessControl accessControl = (String token, ResourceIdentifier identifier, AccessType accessType)
        -> identifier.name.equals("alert1");

    new AlertResource(
        mock(AlertManager.class),
        mock(AlertCreater.class),
        mock(AlertDeleter.class),
        mock(AlertEvaluator.class),
        mock(AppAnalyticsService.class),
        mock(AlertInsightsProvider.class),
        new AuthorizationManager(
            alertTemplateRenderer,
            accessControl
        )
    ).validateMultiple(
        new ThirdEyePrincipal("nobody", ""),
        Collections.singletonList(
            new AlertApi().setTemplate(new AlertTemplateApi().setId(1L)).setName("alert1")
        )
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testEvaluate_withNoAccessToTemplate() throws ExecutionException {
    final AlertTemplateManager alertTemplateManager = mock(AlertTemplateManager.class);
    when(alertTemplateManager.findById(1L)).thenReturn(
        (AlertTemplateDTO) new AlertTemplateDTO().setId(
            1L));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(mock(AlertManager.class), alertTemplateManager);

    new AlertResource(
        mock(AlertManager.class),
        mock(AlertCreater.class),
        mock(AlertDeleter.class),
        mock(AlertEvaluator.class),
        mock(AppAnalyticsService.class),
        mock(AlertInsightsProvider.class),
        new AuthorizationManager(
            alertTemplateRenderer,
            AccessControlProvider.alwaysDeny
        )
    ).evaluate(nobody(),
        new AlertEvaluationApi()
            .setAlert(new AlertApi().setTemplate(new AlertTemplateApi().setId(1L)))
            .setStart(new Date())
            .setEnd(new Date())
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testReset_withNoAccess() {
    final AlertManager alertManager = mock(AlertManager.class);
    when(alertManager.findById(1L)).thenReturn((AlertDTO) new AlertDTO().setId(1L));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(alertManager, mock(AlertTemplateManager.class));

    new AlertResource(
        alertManager,
        mock(AlertCreater.class),
        mock(AlertDeleter.class),
        mock(AlertEvaluator.class),
        mock(AppAnalyticsService.class),
        mock(AlertInsightsProvider.class),
        new AuthorizationManager(
            alertTemplateRenderer,
            AccessControlProvider.alwaysDeny
        )
    ).reset(nobody(), 1L);
  }
}
