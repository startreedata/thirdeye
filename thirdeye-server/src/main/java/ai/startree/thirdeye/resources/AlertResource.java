/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.alert.AlertCreater;
import ai.startree.thirdeye.alert.AlertDeleter;
import ai.startree.thirdeye.alert.AlertEvaluator;
import ai.startree.thirdeye.mapper.AlertApiBeanMapper;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Alert", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource extends CrudResource<AlertApi, AlertDTO> {

  private static final Logger log = LoggerFactory.getLogger(AlertResource.class);

  private static final String CRON_EVERY_HOUR = "0 0 * * * ? *";

  private final AlertCreater alertCreater;
  private final AlertDeleter alertDeleter;
  private final AlertApiBeanMapper alertApiBeanMapper;
  private final AlertEvaluator alertEvaluator;

  @Inject
  public AlertResource(
      final AlertManager alertManager,
      final AlertCreater alertCreater,
      final AlertDeleter alertDeleter,
      final AlertApiBeanMapper alertApiBeanMapper,
      final AlertEvaluator alertEvaluator) {
    super(alertManager, ImmutableMap.of());
    this.alertCreater = alertCreater;
    this.alertDeleter = alertDeleter;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.alertEvaluator = alertEvaluator;
  }

  @Override
  protected void deleteDto(AlertDTO dto) {
    alertDeleter.delete(dto);
  }

  @Override
  protected AlertDTO createDto(final ThirdEyePrincipal principal, final AlertApi api) {
    if (api.getCron() == null) {
      api.setCron(CRON_EVERY_HOUR);
    }

    return alertCreater.create(api
        .setOwner(new UserApi().setPrincipal(principal.getName()))
    );
  }

  @Override
  protected AlertDTO toDto(final AlertApi api) {
    return alertApiBeanMapper.toAlertDTO(api);
  }

  @Override
  protected void validate(final AlertApi api, final AlertDTO existing) {
    super.validate(api, existing);
    ensureExists(api.getName(), "Name must be present");
    ensure(!StringUtils.containsWhitespace(api.getName()), "Name must not contain white space");
    optional(api.getCron()).ifPresent(cron ->
        ensure(CronExpression.isValidExpression(cron), ERR_CRON_INVALID, api.getCron()));
    if (existing == null) {
      alertCreater.ensureCreationIsPossible(api);
    }
  }

  @Override
  protected void prepareUpdatedDto(final ThirdEyePrincipal principal,
      final AlertDTO existing,
      final AlertDTO updated) {
    updated.setLastTimestamp(existing.getLastTimestamp());

    // Always set a default cron if not present.
    if (updated.getCron() == null) {
      updated.setCron(CRON_EVERY_HOUR);
    }
  }

  @Override
  protected AlertApi toApi(final AlertDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Path("{id}/run")
  @POST
  @Timed
  public Response runTask(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") final Long id,
      @FormParam("start") final Long startTime,
      @FormParam("end") final Long endTime
  ) {
    ensureExists(startTime, "start");

    final AlertDTO dto = get(id);
    alertCreater.createOnboardingTask(dto,
        startTime,
        optional(endTime).orElse(System.currentTimeMillis())
    );

    return Response.ok().build();
  }

  @POST
  @Timed
  @Path("/validate")
  @Produces(MediaType.APPLICATION_JSON)
  // can be moved to CrudResource if /validate is needed for other entities.
  public Response validateMultiple(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      List<AlertApi> list) {
    ensureExists(list, "Invalid request");

    for (AlertApi api : list) {
      AlertDTO existing = api.getId() == null ? null : ensureExists(dtoManager.findById(api.getId()));
      validate(api, existing);
    }

    return Response.ok().build();
  }


  @Path("evaluate")
  @POST
  @Timed
  public Response evaluate(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      final AlertEvaluationApi request
  ) throws ExecutionException {
    ensureExists(request.getStart(), "start");
    ensureExists(request.getEnd(), "end");

    final AlertApi alert = request.getAlert();
    ensureExists(alert)
        .setOwner(new UserApi()
            .setPrincipal(principal.getName()));

    return Response.ok(alertEvaluator.evaluate(request)).build();
  }

  @ApiOperation(value = "Delete associated Anomalies")
  @DELETE
  @Path("{id}/reset")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response reset(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") final Long id) {
    final AlertDTO dto = get(id);
    alertDeleter.deleteAssociatedAnomalies(dto.getId());
    log.warn(String.format("Resetting alert id: %d by principal: %s", id, principal.getName()));

    return respondOk(toApi(dto));
  }
}
