package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureNull;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static org.apache.pinot.thirdeye.spi.datalayer.util.ThirdEyeSpiUtils.optional;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.util.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;

@Api(tags = "Metric")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MetricResource extends CrudResource<MetricApi, MetricConfigDTO> {

  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public MetricResource(final AuthService authService, final MetricConfigManager metricConfigManager, final DatasetConfigManager datasetConfigManager) {
    super(authService, metricConfigManager, ImmutableMap.of());
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  @Override
  protected MetricConfigDTO createDto(final ThirdEyePrincipal principal, final MetricApi api) {
    ensureExists(api.getDataset(), "dataset");
    ensureExists(this.datasetConfigManager.findByDataset(api.getDataset().getName()));
    ensureNull(api.getId(), ERR_ID_UNEXPECTED_AT_CREATION);
    ensure(this.metricConfigManager.findByMetricName(api.getName()).isEmpty(), ERR_DUPLICATE_NAME);

    final MetricConfigDTO dto = ApiBeanMapper.toMetricConfigDto(api);
    dto.setAlias(SpiUtils.constructMetricAlias(api.getDataset().getName(), api.getName()));
    dto.setCreatedBy(principal.getName());
    dto.setDatasetConfig(ApiBeanMapper.toDatasetConfigDto(api.getDataset()));

    return dto;
  }

  @Override
  protected MetricConfigDTO updateDto(final ThirdEyePrincipal principal, final MetricApi api) {
    final Long id = api.getId();
    final MetricConfigDTO dto = get(id);
    optional(api.getName()).ifPresent(dto::setName);
    optional(api.getDerivedMetricExpression()).ifPresent(dto::setDerivedMetricExpression);
    optional(api.getRollupThreshold()).ifPresent(dto::setRollupThreshold);
    optional(api.getActive()).ifPresent(dto::setActive);
    optional(api.getViews()).ifPresent(dto::setViews);
    optional(api.getWhere()).ifPresent(dto::setWhere);
    optional(api.getAggregationFunction()).ifPresent(dto::setDefaultAggFunction);
    return dto;
  }

  @Override
  protected MetricApi toApi(final MetricConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
