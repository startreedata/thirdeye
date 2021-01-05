package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.badRequest;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;

@Api(tags = "Metric")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MetricResource extends CrudResource<MetricApi, MetricConfigDTO> {

  @Inject
  public MetricResource(final AuthService authService, final MetricConfigManager metricConfigManager) {
    super(authService, metricConfigManager, ImmutableMap.of());
  }

  @Override
  protected MetricConfigDTO createDto(final ThirdEyePrincipal principal, final MetricApi api) {
    final MetricConfigDTO dto = ApiBeanMapper.toMetricConfigDto(api);
    dto.setAlias(ThirdEyeUtils.constructMetricAlias(api.getDataset().getName(), api.getName()));
    dto.setCreatedBy(principal.getName());
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

  @DELETE
  @Path("{id}")
  @Override
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    authService.authenticate(authHeader);
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }
}
