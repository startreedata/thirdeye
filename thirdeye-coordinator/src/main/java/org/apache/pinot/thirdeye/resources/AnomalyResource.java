package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;
import static org.apache.pinot.thirdeye.util.ResourceUtils.badRequest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.AnomalyApi;
import org.apache.pinot.thirdeye.spi.api.AnomalyFeedbackApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;

@Api(tags = "Anomaly")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource extends CrudResource<AnomalyApi, MergedAnomalyResultDTO> {

  public static final ImmutableMap<String, String> API_TO_BEAN_MAP = ImmutableMap.<String, String>builder()
      .put("alert.id", "detectionConfigId")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .build();
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  public AnomalyResource(
      final AuthService authService,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(authService, mergedAnomalyResultManager, API_TO_BEAN_MAP);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
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
    return ApiBeanMapper.toApi(dto);
  }

  @Path("{id}/feedback")
  @POST
  @Timed
  public Response setFeedback(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id,
      AnomalyFeedbackApi api) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
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
