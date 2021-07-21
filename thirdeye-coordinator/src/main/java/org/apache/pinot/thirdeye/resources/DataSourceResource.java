package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ResourceUtils.respondOk;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.stream.Collectors;
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
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.DataSourceApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DataSourceManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;

@Api(tags = "Data Source")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DataSourceResource extends CrudResource<DataSourceApi, DataSourceDTO> {

  private final DataSourceCache dataSourceCache;

  @Inject
  public DataSourceResource(
      final AuthService authService,
      final DataSourceManager dataSourceManager,
      final DataSourceCache dataSourceCache) {
    super(authService, dataSourceManager, ImmutableMap.of());
    this.dataSourceCache = dataSourceCache;
  }

  @Override
  protected DataSourceDTO createDto(final ThirdEyePrincipal principal,
      final DataSourceApi api) {
    final DataSourceDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected DataSourceDTO toDto(final DataSourceApi api) {
    return ApiBeanMapper.toDataSourceDto(api);
  }

  @Override
  protected DataSourceApi toApi(final DataSourceDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @POST
  @Path("onboard-dataset")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardDataset(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @FormParam("dataSourceName") String dataSourceName,
      @FormParam("datasetName") String datasetName) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(dataSourceName, "dataSourceName is a required field");
    ensureExists(datasetName, "datasetName is a required field");

    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
    final DatasetConfigDTO datasetConfigDTO = dataSource.onboardDataset(datasetName);

    return respondOk(ApiBeanMapper.toApi(datasetConfigDTO));
  }

  @POST
  @Path("onboard-all")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @FormParam("name") String name) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(name, "name is a required field");

    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(name);
    final List<DatasetConfigDTO> datasets = dataSource.onboardAll();

    return respondOk(datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList()));
  }
}
