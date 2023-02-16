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

import static ai.startree.thirdeye.datalayer.bao.EnumerationItemManagerImpl.eiRef;
import static ai.startree.thirdeye.datalayer.bao.EnumerationItemManagerImpl.toAlertDTO;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
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
  private final AlertManager alertManager;

  @Inject
  public MaintenanceResource(final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final AuthorizationManager authorizationManager,
      final AlertManager alertManager) {
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.authorizationManager = authorizationManager;
    this.alertManager = alertManager;
  }

  private static AbstractDTO ei(final EnumerationItemDTO existingOrCreated) {
    return new EnumerationItemDTO().setId(existingOrCreated.getId());
  }

  @DELETE
  @Path("/enumeration-items/purge")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Remove enumeration items that are not associated with any alert and have no anomalies")
  public Response purge(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(defaultValue = "true") @FormParam("dryRun") final boolean dryRun) {
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
    } catch (final Exception e) {
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

  @DELETE
  @Path("/enumeration-items/remove-alerts")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Remove deprecated alerts field from enumeration items")
  public Response removeDeprecatedAlerts(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal) {
    enumerationItemManager.findAll().stream()
        .peek(ei -> authorizationManager.ensureCanEdit(principal, ei, ei))
        .map(ei -> ei.setAlerts(null))
        .forEach(enumerationItemManager::update);
    return Response.ok().build();
  }

  @POST
  @Path("/enumeration-items/fix-incorrect-migrations")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Go through all anomalies and fix incorrect migrations")
  public Response fixIncorrectMigrations(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(defaultValue = "true") @FormParam("dryRun") final boolean dryRun) {

    final List<EnumerationItemDTO> allEis = enumerationItemManager.findAll();

    final Map<Long, EnumerationItemDTO> idToEi = allEis.stream()
        .collect(Collectors.toMap(AbstractDTO::getId, ei -> ei));

    final Set<Long> alertIds = allEis.stream()
        .filter(ei -> ei.getAlert() != null)
        .map(EnumerationItemDTO::getAlert)
        .map(AbstractDTO::getId)
        .collect(Collectors.toSet());

    final Map<EiKey, EnumerationItemDTO> eiMap = new HashMap<>();
    int count = 0;
    for (final Long alertId : alertIds) {
      log.info("Processing alert(id: {}) {}/{}", alertId, count, alertIds.size());
      final AnomalyFilter f = new AnomalyFilter().setAlertId(alertId);
      final List<AnomalyDTO> anomalies = anomalyManager.filter(f);
      for (final AnomalyDTO anomaly : anomalies) {
        if (anomaly.getEnumerationItem() == null) {
          continue;
        }
        final EnumerationItemDTO ei = idToEi.get(anomaly.getEnumerationItem().getId());
        if (ei == null) {
          log.error("Anomaly(id: {}) has an enumeration item(id: {}) that does not exist",
              anomaly.getId(),
              anomaly.getEnumerationItem().getId());
          continue;
        }
        if (ei.getAlert() == null) {
          log.error("Enumeration item(id: {}) has no alert", ei.getId());
          continue;
        }
        if (!alertId.equals(ei.getAlert().getId())) {
          log.error(
              "Enumeration item(id: {}) has an alert(id: {}) that does not match anomaly(id: {})'s alert(id: {})",
              ei.getId(),
              ei.getAlert().getId(),
              anomaly.getId(),
              alertId);
          // This is the problem we are trying to fix

          final EnumerationItemDTO existingOrCreated = getExistingOrCreate(alertId, ei, eiMap);
          log.info("Moving anomaly {} to {} enumeration item(id: {}) from (id: {})",
              anomaly.getId(),
              idToEi.containsKey(existingOrCreated.getId()) ? "existing" : "new",
              existingOrCreated.getId(),
              ei.getId());

          if (!dryRun) {
            anomalyManager.update(
                anomaly.setEnumerationItem(eiRef(existingOrCreated.getId()))
            );
          }
        }
      }
      count++;
    }

    return Response.ok(
        alertIds.stream().map(
            id -> new AlertApi().setId(id)
        ).collect(Collectors.toList())
    ).build();
  }

  private EnumerationItemDTO getExistingOrCreate(final Long alertId, final EnumerationItemDTO ei,
      final Map<EiKey, EnumerationItemDTO> eiMap) {
    final EiKey key = new EiKey(alertId, ei.getName(), ei.getParams());
    if (eiMap.containsKey(key)) {
      return eiMap.get(key);
    }
    final EnumerationItemDTO existingOrCreated = enumerationItemManager.findExistingOrCreate(
        new EnumerationItemDTO()
            .setAlert(toAlertDTO(alertId))
            .setName(ei.getName())
            .setParams(ei.getParams())
    );
    eiMap.put(key, existingOrCreated);
    return existingOrCreated;
  }

  public static class EiKey {

    private final Long alertId;
    private final String name;
    private final Map<String, Object> params;

    public EiKey(final Long alertId, final String name, final Map<String, Object> params) {
      this.alertId = alertId;
      this.name = name;
      this.params = params;
    }

    public Long getAlertId() {
      return alertId;
    }

    public String getName() {
      return name;
    }

    public Map<String, Object> getParams() {
      return params;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final EiKey eiKey = (EiKey) o;
      return Objects.equals(alertId, eiKey.alertId) &&
          Objects.equals(name, eiKey.name) &&
          Objects.equals(params, eiKey.params);
    }

    @Override
    public int hashCode() {
      return Objects.hash(alertId, name, params);
    }
  }
}
