package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.badRequest;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;

@Api(tags = "Dataset")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DatasetResource extends CrudResource<DatasetApi, DatasetConfigDTO> {

  @Inject
  public DatasetResource(
      final AuthService authService,
      final DatasetConfigManager datasetConfigManager) {
    super(authService, datasetConfigManager, ImmutableMap.of());
  }

  @Override
  protected DatasetConfigDTO createDto(final ThirdEyePrincipal principal,
      final DatasetApi api) {
    final DatasetConfigDTO dto = ApiBeanMapper.toDatasetConfigDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected DatasetConfigDTO updateDto(final ThirdEyePrincipal principal,
      final DatasetApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }

  @Override
  protected DatasetApi toApi(final DatasetConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
