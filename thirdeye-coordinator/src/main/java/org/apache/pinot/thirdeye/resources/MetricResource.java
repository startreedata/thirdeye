package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;

@Api(tags = "Metric")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MetricResource extends CrudResource<MetricApi, MetricConfigDTO> {

  private final DatasetConfigManager datasetConfigManager;

  @Inject
  public MetricResource(final AuthService authService, final MetricConfigManager metricConfigManager, final DatasetConfigManager datasetConfigManager) {
    super(authService, metricConfigManager, ImmutableMap.of());
    this.datasetConfigManager = datasetConfigManager;
  }

  @Override
  protected MetricConfigDTO createDto(final ThirdEyePrincipal principal, final MetricApi api) {
    ensureExists(this.datasetConfigManager.findByDataset(api.getDataset().getName()));
    final MetricConfigDTO dto = ApiBeanMapper.toMetricConfigDto(api);
    dto.setAlias(ThirdEyeUtils.constructMetricAlias(api.getDataset().getName(), api.getName()));
    dto.setCreatedBy(principal.getName());

    DatasetConfigDTO datasetConfigDTO =  ApiBeanMapper.toDatasetConfigDto(api.getDataset());

    dto.setDatasetConfig(datasetConfigDTO);
    dto.setViews(api.getViews());
    return dto;
  }

  @Override
  protected MetricConfigDTO updateDto(final ThirdEyePrincipal principal, final MetricApi api) {
    final Long id = api.getId();
    final MetricConfigDTO dto = get(id);
    optional(api.getName()).ifPresent(dto::setName);    
    optional(api.getDerived()).ifPresent(dto::setDerived);
    optional(api.getDerivedMetricExpression()).ifPresent(dto::setDerivedMetricExpression);
    optional(api.getRollupThreshold()).ifPresent(dto::setRollupThreshold);
    optional(api.getActive()).ifPresent(dto::setActive);
    optional(api.getViews()).ifPresent(dto::setViews);
    return dto;
  }

  @Override
  protected MetricApi toApi(final MetricConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
