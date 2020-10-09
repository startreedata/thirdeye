package org.apache.pinot.thirdeye.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;

public class ApiResource {

  private final AuthResource authResource;
  private final ApplicationResource applicationResource;
  private final DatasetResource datasetResource;

  @Inject
  public ApiResource(final AuthResource authResource,
      final ApplicationResource applicationResource,
      final DatasetResource datasetResource) {
    this.authResource = authResource;
    this.applicationResource = applicationResource;
    this.datasetResource = datasetResource;
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
}
