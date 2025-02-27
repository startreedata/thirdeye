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

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

public class ApiResource {

  private final AppAnalyticsResource appAnalyticsResource;
  private final AuthResource authResource;
  private final AuthInfoResource authInfoResource;
  private final DataSourceResource dataSourceResource;
  private final DatasetResource datasetResource;
  private final MetricResource metricResource;
  private final AlertResource alertResource;
  private final AlertTemplateResource alertTemplateResource;
  private final SubscriptionGroupResource subscriptionGroupResource;
  private final AnomalyResource anomalyResource;
  private final EntityResource entityResource;
  private final RcaResource rcaResource;
  private final EventResource eventResource;
  private final TaskResource taskResource;
  private final UiResource uiResource;
  private final EnumerationItemResource enumerationItemResource;
  private final NamespaceResource namespaceResource;
  private final NamespaceConfigurationResource namespaceConfigurationResource;

  @Inject
  public ApiResource(final AppAnalyticsResource appAnalyticsResource,
      final AuthResource authResource,
      final AuthInfoResource authInfoResource,
      final DataSourceResource dataSourceResource,
      final DatasetResource datasetResource,
      final MetricResource metricResource,
      final AlertResource alertResource,
      final AlertTemplateResource alertTemplateResource,
      final SubscriptionGroupResource subscriptionGroupResource,
      final AnomalyResource anomalyResource,
      final EntityResource entityResource,
      final RcaResource rcaResource,
      final EventResource eventResource,
      final TaskResource taskResource,
      final UiResource uiResource,
      final EnumerationItemResource enumerationItemResource,
      final NamespaceResource namespaceResource,
      final NamespaceConfigurationResource namespaceConfigurationResource) {
    this.appAnalyticsResource = appAnalyticsResource;
    this.authResource = authResource;
    this.authInfoResource = authInfoResource;
    this.dataSourceResource = dataSourceResource;
    this.datasetResource = datasetResource;
    this.metricResource = metricResource;
    this.alertResource = alertResource;
    this.alertTemplateResource = alertTemplateResource;
    this.subscriptionGroupResource = subscriptionGroupResource;
    this.anomalyResource = anomalyResource;
    this.entityResource = entityResource;
    this.rcaResource = rcaResource;
    this.eventResource = eventResource;
    this.taskResource = taskResource;
    this.uiResource = uiResource;
    this.enumerationItemResource = enumerationItemResource;
    this.namespaceResource = namespaceResource;
    this.namespaceConfigurationResource = namespaceConfigurationResource;
  }

  @Path("app-analytics")
  public AppAnalyticsResource getAnalyticsResource() {
    return appAnalyticsResource;
  }

  @Path("auth")
  public AuthResource getAuthResource() {
    return authResource;
  }

  @Path("info")
  public AuthInfoResource getAuthInfoResource() {
    return authInfoResource;
  }

  @Path("data-sources")
  public DataSourceResource getDataSourceResource() {
    return dataSourceResource;
  }

  @Path("datasets")
  public DatasetResource getDatasetResource() {
    return datasetResource;
  }

  @Path("metrics")
  public MetricResource getMetricResource() {
    return metricResource;
  }

  @Path("alerts")
  public AlertResource getAlertResource() {
    return alertResource;
  }

  @Path("alert-templates")
  public AlertTemplateResource getAlertTemplateResource() {
    return alertTemplateResource;
  }

  @Path("enumeration-items")
  public EnumerationItemResource getEnumerationItemResource() {
    return enumerationItemResource;
  }

  // using the same route as in other Startree apps - workspace is equivalent to namespace in ThirdEye
  @Path("workspaces")
  public NamespaceResource getNamespaceResource() {
    return namespaceResource;
  }

  @Path("subscription-groups")
  public SubscriptionGroupResource getSubscriptionGroupResource() {
    return subscriptionGroupResource;
  }

  @Path("anomalies")
  public AnomalyResource getAnomalyResource() {
    return anomalyResource;
  }

  @Path("entities")
  public EntityResource getEntityResource() {
    return entityResource;
  }

  @Path("rca")
  public RcaResource getRcaResource() {
    return rcaResource;
  }

  @Path("events")
  public EventResource getEventResource() {
    return eventResource;
  }

  @Path("tasks")
  public TaskResource getTaskResource() {
    return taskResource;
  }

  @Path("ui")
  public UiResource getUIResource() {
    return uiResource;
  }

  @Path("workspace-configuration")
  public NamespaceConfigurationResource getNamespaceConfigurationResource() {
    return namespaceConfigurationResource;
  }
}
