package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.badRequest;

import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.api.AnomalyApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;

@Api(tags = "Anomaly")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource extends CrudResource<AnomalyApi, MergedAnomalyResultDTO> {

  @Inject
  public AnomalyResource(
      final AuthService authService,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(authService, mergedAnomalyResultManager);
  }

  @Override
  protected MergedAnomalyResultDTO createDto(final ThirdEyePrincipal principal,
      final AnomalyApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }

  @Override
  protected MergedAnomalyResultDTO updateDto(final ThirdEyePrincipal principal,
      final AnomalyApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }

  @Override
  protected AnomalyApi toApi(final MergedAnomalyResultDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
