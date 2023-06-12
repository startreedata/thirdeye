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
import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "zzz Maintenance zzz")
@SecurityRequirement(name="oauth")
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

  private static final Logger log = LoggerFactory.getLogger(EnumerationItemResource.class);

  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;
  private final AuthorizationManager authorizationManager;
  private final EnumerationItemMaintainer enumerationItemMaintainer;

  @Inject
  public MaintenanceResource(final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager,
      final EnumerationItemMaintainer enumerationItemMaintainer) {
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.authorizationManager = authorizationManager;
    this.enumerationItemMaintainer = enumerationItemMaintainer;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("/enumeration-items/migrate")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update the ignored index based on anomaly labels for historical anomalies")
  public Response migrateEnumerationItems(
      @Parameter(hidden = true) @Auth final ThirdEyePrincipal principal,
      @FormParam("from") final long fromId,
      @FormParam("to") final long toId

  ) {
    final EnumerationItemDTO from = enumerationItemManager.findById(fromId);
    authorizationManager.ensureCanDelete(principal, from);

    final EnumerationItemDTO to = enumerationItemManager.findById(toId);
    authorizationManager.ensureCanDelete(principal, to);

    enumerationItemMaintainer.migrateAndRemove(from, to);
    logDeleteOperation(from, principal, false);

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
    } catch (final Exception e) {
      eiString = ei.toString();
    }
    log.warn("Deleting{} by {}. enumeration item(id: {}}) json: {}",
        dryRun ? "(dryRun)" : "",
        principal.getName(),
        ei.getId(),
        eiString);
  }

  @POST
  @Path("/anomaly/index-ignored")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update the ignored index based on anomaly labels for historical anomalies")
  public Response updateIgnoreLabelIndex(
      @Parameter(hidden = true) @Auth final ThirdEyePrincipal principal
  ) {
    // skip already updated ignored index
    final DaoFilter filter = new DaoFilter().setPredicate(Predicate.NEQ("ignored", true));
    anomalyManager.filter(filter).stream()
        .peek(anomaly -> authorizationManager.ensureCanEdit(principal, anomaly, anomaly))
        .filter(this::isIgnored)
        .forEach(anomalyManager::update);
    return Response.ok().build();
  }

  private boolean isIgnored(final AnomalyDTO anomaly) {
    final List<AnomalyLabelDTO> labels = anomaly.getAnomalyLabels();
    return labels != null && labels.stream().anyMatch(AnomalyLabelDTO::isIgnore);
  }
}
