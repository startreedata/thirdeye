/*
 * Copyright 2022 StarTree Inc
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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(
        apiKeyAuthDefinitions = @ApiKeyAuthDefinition(
            name = HttpHeaders.AUTHORIZATION,
            in = ApiKeyLocation.HEADER,
            key = "oauth"
        )
    )
)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaResource {

  private static final Logger LOG = LoggerFactory.getLogger(RcaResource.class);

  private final RcaInvestigationResource rcaInvestigationResource;
  private final RcaMetricResource rcaMetricResource;
  private final RcaDimensionAnalysisResource rcaDimensionAnalysisResource;
  private final RcaRelatedResource rcaRelatedResource;

  @Inject
  public RcaResource(
      final RcaInvestigationResource rcaInvestigationResource,
      final RcaMetricResource rcaMetricResource,
      final RcaDimensionAnalysisResource rcaDimensionAnalysisResource,
      final RcaRelatedResource rcaRelatedResource) {
    this.rcaInvestigationResource = rcaInvestigationResource;
    this.rcaMetricResource = rcaMetricResource;
    this.rcaDimensionAnalysisResource = rcaDimensionAnalysisResource;
    this.rcaRelatedResource = rcaRelatedResource;
  }

  @Path(value = "/dim-analysis")
  public RcaDimensionAnalysisResource getDimensionAnalysisResource() {
    return rcaDimensionAnalysisResource;
  }

  @Path(value = "/investigations")
  public RcaInvestigationResource getRcaInvestigationResource() {
    return rcaInvestigationResource;
  }

  @Path(value = "/metrics")
  public RcaMetricResource getRcaMetricResource() {
    return rcaMetricResource;
  }

  @Path(value = "/related")
  public RcaRelatedResource getRcaRelatedResource() {
    return rcaRelatedResource;
  }
}
