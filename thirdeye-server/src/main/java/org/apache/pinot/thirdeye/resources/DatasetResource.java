package org.apache.pinot.thirdeye.resources;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;

@Api(tags = "Dataset")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DatasetResource extends CrudResource<DatasetApi, DatasetConfigDTO> {

  @Inject
  public DatasetResource(
      final DatasetConfigManager datasetConfigManager) {
    super(datasetConfigManager, ImmutableMap.of());
  }

  @Override
  protected DatasetConfigDTO createDto(final ThirdEyePrincipal principal,
      final DatasetApi api) {
    final DatasetConfigDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected DatasetConfigDTO toDto(final DatasetApi api) {
    return ApiBeanMapper.toDatasetConfigDto(api);
  }

  @Override
  protected DatasetApi toApi(final DatasetConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
