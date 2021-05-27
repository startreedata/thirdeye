package org.apache.pinot.thirdeye.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;

public class ApiResource {

  private final AuthResource authResource;
  private final ApplicationResource applicationResource;
  private final DataSourceResource dataSourceResource;
  private final DatasetResource datasetResource;
  private final MetricResource metricResource;
  private final AlertResource alertResource;
  private final SubscriptionGroupResource subscriptionGroupResource;
  private final AnomalyResource anomalyResource;
  private final EntityResource entityResource;
  private final RcaResource rcaResource;
  private final EventResource eventResource;

  @Inject
  public ApiResource(final AuthResource authResource,
      final ApplicationResource applicationResource,
      final DataSourceResource dataSourceResource,
      final DatasetResource datasetResource,
      final MetricResource metricResource,
      final AlertResource alertResource,
      final SubscriptionGroupResource subscriptionGroupResource,
      final AnomalyResource anomalyResource,
      final EntityResource entityResource,
      final RcaResource rcaResource,
      final EventResource eventResource) {
    this.authResource = authResource;
    this.applicationResource = applicationResource;
    this.dataSourceResource = dataSourceResource;
    this.datasetResource = datasetResource;
    this.metricResource = metricResource;
    this.alertResource = alertResource;
    this.subscriptionGroupResource = subscriptionGroupResource;
    this.anomalyResource = anomalyResource;
    this.entityResource = entityResource;
    this.rcaResource = rcaResource;
    this.eventResource = eventResource;
  }

  @Path("auth")
  public AuthResource getAuthResource() {
    return authResource;
  }

  @Path("applications")
  public ApplicationResource getApplicationResource() {
    return applicationResource;
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
}
