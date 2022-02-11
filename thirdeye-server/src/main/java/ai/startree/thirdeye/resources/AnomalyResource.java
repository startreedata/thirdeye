/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.badRequest;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Anomaly", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource extends CrudResource<AnomalyApi, MergedAnomalyResultDTO> {

  public static final ImmutableMap<String, String> API_TO_BEAN_MAP = ImmutableMap.<String, String>builder()
      .put("alert.id", "detectionConfigId")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .put("isChild", "child")
      .build();
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;

  @Inject
  public AnomalyResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager) {
    super(mergedAnomalyResultManager, API_TO_BEAN_MAP);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
  }

  private static AnomalyFeedbackDTO toAnomalyFeedbackDTO(AnomalyFeedbackApi api) {
    final AnomalyFeedbackDTO dto = new AnomalyFeedbackDTO();
    dto.setComment(api.getComment());
    dto.setFeedbackType(api.getType());

    return dto;
  }

  @Override
  protected MergedAnomalyResultDTO createDto(final ThirdEyePrincipal principal,
      final AnomalyApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }

  @Override
  protected MergedAnomalyResultDTO toDto(final AnomalyApi api) {
    // For now, anomalies are to be created/edited by the system.
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }

  @Override
  protected AnomalyApi toApi(final MergedAnomalyResultDTO dto) {
    final AnomalyApi anomalyApi = ApiBeanMapper.toApi(dto);
    optional(anomalyApi.getAlert())
        .filter(alertApi -> alertApi.getId() != null)
        .ifPresent(this::populateNameIfPossible);
    return anomalyApi;
  }

  private void populateNameIfPossible(final AlertApi alertApi) {
    optional(alertManager.findById(alertApi.getId()))
        .ifPresent(alert -> alertApi.setName(alert.getName()));
  }

  @Path("{id}/feedback")
  @POST
  @Timed
  public Response setFeedback(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") Long id,
      AnomalyFeedbackApi api) {
    final MergedAnomalyResultDTO dto = get(id);

    final AnomalyFeedbackDTO feedbackDTO = toAnomalyFeedbackDTO(api);
    dto.setFeedback(feedbackDTO);
    mergedAnomalyResultManager.updateAnomalyFeedback(dto);

    if (dto.isChild()) {
      optional(mergedAnomalyResultManager.findParent(dto))
          .ifPresent(p -> {
            p.setFeedback(feedbackDTO);
            mergedAnomalyResultManager.updateAnomalyFeedback(p);
          });
    }

    return Response
        .ok()
        .build();
  }
}
