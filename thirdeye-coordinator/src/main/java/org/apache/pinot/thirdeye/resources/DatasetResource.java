package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.respondOk;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.util.ApiBeanMapper;

@Api(tags = "Dataset")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DatasetResource extends CrudResource<DatasetApi, DatasetConfigDTO> {

  private final DataSourceCache dataSourceCache;

  @Inject
  public DatasetResource(
      final AuthService authService,
      final DatasetConfigManager datasetConfigManager,
      final DataSourceCache dataSourceCache) {
    super(authService, datasetConfigManager, ImmutableMap.of());
    this.dataSourceCache = dataSourceCache;
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

  @POST
  @Path("onboard")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardDataset(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @FormParam("dataSourceName") String dataSourceName,
      @FormParam("datasetName") String datasetName
  ) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(dataSourceName, "dataSourceName is a required field");
    ensureExists(datasetName, "datasetName is a required field");

    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
    final DatasetConfigDTO datasetConfigDTO = dataSource.onboardDataset(datasetName);

    return respondOk(toApi(datasetConfigDTO));
  }
}
