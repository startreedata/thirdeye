package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;

import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource extends CrudResource<ApplicationApi, ApplicationDTO> {

  private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

  @Inject
  public ApplicationResource(
      final ApplicationManager applicationManager,
      final AuthService authService) {
    super(authService, applicationManager, ImmutableMap.of());
  }

  @Override
  protected ApplicationDTO createDto(final ThirdEyePrincipal principal, final ApplicationApi api) {
    final ApplicationDTO dto = ApiBeanMapper.toApplicationDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected ApplicationDTO updateDto(final ThirdEyePrincipal principal, final ApplicationApi api) {
    final Long id = api.getId();
    final ApplicationDTO dto = get(id);

    optional(api.getName())
        .ifPresent(dto::setApplication);
    return dto;
  }

  @Override
  protected ApplicationApi toApi(final ApplicationDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
