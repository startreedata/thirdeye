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

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.Objects;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "zzz Maintenance zzz",
    authorizations = {
        @Authorization(value = "oauth")
    })
@SwaggerDefinition(securityDefinition = @SecurityDefinition(
    apiKeyAuthDefinitions = @ApiKeyAuthDefinition(
        name = HttpHeaders.AUTHORIZATION,
        in = ApiKeyLocation.HEADER,
        key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

  private static final Logger log = LoggerFactory.getLogger(EnumerationItemResource.class);

  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AuthorizationManager authorizationManager;

  @Inject
  public MaintenanceResource(final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final AuthorizationManager authorizationManager) {
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.authorizationManager = authorizationManager;
  }

  @DELETE
  @Path("/enumeration-items/purge")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Remove enumeration items that are not associated with any alert and have no anomalies")
  public Response purge(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(defaultValue = "true") @FormParam("dryRun") boolean dryRun) {
    enumerationItemManager.findAll().stream()
        .peek(ei -> authorizationManager.ensureCanDelete(principal, ei))
        .filter(ei -> ei.getAlert() == null)
        .filter(this::shouldPurge)
        .peek(ei -> logDeleteOperation(ei, principal, dryRun))
        .filter(ei -> !dryRun)
        .forEach(enumerationItemManager::delete);
    return Response.ok().build();
  }

  private void logDeleteOperation(final EnumerationItemDTO ei,
      final ThirdEyePrincipal principal,
      final boolean dryRun) {
    String eiString;
    try {
      eiString = ThirdEyeSerialization
          .getObjectMapper()
          .writeValueAsString(ApiBeanMapper.toApi(ei));
    } catch (Exception e) {
      eiString = ei.toString();
    }
    log.warn("Deleting{} by {}. enumeration item(id: {}}) json: {}",
        dryRun ? "(dryRun)" : "",
        principal.getName(),
        ei.getId(),
        eiString);
  }

  private boolean shouldPurge(final EnumerationItemDTO ei) {
    final AnomalyFilter f = new AnomalyFilter().setEnumerationItemId(ei.getId());
    final long anomalyCount = anomalyManager.filter(f).size();
    if (anomalyCount > 0) {
      return false;
    }
    final long subscriptionGroupCount = subscriptionGroupManager.findAll().stream()
        .filter(Objects::nonNull)
        .filter(sg -> sg.getAlertAssociations() != null)
        .filter(sg -> sg.getAlertAssociations().stream()
            .filter(Objects::nonNull)
            .filter(aa -> aa.getEnumerationItem() != null)
            .anyMatch(aa -> ei.getId().equals(aa.getEnumerationItem().getId()))
        )
        .count();
    return subscriptionGroupCount == 0;
  }
}
