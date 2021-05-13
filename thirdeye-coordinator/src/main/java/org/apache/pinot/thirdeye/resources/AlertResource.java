package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_CRON_INVALID;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_MISSING_ID;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.respondOk;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.statusResponse;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.alert.AlertApiBeanMapper;
import org.apache.pinot.thirdeye.alert.AlertCreater;
import org.apache.pinot.thirdeye.alert.AlertDeleter;
import org.apache.pinot.thirdeye.alert.AlertEvaluator;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.api.AlertNodeApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.api.UserApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Alert")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource extends CrudResource<AlertApi, AlertDTO> {

  private static final Logger log = LoggerFactory.getLogger(AlertResource.class);

  public static final String DEFAULT_CRON = "0 */1 * * * ?";

  private final AlertManager alertManager;
  private final MetricConfigManager metricConfigManager;
  private final AlertCreater alertCreater;
  private final AlertDeleter alertDeleter;
  private final AlertApiBeanMapper alertApiBeanMapper;
  private final AuthService authService;
  private final AlertEvaluator alertEvaluator;

  @Inject
  public AlertResource(
      final AlertManager alertManager,
      final MetricConfigManager metricConfigManager,
      final AlertCreater alertCreater,
      final AlertDeleter alertDeleter,
      final AlertApiBeanMapper alertApiBeanMapper,
      final AuthService authService,
      final AlertEvaluator alertEvaluator) {
    super(authService, alertManager, ImmutableMap.of());
    this.alertManager = alertManager;
    this.metricConfigManager = metricConfigManager;
    this.alertCreater = alertCreater;
    this.alertDeleter = alertDeleter;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.authService = authService;
    this.alertEvaluator = alertEvaluator;
  }

  @Override
  protected AlertDTO createDto(final ThirdEyePrincipal principal, final AlertApi api) {
    ensureExists(api.getName(), "Name must be present");
    ensureExists(api.getNodes(), "Exactly 1 detection must be present");

    if (api.getCron() == null) {
      api.setCron(DEFAULT_CRON);
    }
    ensure(CronExpression.isValidExpression(api.getCron()), ERR_CRON_INVALID, api.getCron());

    return alertCreater.create(api
        .setOwner(new UserApi().setPrincipal(principal.getName()))
    );
  }

  @Override
  protected AlertDTO updateDto(final ThirdEyePrincipal principal, final AlertApi api) {
    final Long id = ensureExists(api.getId(), ERR_MISSING_ID);
    final AlertDTO existing = ensureExists(alertManager.findById(id));

    final AlertDTO updated = alertApiBeanMapper.toAlertDTO(api);
    updated.setId(id);
    updated.setCreatedBy(existing.getCreatedBy());
    updated.setLastTimestamp(existing.getLastTimestamp());

    optional(api.getCron())
        .ifPresent(cron -> {
          ensure(CronExpression.isValidExpression(cron), ERR_CRON_INVALID, cron);
          updated.setCron(cron);
        });

    alertManager.update(updated);
    return updated;
  }

  @Override
  protected AlertApi toApi(final AlertDTO dto) {
    final AlertApi api = ApiBeanMapper.toApi(dto);

    // Add metric and dataset info
    optional(api.getNodes())
        .map(Map::values)
        .ifPresent(nodes -> nodes.stream()
            .map(AlertNodeApi::getMetric)
            .forEach(metricApi -> optional(metricApi)
                .map(MetricApi::getId)
                .map(metricConfigManager::findById)
                .ifPresent(metricDto -> metricApi
                    .setName(metricDto.getName())
                    .setDataset(new DatasetApi()
                        .setName(metricDto.getDataset())))));

    return api;
  }

  @Path("{id}/run")
  @POST
  @Timed
  public Response runTask(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id,
      @FormParam("start") Long startTime,
      @FormParam("end") Long endTime
  ) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(startTime, "start");

    final AlertDTO dto = get(id);
    alertCreater.createOnboardingTask(dto,
        startTime,
        optional(endTime).orElse(System.currentTimeMillis())
    );

    return Response.ok().build();
  }

  @Path("evaluate")
  @POST
  @Timed
  public Response evaluate(
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authHeader,
      final AlertEvaluationApi request
  ) throws ExecutionException {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(request.getStart(), "start");
    ensureExists(request.getEnd(), "end");

    ensureExists(request.getAlert())
        .setOwner(new UserApi()
            .setPrincipal(principal.getName()));

    final AlertEvaluationApi evaluation = alertEvaluator.evaluate(request);
    return Response
        .ok(evaluation)
        .build();
  }

  @DELETE
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @Override
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final AlertDTO dto = alertManager.findById(id);
    if (dto != null) {
      alertDeleter.delete(dto);
      log.warn(String.format("Deleted id: %d by principal: %s", id, principal));

      return respondOk(toApi(dto));
    }

    return respondOk(statusResponse(ERR_OBJECT_DOES_NOT_EXIST, id));
  }
}
