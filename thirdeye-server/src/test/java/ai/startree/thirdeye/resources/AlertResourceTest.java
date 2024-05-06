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
package ai.startree.thirdeye.resources;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.alert.AlertEvaluator;
import ai.startree.thirdeye.service.alert.AlertInsightsProvider;
import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.NamespaceResolver;
import ai.startree.thirdeye.auth.ThirdEyeAuthorizerProvider;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.config.TimeConfiguration;
import ai.startree.thirdeye.resources.testutils.SingleNamespaceAuthorizer;
import ai.startree.thirdeye.resources.testutils.SingleResourceAuthorizer;
import ai.startree.thirdeye.service.AlertService;
import ai.startree.thirdeye.service.AnomalyMetricsProvider;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.PlanNodeApi;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import ai.startree.thirdeye.util.StringTemplateUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertResourceTest {

  private static final String VALID_CRON = "0 0 * * * ? *";

  static ThirdEyeServerPrincipal nobody() {
    return new ThirdEyeServerPrincipal("nobody", "", AuthenticationType.OAUTH);
  }

  private static AlertResource newAlertResource(final AlertManager alertManager,
      final AlertTemplateRenderer alertTemplateRenderer,
      final ThirdEyeAuthorizer thirdEyeAuthorizer) {
    final AuthorizationManager authorizationManager = newAuthorizationManager(alertTemplateRenderer,
        thirdEyeAuthorizer);
    return new AlertResource(newAlertService(alertManager, authorizationManager));
  }

  private static AlertService newAlertService(final AlertManager alertManager,
      final AuthorizationManager authorizationManager) {
    return new AlertService(
        alertManager,
        mock(AnomalyManager.class),
        mock(AlertEvaluator.class),
        mock(AnomalyMetricsProvider.class),
        mock(AlertInsightsProvider.class),
        mock(SubscriptionGroupManager.class),
        mock(EnumerationItemManager.class),
        mock(TaskManager.class),
        new TimeConfiguration(),
        authorizationManager
    );
  }

  private static AuthorizationManager newAuthorizationManager(
      final AlertTemplateRenderer alertTemplateRenderer,
      final ThirdEyeAuthorizer thirdEyeAuthorizer) {
    return new AuthorizationManager(alertTemplateRenderer,
        mock(AlertManager.class), 
        thirdEyeAuthorizer, new NamespaceResolver(null, null, null, null), new AuthConfiguration());
  }

  @Test
  public void testAlertEvaluationPlan() throws IOException, ClassNotFoundException {
    final ClassLoader classLoader = AlertResourceTest.class.getClassLoader();
    URL resource = requireNonNull(classLoader.getResource("alertEvaluation.json"));
    final AlertEvaluationApi apiTemplate = ThirdEyeSerialization.getObjectMapper()
        .readValue(resource, AlertEvaluationApi.class);
    resource = classLoader.getResource("alertEvaluation-context.json");
    final Map<String, Object> alertEvaluationPlanApiContext = ThirdEyeSerialization.getObjectMapper()
        .readValue(resource, Map.class);

    final AlertEvaluationApi api = StringTemplateUtils.applyContext(apiTemplate,
        alertEvaluationPlanApiContext);

    Assert.assertEquals(api.getAlert().getName(), "percentage-change-template");
    Assert.assertEquals(api.getAlert().getDescription(),
        "Percentage drop template");
    Assert.assertEquals(api.getAlert().getCron(), "0 0/1 * 1/1 * ? *");

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

  @Test(expectedExceptions = ForbiddenException.class)
  public void testCreateMultiple_withNoAccessToTemplate() {
    final AlertTemplateManager alertTemplateManager = mock(AlertTemplateManager.class);
    when(alertTemplateManager.findById(2L))
        .thenReturn(((AlertTemplateDTO) new AlertTemplateDTO().setId(2L)).setName("template1"));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(
        mock(AlertManager.class), alertTemplateManager);

    final ThirdEyeAuthorizer thirdEyeAuthorizer = new SingleResourceAuthorizer("0");

    newAlertResource(mock(AlertManager.class),
        alertTemplateRenderer,
        thirdEyeAuthorizer).createMultiple(
        nobody(),
        Collections.singletonList(
            new AlertApi().setName("alert1")
                .setCron(VALID_CRON)
                .setTemplate(new AlertTemplateApi().setId(2L))
        ));
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testRunTask_withNoAccess() {
    final AlertManager alertManager = mock(AlertManager.class);
    when(alertManager.findById(1L)).thenReturn((AlertDTO) new AlertDTO().setId(1L));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(alertManager,
        mock(AlertTemplateManager.class));

    newAlertResource(alertManager,
        alertTemplateRenderer,
        ThirdEyeAuthorizerProvider.ALWAYS_DENY).runTask(
        nobody(),
        1L,
        0L,
        1L);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testValidate_withNoAccess() {
    newAlertResource(mock(AlertManager.class),
        mock(AlertTemplateRenderer.class),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY).validateMultiple(
        nobody(),
        Collections.singletonList(
            new AlertApi().setTemplate(new AlertTemplateApi().setId(1L))
                .setName("alert1")
                .setCron(VALID_CRON)
        )
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testValidate_withNoAccessToTemplate() {
    final AlertTemplateManager alertTemplateManager = mock(AlertTemplateManager.class);
    when(alertTemplateManager.findById(1L))
        .thenReturn(((AlertTemplateDTO) new AlertTemplateDTO().setId(1L)).setName("template1")
            .setCron(VALID_CRON));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(
        mock(AlertManager.class), alertTemplateManager);

    final ThirdEyeAuthorizer thirdEyeAuthorizer = new SingleResourceAuthorizer("alert1");

    newAlertResource(mock(AlertManager.class),
        alertTemplateRenderer,
        thirdEyeAuthorizer).validateMultiple(
        nobody(),
        Collections.singletonList(
            new AlertApi().setTemplate(new AlertTemplateApi().setId(1L))
                .setName("alert1")
                .setCron(VALID_CRON)
        )
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testEvaluate_withNoAccessToTemplate() throws ExecutionException {
    final AlertTemplateManager alertTemplateManager = mock(AlertTemplateManager.class);
    when(alertTemplateManager.findById(1L)).thenReturn(
        (AlertTemplateDTO) new AlertTemplateDTO().setId(
            1L));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(
        mock(AlertManager.class), alertTemplateManager);

    newAlertResource(mock(AlertManager.class),
        alertTemplateRenderer,
        ThirdEyeAuthorizerProvider.ALWAYS_DENY).evaluate(nobody(),
        new AlertEvaluationApi()
            .setAlert(new AlertApi().setTemplate(new AlertTemplateApi().setId(1L)))
            .setStart(new Date())
            .setEnd(new Date())
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testEvaluate_withExistingAlertAndNoAccessToAlert() throws ExecutionException {
    final var alertTemplateManager = mock(AlertTemplateManager.class);
    final var alertTemplateRenderer = new AlertTemplateRenderer(mock(AlertManager.class),
        alertTemplateManager);
    final var alertEvaluator = mock(AlertEvaluator.class);
    final var alertManager = mock(AlertManager.class);

    final var alertTemplateDto = new AlertTemplateDTO();
    alertTemplateDto.setId(1L);
    alertTemplateDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("allowedNamespace"));

    final var alertDto = new AlertDTO();
    alertDto.setId(2L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("blockedNamespace"));
    alertDto.setTemplate(alertTemplateDto);

    final var alertEvaluationApi = new AlertEvaluationApi()
        .setAlert(new AlertApi().setId(2L))
        .setStart(new Date())
        .setEnd(new Date());

    when(alertTemplateManager.findById(1L)).thenReturn(alertTemplateDto);
    when(alertManager.findById(2L)).thenReturn(alertDto);
    when(alertEvaluator.evaluate(alertEvaluationApi))
        .thenReturn(new AlertEvaluationApi().setDetectionEvaluations(new HashMap<>()));

    new AlertResource(new AlertService(
        alertManager,
        mock(AnomalyManager.class),
        alertEvaluator,
        mock(AnomalyMetricsProvider.class),
        mock(AlertInsightsProvider.class),
        mock(SubscriptionGroupManager.class),
        mock(EnumerationItemManager.class),
        mock(TaskManager.class),
        new TimeConfiguration(),
        newAuthorizationManager(alertTemplateRenderer,
            SingleNamespaceAuthorizer.of("allowedNamespace"))
    )).evaluate(nobody(), alertEvaluationApi);
  }

  @Test
  public void testEvaluate_withExistingAlertAndReadAccessToAlertAndPartialAccessToEnums()
      throws ExecutionException {
    final var alertTemplateManager = mock(AlertTemplateManager.class);
    final var alertTemplateRenderer = new AlertTemplateRenderer(mock(AlertManager.class),
        alertTemplateManager);
    final var alertEvaluator = mock(AlertEvaluator.class);
    final var alertManager = mock(AlertManager.class);

    final var alertTemplateDto = new AlertTemplateDTO();
    alertTemplateDto.setId(1L);
    alertTemplateDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("allowedNamespace"));

    final var alertDto = new AlertDTO();
    alertDto.setId(2L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("allowedNamespace"));
    alertDto.setTemplate(alertTemplateDto);

    final var alertEvaluationApi = new AlertEvaluationApi()
        .setAlert(new AlertApi().setId(2L))
        .setStart(new Date())
        .setEnd(new Date());

    when(alertTemplateManager.findById(1L)).thenReturn(alertTemplateDto);
    when(alertManager.findById(2L)).thenReturn(alertDto);
    when(alertEvaluator.evaluate(alertEvaluationApi))
        .thenReturn(new AlertEvaluationApi().setDetectionEvaluations(
            new HashMap<>() {{
              put("allowedEval",
                  new DetectionEvaluationApi().setEnumerationItem(new EnumerationItemApi()
                      .setAuth(
                          new AuthorizationConfigurationApi().setNamespace("allowedNamespace"))));
              put("blockedEval",
                  new DetectionEvaluationApi().setEnumerationItem(new EnumerationItemApi()
                      .setAuth(
                          new AuthorizationConfigurationApi().setNamespace("blockedNamespace"))));
            }}
        ));

    final var alertResource = new AlertResource(new AlertService(
        alertManager,
        mock(AnomalyManager.class),
        alertEvaluator,
        mock(AnomalyMetricsProvider.class),
        mock(AlertInsightsProvider.class),
        mock(SubscriptionGroupManager.class),
        mock(EnumerationItemManager.class),
        mock(TaskManager.class),
        new TimeConfiguration(),
        newAuthorizationManager(alertTemplateRenderer,
            SingleNamespaceAuthorizer.of("allowedNamespace", AccessType.READ)))
    );

    try (final Response resp = alertResource.evaluate(nobody(), alertEvaluationApi)) {
      assertThat(resp.getStatus()).isEqualTo(200);

      final var results = ((AlertEvaluationApi) resp.getEntity());
      assertThat(results.getDetectionEvaluations().get("allowedEval")).isNotNull();
      assertThat(results.getDetectionEvaluations().get("blockedEval")).isNull();
    }
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testEvaluate_withNewAlertAndNoWriteAccess() throws ExecutionException {
    final var alertTemplateManager = mock(AlertTemplateManager.class);
    final var alertTemplateRenderer = new AlertTemplateRenderer(mock(AlertManager.class),
        alertTemplateManager);
    final var alertEvaluator = mock(AlertEvaluator.class);

    final var alertTemplateDto = new AlertTemplateDTO();
    alertTemplateDto.setId(1L);
    alertTemplateDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("readonlyNamespace"));

    final var alertApi = new AlertApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("readonlyNamespace"))
        .setTemplate(new AlertTemplateApi().setId(1L));

    final var alertEvaluationApi = new AlertEvaluationApi()
        .setAlert(alertApi)
        .setStart(new Date())
        .setEnd(new Date());

    when(alertTemplateManager.findById(1L)).thenReturn(alertTemplateDto);
    when(alertEvaluator.evaluate(alertEvaluationApi))
        .thenReturn(new AlertEvaluationApi().setDetectionEvaluations(new HashMap<>()));

    new AlertResource(new AlertService(
        mock(AlertManager.class),
        mock(AnomalyManager.class),
        alertEvaluator,
        mock(AnomalyMetricsProvider.class),
        mock(AlertInsightsProvider.class),
        mock(SubscriptionGroupManager.class),
        mock(EnumerationItemManager.class),
        mock(TaskManager.class),
        new TimeConfiguration(),
        newAuthorizationManager(alertTemplateRenderer,
            SingleNamespaceAuthorizer.of("readonlyNamespace", AccessType.READ)))
    ).evaluate(nobody(), alertEvaluationApi);
  }

  @Test
  public void testEvaluate_withNewAlertAndWriteAccessToAlertAndPartialAccessToEnums()
      throws ExecutionException {
    final var alertTemplateManager = mock(AlertTemplateManager.class);
    final var alertTemplateRenderer = new AlertTemplateRenderer(mock(AlertManager.class),
        alertTemplateManager);
    final var alertEvaluator = mock(AlertEvaluator.class);

    final var alertTemplateDto = new AlertTemplateDTO();
    alertTemplateDto.setId(1L);
    alertTemplateDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("allowedNamespace"));

    final var alertApi = new AlertApi()
        .setAuth(new AuthorizationConfigurationApi().setNamespace("allowedNamespace"))
        .setTemplate(new AlertTemplateApi().setId(1L));

    final var alertEvaluationApi = new AlertEvaluationApi()
        .setAlert(alertApi)
        .setStart(new Date())
        .setEnd(new Date());

    when(alertTemplateManager.findById(1L)).thenReturn(alertTemplateDto);
    when(alertEvaluator.evaluate(alertEvaluationApi))
        .thenReturn(new AlertEvaluationApi().setDetectionEvaluations(
            new HashMap<>() {{
              put("allowedEval",
                  new DetectionEvaluationApi().setEnumerationItem(new EnumerationItemApi()
                      .setAuth(
                          new AuthorizationConfigurationApi().setNamespace("allowedNamespace"))));
              put("blockedEval",
                  new DetectionEvaluationApi().setEnumerationItem(new EnumerationItemApi()
                      .setAuth(
                          new AuthorizationConfigurationApi().setNamespace("blockedNamespace"))));
            }}
        ));

    final var resource = new AlertResource(new AlertService(
        mock(AlertManager.class),
        mock(AnomalyManager.class),
        alertEvaluator,
        mock(AnomalyMetricsProvider.class),
        mock(AlertInsightsProvider.class),
        mock(SubscriptionGroupManager.class),
        mock(EnumerationItemManager.class),
        mock(TaskManager.class),
        new TimeConfiguration(),
        newAuthorizationManager(alertTemplateRenderer,
            SingleNamespaceAuthorizer.of("allowedNamespace"))));

    try (final Response resp = resource.evaluate(nobody(), alertEvaluationApi)) {
      assertThat(resp.getStatus()).isEqualTo(200);

      final var results = ((AlertEvaluationApi) resp.getEntity());
      assertThat(results.getDetectionEvaluations().get("allowedEval")).isNotNull();
      assertThat(results.getDetectionEvaluations().get("blockedEval")).isNull();
    }
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testReset_withNoAccess() {
    final AlertManager alertManager = mock(AlertManager.class);
    when(alertManager.findById(1L)).thenReturn((AlertDTO) new AlertDTO().setId(1L));
    final AlertTemplateRenderer alertTemplateRenderer = new AlertTemplateRenderer(alertManager,
        mock(AlertTemplateManager.class));

    newAlertResource(alertManager,
        alertTemplateRenderer,
        ThirdEyeAuthorizerProvider.ALWAYS_DENY).reset(
        nobody(),
        1L);
  }

  
}
