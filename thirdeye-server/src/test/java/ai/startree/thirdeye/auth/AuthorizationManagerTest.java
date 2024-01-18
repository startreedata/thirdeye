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
package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_NAME;
import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_NAMESPACE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AuthorizationManagerTest {

  @DataProvider
  public static Object[][] namespaceResolutionCases() {
    // see spec here: https://dev.startree.ai/docs/get-started-with-thirdeye/access-control-in-thirdeye#namespaces-for-thirdeye-resources
    final List<Object[]> testCases = new ArrayList<>();

    String testName = "null dto returns default values";
    testCases.add(testCase(testName, null, null, null, null,
        ResourceIdentifier.from(DEFAULT_NAME, DEFAULT_NAMESPACE,
            ResourceIdentifier.DEFAULT_ENTITY_TYPE)));

    testName = "DataSourceDto - Datasource with resource";
    DataSourceDTO dataSourceDTO = new DataSourceDTO();
    dataSourceDTO.setId(3L);
    dataSourceDTO.setAuth(new AuthorizationConfigurationDTO().setNamespace("datasource_namespace"));
    testCases.add(testCase(testName, null, null, null, dataSourceDTO,
        ResourceIdentifier.from("3", "datasource_namespace", "DATA_SOURCE")));

    testName = "DataSourceDto - Datasource without resource";
    dataSourceDTO = new DataSourceDTO();
    dataSourceDTO.setId(3L);
    testCases.add(testCase(testName, null, null, null, dataSourceDTO,
        ResourceIdentifier.from("3", "default", "DATA_SOURCE")));

    testName = "DatasetConfigDTO - Dataset with resource";
    DatasetConfigDTO datasetDto = new DatasetConfigDTO();
    datasetDto.setId(4L);
    datasetDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("dataset_namespace"));
    testCases.add(testCase(testName, null, null, null, datasetDto,
        ResourceIdentifier.from("4", "dataset_namespace", "DATASET")));

    testName = "DatasetConfigDTO - Dataset without resource";
    datasetDto = new DatasetConfigDTO();
    datasetDto.setId(4L);
    testCases.add(testCase(testName, null, null, null, datasetDto,
        ResourceIdentifier.from("4", "default", "DATASET")));

    testName = "AlertTemplateDTO - Template with resource";
    AlertTemplateDTO alertTemplateDTO = new AlertTemplateDTO();
    alertTemplateDTO.setId(5L);
    alertTemplateDTO.setAuth(
        new AuthorizationConfigurationDTO().setNamespace("alert_template_namespace"));
    testCases.add(testCase(testName, null, null, null, alertTemplateDTO,
        ResourceIdentifier.from("5", "alert_template_namespace", "ALERT_TEMPLATE")));

    testName = "AlertTemplateDTO - Template without resource";
    alertTemplateDTO = new AlertTemplateDTO();
    alertTemplateDTO.setId(5L);
    testCases.add(testCase(testName, null, null, null, alertTemplateDTO,
        ResourceIdentifier.from("5", "default", "ALERT_TEMPLATE")));

    testName = "AlertDTO - Alert with resource";
    AlertDTO alertDTO = new AlertDTO();
    alertDTO.setId(6L);
    alertDTO.setAuth(new AuthorizationConfigurationDTO().setNamespace("alert_template_namespace"));
    testCases.add(testCase(testName, null, null, null, alertDTO,
        ResourceIdentifier.from("6", "alert_template_namespace", "ALERT")));

    testName = "AlertDTO - Alert without resource";
    alertDTO = new AlertDTO();
    alertDTO.setId(6L);
    testCases.add(testCase(testName, null, null, null, alertDTO,
        ResourceIdentifier.from("6", "default", "ALERT")));

    // Enumeration item can inherit from alert
    testName = "EnumerationItemDto - Enum with resource, alert with resource.";
    AlertDTO alertDto = alertWithResource();
    EnumerationItemDTO enumItem = enumWithResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, enumItem,
        ResourceIdentifier.from("2", "enum_namespace", "ENUMERATION_ITEM")));

    testName = "EnumerationItemDto - Enum without resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithoutResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, enumItem,
        ResourceIdentifier.from("2", "alert_namespace", "ENUMERATION_ITEM")));

    testName = "EnumerationItemDto - Enum with resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, enumItem,
        ResourceIdentifier.from("2", "enum_namespace", "ENUMERATION_ITEM")));

    testName = "EnumerationItemDto - Enum without resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithoutResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, enumItem,
        ResourceIdentifier.from("2", "default", "ENUMERATION_ITEM")));

    // Anomalies can inherit from enumeration item or alert
    testName = "AnomalyDto - Enum with resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithResource();
    AnomalyDTO anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, anomalyDTO,
        ResourceIdentifier.from("3", "enum_namespace", "ANOMALY")));

    testName = "AnomalyDto - Enum without resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, anomalyDTO,
        ResourceIdentifier.from("3", "alert_namespace", "ANOMALY")));

    testName = "AnomalyDto - Enum with resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithResource();
    anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, anomalyDTO,
        ResourceIdentifier.from("3", "enum_namespace", "ANOMALY")));

    testName = "AnomalyDto - Enum without resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, anomalyDTO,
        ResourceIdentifier.from("3", "default", "ANOMALY")));

    testName = "AnomalyDto - No enum, alert with resource.";
    alertDto = alertWithResource();
    anomalyDTO = anomalyWithoutEnum();
    testCases.add(testCase(testName, alertDto, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "alert_namespace", "ANOMALY")));

    testName = "AnomalyDto - No enum, alert without resource.";
    alertDto = alertWithoutResource();
    anomalyDTO = anomalyWithoutEnum();
    testCases.add(testCase(testName, alertDto, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "default", "ANOMALY")));

    // rca investigation inherit from the anomaly
    testName = "RcaInvestigationDTO - Enum with resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithResource();
    anomalyDTO = anomalyWithEnum();
    RcaInvestigationDTO rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, rcaDto,
        ResourceIdentifier.from("7", "enum_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - Enum without resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, rcaDto,
        ResourceIdentifier.from("7", "alert_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - Enum with resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithResource();
    anomalyDTO = anomalyWithEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, rcaDto,
        ResourceIdentifier.from("7", "enum_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - Enum without resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, rcaDto,
        ResourceIdentifier.from("7", "default", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - No enum, alert with resource.";
    alertDto = alertWithResource();
    anomalyDTO = anomalyWithoutEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, null, anomalyDTO, rcaDto,
        ResourceIdentifier.from("7", "alert_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - No enum, alert without resource.";
    alertDto = alertWithoutResource();
    anomalyDTO = anomalyWithoutEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, null, anomalyDTO, rcaDto,
        ResourceIdentifier.from("7", "default", "RCA_INVESTIGATION")));

    // FIXME use cases below are not clearly defined in the spec: https://dev.startree.ai/docs/get-started-with-thirdeye/access-control-in-thirdeye#namespaces-for-thirdeye-resources
    //  testing the current behaviour to detect behaviour changes but feel free to change the behaviour
    // spec: other DTOs always return the default namespace
    // --> it's not enforced in the Namespace resolver
    testName = "SubscriptionDto - returns a custom namespace";
    SubscriptionGroupDTO subscriptionGroup = new SubscriptionGroupDTO();
    subscriptionGroup.setId(8L);
    subscriptionGroup.setAuth(
        new AuthorizationConfigurationDTO().setNamespace("subscription_namespace"));
    testCases.add(testCase(testName, alertDto, null, anomalyDTO, subscriptionGroup,
        ResourceIdentifier.from("8", "subscription_namespace", "SUBSCRIPTION_GROUP")));

    return testCases.toArray(new Object[][]{});
  }

  private static Object[] testCase(final String testName, final AlertDTO alert,
      final EnumerationItemDTO enumItem, final AnomalyDTO anomaly, final AbstractDTO inputDto,
      ResourceIdentifier expected) {
    return new Object[]{testName, alert, enumItem, anomaly, inputDto, expected};
  }

  // inputDto is the entity having its namespace resolved
  // alert and enumItem are entities that are potentially used when the namespace is resolved
  @Test(dataProvider = "namespaceResolutionCases")
  public void testNamespaceResolution(final String testName, final AlertDTO alert,
      final EnumerationItemDTO enumItem, final AnomalyDTO anomaly, final AbstractDTO inputDto,
      ResourceIdentifier expected) {
    final AlertManager alertManager = mock(AlertManager.class);
    if (alert != null) {
      when(alertManager.findById(alert.getId())).thenReturn(alert);
    }
    final EnumerationItemManager enumManager = mock(EnumerationItemManager.class);
    if (enumItem != null) {
      when(enumManager.findById(enumItem.getId())).thenReturn(enumItem);
    }
    final AnomalyManager anomalyManager = mock(AnomalyManager.class);
    if (anomaly != null) {
      when(anomalyManager.findById(anomaly.getId())).thenReturn(anomaly);
    }
    final AuthorizationManager authorizationManager = new AuthorizationManager(null, null,
        new NamespaceResolver(alertManager, enumManager, anomalyManager));
    final ResourceIdentifier output = authorizationManager.resourceId(inputDto);
    // FIXME CYRIL write equals method
    assertThat(output.getName()).isEqualTo(expected.getName());
    assertThat(output.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(output.getEntityType()).isEqualTo(expected.getEntityType());
  }

  private static EnumerationItemDTO enumWithResource() {
    final var enumItem = new EnumerationItemDTO();
    enumItem.setId(2L);
    enumItem.setAuth(new AuthorizationConfigurationDTO().setNamespace("enum_namespace"));
    AlertDTO alertRef = new AlertDTO();
    alertRef.setId(1L);
    enumItem.setAlert(alertRef);
    return enumItem;
  }

  private static EnumerationItemDTO enumWithoutResource() {
    final var enumItem = new EnumerationItemDTO();
    enumItem.setId(2L);
    AlertDTO alertRef = new AlertDTO();
    alertRef.setId(1L);
    enumItem.setAlert(alertRef);
    return enumItem;
  }

  @NonNull
  private static AlertDTO alertWithResource() {
    AlertDTO alertDto = new AlertDTO();
    alertDto.setId(1L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("alert_namespace"));
    return alertDto;
  }

  @NonNull
  private static AlertDTO alertWithoutResource() {
    AlertDTO alertDto = new AlertDTO();
    alertDto.setId(1L);
    return alertDto;
  }

  private static AnomalyDTO anomalyWithEnum() {
    final AnomalyDTO anomalyDTO = new AnomalyDTO();
    anomalyDTO.setEnumerationItem(
        new EnumerationItemDTO().setId(2L).setAlert((AlertDTO) new AlertDTO().setId(1L)));
    anomalyDTO.setDetectionConfigId(1L);
    anomalyDTO.setId(3L);
    return anomalyDTO;
  }

  private static AnomalyDTO anomalyWithoutEnum() {
    final AnomalyDTO anomalyDTO = new AnomalyDTO();
    anomalyDTO.setDetectionConfigId(1L);
    anomalyDTO.setId(3L);
    return anomalyDTO;
  }

  @NonNull
  private static RcaInvestigationDTO rcaOfAnomaly(final AnomalyDTO anomalyDTO) {
    RcaInvestigationDTO rcaDto = new RcaInvestigationDTO();
    rcaDto.setAnomaly((AnomalyDTO) new AnomalyDTO().setId(anomalyDTO.getId()));
    rcaDto.setId(7L);
    return rcaDto;
  }
}
