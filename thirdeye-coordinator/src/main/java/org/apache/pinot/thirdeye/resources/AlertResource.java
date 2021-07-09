package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ResourceUtils.respondOk;
import static org.apache.pinot.thirdeye.util.ResourceUtils.statusResponse;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import java.util.HashMap;
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
import org.apache.pinot.thirdeye.alert.AlertEvaluatorV2;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.AlertNodeApi;
import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.DetectionEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.api.UserApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.util.ApiBeanMapper;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Alert")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource extends CrudResource<AlertApi, AlertDTO> {

  private static final Logger log = LoggerFactory.getLogger(AlertResource.class);

  private static final String CRON_EVERY_1MIN = "0 */1 * * * ?";

  private final AlertManager alertManager;
  private final MetricConfigManager metricConfigManager;
  private final AlertCreater alertCreater;
  private final AlertDeleter alertDeleter;
  private final AlertApiBeanMapper alertApiBeanMapper;
  private final AuthService authService;
  private final AlertEvaluator alertEvaluator;
  private final AlertEvaluatorV2 alertEvaluatorV2;

  @Inject
  public AlertResource(
      final AlertManager alertManager,
      final MetricConfigManager metricConfigManager,
      final AlertCreater alertCreater,
      final AlertDeleter alertDeleter,
      final AlertApiBeanMapper alertApiBeanMapper,
      final AuthService authService,
      final AlertEvaluator alertEvaluator,
      final AlertEvaluatorV2 alertEvaluatorV2) {
    super(authService, alertManager, ImmutableMap.of());
    this.alertManager = alertManager;
    this.metricConfigManager = metricConfigManager;
    this.alertCreater = alertCreater;
    this.alertDeleter = alertDeleter;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.authService = authService;
    this.alertEvaluator = alertEvaluator;
    this.alertEvaluatorV2 = alertEvaluatorV2;
  }

  private static AlertEvaluationApi toV1Format(
      final Map<String, Map<String, DetectionEvaluationApi>> v2Result) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    for (final String key : v2Result.keySet()) {
      final Map<String, DetectionEvaluationApi> detectionEvaluationApiMap = v2Result.get(key);
      for (final String apiKey : detectionEvaluationApiMap.keySet()) {
        map.put(key + "_" + apiKey, detectionEvaluationApiMap.get(apiKey));
      }
    }
    return new AlertEvaluationApi().setDetectionEvaluations(map);
  }

  private static boolean isV2Evaluation(final AlertApi alert) {
    return optional(alert)
        .map(AlertApi::getTemplate)
        .map(AlertTemplateApi::getNodes)
        .isPresent();
  }

  @Override
  protected AlertDTO createDto(final ThirdEyePrincipal principal, final AlertApi api) {
    ensureExists(api.getName(), "Name must be present");

    if (api.getCron() == null) {
      api.setCron(CRON_EVERY_1MIN);
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
    optional(api.getCron()).ifPresent(cron ->
        ensure(CronExpression.isValidExpression(cron), ERR_CRON_INVALID, api.getCron()));
  }

  @Override
  protected void prepareUpdatedDto(final ThirdEyePrincipal principal,
      final AlertDTO existing,
      final AlertDTO updated) {
    updated.setLastTimestamp(existing.getLastTimestamp());

    // Always set a default cron if not present.
    if (updated.getCron() == null) {
      updated.setCron(CRON_EVERY_1MIN);
    }
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
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authHeader,
      @PathParam("id") final Long id,
      @FormParam("start") final Long startTime,
      @FormParam("end") final Long endTime
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
      @HeaderParam("UseV1Format") final boolean useV1Format,
      final AlertEvaluationApi request
  ) throws ExecutionException {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(request.getStart(), "start");
    ensureExists(request.getEnd(), "end");

    ensureExists(request.getAlert())
        .setOwner(new UserApi()
            .setPrincipal(principal.getName()));

    AlertEvaluationApi evaluation;
    if (isV2Evaluation(request.getAlert())) {
      evaluation = alertEvaluatorV2.evaluate(request);
      if (useV1Format) {
        evaluation = toV1Format(evaluation.getEvaluations());
      }
    } else {
      // v1 Evaluation. Will be deprecated in the future
      evaluation = alertEvaluator.evaluate(request);
    }
    return Response.ok(evaluation).build();
  }

  @DELETE
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @Override
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) final String authHeader,
      @PathParam("id") final Long id) {
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
