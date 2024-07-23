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
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskType;
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
    testCases.add(testCase(testName, null, null, null, null, null,
        ResourceIdentifier.from(DEFAULT_NAME, DEFAULT_NAMESPACE,
            ResourceIdentifier.DEFAULT_ENTITY_TYPE)));

    testName = "DataSourceDto - Datasource with resource";
    DataSourceDTO dataSourceDTO = new DataSourceDTO();
    dataSourceDTO.setId(3L);
    dataSourceDTO.setAuth(new AuthorizationConfigurationDTO().setNamespace("datasource_namespace"));
    testCases.add(testCase(testName, null, null, null, null, dataSourceDTO,
        ResourceIdentifier.from("3", "datasource_namespace", "DATA_SOURCE")));

    testName = "DataSourceDto - Datasource without resource";
    dataSourceDTO = new DataSourceDTO();
    dataSourceDTO.setId(3L);
    testCases.add(testCase(testName, null, null, null, null, dataSourceDTO,
        ResourceIdentifier.from("3", "default", "DATA_SOURCE")));

    testName = "DatasetConfigDTO - Dataset with resource";
    DatasetConfigDTO datasetDto = new DatasetConfigDTO();
    datasetDto.setId(4L);
    datasetDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("dataset_namespace"));
    testCases.add(testCase(testName, null, null, null, null, datasetDto,
        ResourceIdentifier.from("4", "dataset_namespace", "DATASET")));

    testName = "DatasetConfigDTO - Dataset without resource";
    datasetDto = new DatasetConfigDTO();
    datasetDto.setId(4L);
    testCases.add(testCase(testName, null, null, null, null, datasetDto,
        ResourceIdentifier.from("4", "default", "DATASET")));

    testName = "AlertTemplateDTO - Template with resource";
    AlertTemplateDTO alertTemplateDTO = new AlertTemplateDTO();
    alertTemplateDTO.setId(5L);
    alertTemplateDTO.setAuth(
        new AuthorizationConfigurationDTO().setNamespace("alert_template_namespace"));
    testCases.add(testCase(testName, null, null, null, null, alertTemplateDTO,
        ResourceIdentifier.from("5", "alert_template_namespace", "ALERT_TEMPLATE")));

    testName = "AlertTemplateDTO - Template without resource";
    alertTemplateDTO = new AlertTemplateDTO();
    alertTemplateDTO.setId(5L);
    testCases.add(testCase(testName, null, null, null, null, alertTemplateDTO,
        ResourceIdentifier.from("5", "default", "ALERT_TEMPLATE")));

    testName = "AlertDTO - Alert with resource";
    AlertDTO alertDto = new AlertDTO();
    alertDto.setId(6L);
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("alert_template_namespace"));
    testCases.add(testCase(testName, null, null, null, null, alertDto,
        ResourceIdentifier.from("6", "alert_template_namespace", "ALERT")));

    testName = "AlertDTO - Alert without resource";
    alertDto = new AlertDTO();
    alertDto.setId(6L);
    testCases.add(testCase(testName, null, null, null, null, alertDto,
        ResourceIdentifier.from("6", "default", "ALERT")));

    // Enumeration item can inherit from alert
    testName = "EnumerationItemDto - Enum with resource, alert with resource.";
    alertDto = alertWithResource();
    EnumerationItemDTO enumItem = enumWithResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, enumItem,
        ResourceIdentifier.from("2", "enum_namespace", "ENUMERATION_ITEM")));

    testName = "EnumerationItemDto - Enum without resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithoutResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, enumItem,
        ResourceIdentifier.from("2", "alert_namespace", "ENUMERATION_ITEM")));

    testName = "EnumerationItemDto - Enum with resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, enumItem,
        ResourceIdentifier.from("2", "enum_namespace", "ENUMERATION_ITEM")));

    testName = "EnumerationItemDto - Enum without resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithoutResource();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, enumItem,
        ResourceIdentifier.from("2", "default", "ENUMERATION_ITEM")));

    // Anomalies can inherit from enumeration item or alert
    testName = "AnomalyDto - Enum with resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithResource();
    AnomalyDTO anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "enum_namespace", "ANOMALY")));

    testName = "AnomalyDto - Enum without resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "alert_namespace", "ANOMALY")));

    testName = "AnomalyDto - Enum with resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithResource();
    anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "enum_namespace", "ANOMALY")));

    testName = "AnomalyDto - Enum without resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    testCases.add(testCase(testName, alertDto, enumItem, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "default", "ANOMALY")));

    testName = "AnomalyDto - No enum, alert with resource.";
    alertDto = alertWithResource();
    anomalyDTO = anomalyWithoutEnum();
    testCases.add(testCase(testName, alertDto, null, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "alert_namespace", "ANOMALY")));

    testName = "AnomalyDto - No enum, alert without resource.";
    alertDto = alertWithoutResource();
    anomalyDTO = anomalyWithoutEnum();
    testCases.add(testCase(testName, alertDto, null, null, null, anomalyDTO,
        ResourceIdentifier.from("3", "default", "ANOMALY")));

    // rca investigation inherit from the anomaly
    testName = "RcaInvestigationDTO - Enum with resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithResource();
    anomalyDTO = anomalyWithEnum();
    RcaInvestigationDTO rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, null, rcaDto,
        ResourceIdentifier.from("7", "enum_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - Enum without resource, alert with resource.";
    alertDto = alertWithResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, null, rcaDto,
        ResourceIdentifier.from("7", "alert_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - Enum with resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithResource();
    anomalyDTO = anomalyWithEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, null, rcaDto,
        ResourceIdentifier.from("7", "enum_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - Enum without resource, alert without resource.";
    alertDto = alertWithoutResource();
    enumItem = enumWithoutResource();
    anomalyDTO = anomalyWithEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, enumItem, anomalyDTO, null, rcaDto,
        ResourceIdentifier.from("7", "default", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - No enum, alert with resource.";
    alertDto = alertWithResource();
    anomalyDTO = anomalyWithoutEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, null, anomalyDTO, null, rcaDto,
        ResourceIdentifier.from("7", "alert_namespace", "RCA_INVESTIGATION")));

    testName = "RcaInvestigationDTO - No enum, alert without resource.";
    alertDto = alertWithoutResource();
    anomalyDTO = anomalyWithoutEnum();
    rcaDto = rcaOfAnomaly(anomalyDTO);
    testCases.add(testCase(testName, alertDto, null, anomalyDTO, null, rcaDto,
        ResourceIdentifier.from("7", "default", "RCA_INVESTIGATION")));

    testName = "TaskDTO - DETECTION type with Alert with resource.";
    alertDto = alertWithResource();
    TaskDTO taskDto = (TaskDTO) new TaskDTO().setTaskType(TaskType.DETECTION)
        .setRefId(alertDto.getId())
        .setId(9L);
    testCases.add(testCase(testName, alertDto, null, null, null, taskDto,
        ResourceIdentifier.from("9", "alert_namespace", "TASK")));

    testName = "TaskDTO - DETECTION type with Alert without resource.";
    alertDto = alertWithoutResource();
    taskDto = (TaskDTO) new TaskDTO().setTaskType(TaskType.DETECTION)
        .setRefId(alertDto.getId())
        .setId(10L);
    testCases.add(testCase(testName, alertDto, null, null, null, taskDto,
        ResourceIdentifier.from("10", "default", "TASK")));

    testName = "TaskDTO - NOTIFICATION type with SubscriptionGroup with resource.";
    SubscriptionGroupDTO subscriptionGroupDto = subscriptionGroupWithResource();
    taskDto = (TaskDTO) new TaskDTO().setTaskType(TaskType.NOTIFICATION)
        .setRefId(subscriptionGroupDto.getId())
        .setId(11L);
    testCases.add(testCase(testName, null, null, null, subscriptionGroupDto, taskDto,
        ResourceIdentifier.from("11", "subscription_namespace", "TASK")));

    testName = "TaskDTO - NOTIFICATION type with SubscriptionGroup without resource.";
    subscriptionGroupDto = subscriptionGroupWithoutResource();
    taskDto = (TaskDTO) new TaskDTO().setTaskType(TaskType.NOTIFICATION)
        .setRefId(subscriptionGroupDto.getId())
        .setId(12L);
    testCases.add(testCase(testName, null, null, null, subscriptionGroupDto, taskDto,
        ResourceIdentifier.from("12", "default", "TASK")));

    testName = "SubscriptionGroupDTO - Subscription with resource";
    subscriptionGroupDto = subscriptionGroupWithResource();
    testCases.add(testCase(testName, null, null, null, null, subscriptionGroupDto,
        ResourceIdentifier.from("4", "subscription_namespace", "SUBSCRIPTION_GROUP")));

    testName = "SubscriptionGroupDTO - Subscription without resource";
    subscriptionGroupDto = subscriptionGroupWithoutResource();
    testCases.add(testCase(testName, null, null, null, null, subscriptionGroupDto,
        ResourceIdentifier.from("4", "default", "SUBSCRIPTION_GROUP")));

    // FIXME CYRIL authz do metricDTO
    // FIXME CYRIL authz do Events

    // spec:other DTOs always return the default namespace - https://dev.startree.ai/docs/get-started-with-thirdeye/access-control-in-thirdeye#namespaces-for-thirdeye-resources
    // --> it's not enforced in the Namespace resolver - so capturing this undefined behavior in this test to catch changes
    testName = "OtherDto - returns a custom namespace - undefined behavior";
    AnomalyFeedbackDTO undefinedBehaviorDto = new AnomalyFeedbackDTO();
    undefinedBehaviorDto.setId(8L);
    undefinedBehaviorDto.setAuth(
        new AuthorizationConfigurationDTO().setNamespace("custom_namespace"));
    testCases.add(testCase(testName, alertDto, null, anomalyDTO, null, undefinedBehaviorDto,
        ResourceIdentifier.from("8", "custom_namespace", "ANOMALY_FEEDBACK")));

    return testCases.toArray(new Object[][]{});
  }

  private static Object[] testCase(final String testName, final AlertDTO alert,
      final EnumerationItemDTO enumItem, final AnomalyDTO anomaly,
      final SubscriptionGroupDTO subscriptionGroup,
      final AbstractDTO inputDto,
      final ResourceIdentifier expected) {
    return new Object[]{testName, alert, enumItem, anomaly, inputDto, subscriptionGroup, expected};
  }

  // inputDto is the entity having its namespace resolved
  // alert and enumItem are entities that are potentially used when the namespace is resolved
  @Test(dataProvider = "namespaceResolutionCases")
  public void testNamespaceResolution(final String testName, final AlertDTO alert,
      final EnumerationItemDTO enumItem, final AnomalyDTO anomaly, final AbstractDTO inputDto,
      final SubscriptionGroupDTO subscriptionGroup, final ResourceIdentifier expected) {
    final AlertManager alertManager = mock(AlertManager.class);
    if (alert != null) {
      when(alertManager.findById(alert.getId())).thenReturn(alert);
    }
    final SubscriptionGroupManager subscriptionGroupManager = mock(SubscriptionGroupManager.class);
    if (subscriptionGroup != null) {
      when(subscriptionGroupManager.findById(subscriptionGroup.getId())).thenReturn(
          subscriptionGroup);
    }
    final AuthorizationManager authorizationManager = new AuthorizationManager(null, null, null, null,
        new AuthConfiguration());
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
    final AlertDTO alertDto = alertWithoutResource();
    alertDto.setAuth(new AuthorizationConfigurationDTO().setNamespace("alert_namespace"));
    return alertDto;
  }

  @NonNull
  private static AlertDTO alertWithoutResource() {
    final AlertDTO alertDto = new AlertDTO();
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
  private static SubscriptionGroupDTO subscriptionGroupWithoutResource() {
    final SubscriptionGroupDTO dto = new SubscriptionGroupDTO();
    dto.setId(4L);
    return dto;
  }

  private static SubscriptionGroupDTO subscriptionGroupWithResource() {
    final SubscriptionGroupDTO dto = subscriptionGroupWithoutResource();
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace("subscription_namespace"));
    return dto;
  }

  @NonNull
  private static RcaInvestigationDTO rcaOfAnomaly(final AnomalyDTO anomalyDTO) {
    RcaInvestigationDTO rcaDto = new RcaInvestigationDTO();
    rcaDto.setAnomaly((AnomalyDTO) new AnomalyDTO().setId(anomalyDTO.getId()));
    rcaDto.setId(7L);
    return rcaDto;
  }
}
