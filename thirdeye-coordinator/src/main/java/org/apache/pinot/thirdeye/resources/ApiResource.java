package org.apache.pinot.thirdeye.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;

public class ApiResource {

  private final AuthResource authResource;
  private final ApplicationResource applicationResource;
  private final DatasetResource datasetResource;
  private final MetricResource metricResource;
  private final AlertResource alertResource;

  @Inject
  public ApiResource(final AuthResource authResource,
      final ApplicationResource applicationResource,
      final DatasetResource datasetResource,
      final MetricResource metricResource,
      final AlertResource alertResource) {
    this.authResource = authResource;
    this.applicationResource = applicationResource;
    this.datasetResource = datasetResource;
    this.metricResource = metricResource;
    this.alertResource = alertResource;
  }

  @Path("auth")
  public AuthResource getAuthResource() {
    return authResource;
  }

  @Path("applications")
  public ApplicationResource getApplicationResource() {
    return applicationResource;
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
  public AlertResource getRuleResource() {
    return alertResource;
  }
}
