package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureNull;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.quartz.CronExpression;

@Api(tags = "Subscription Group", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class SubscriptionGroupResource extends
    CrudResource<SubscriptionGroupApi, SubscriptionGroupDTO> {

  private static final String CRON_EVERY_5MIN = "0 */5 * * * ?";
  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public SubscriptionGroupResource(
      final SubscriptionGroupManager subscriptionGroupManager) {
    super(subscriptionGroupManager, ImmutableMap.of());
    this.subscriptionGroupManager = subscriptionGroupManager;
  }

  @Override
  protected SubscriptionGroupDTO createDto(final ThirdEyePrincipal principal,
      final SubscriptionGroupApi api) {
    ensureNull(api.getId(), ERR_ID_UNEXPECTED_AT_CREATION);
    if (Strings.isNullOrEmpty(api.getCron())) {
      api.setCron(CRON_EVERY_5MIN);
    }
    return toDto(api);
  }

  @Override
  protected void validate(final SubscriptionGroupApi api, final SubscriptionGroupDTO existing) {
    super.validate(api, existing);
    String cron = api.getCron();
    ensure(Strings.isNullOrEmpty(cron) || CronExpression.isValidExpression(cron),
        ERR_CRON_INVALID,
        cron);

    // For new Subscription Group or existing Subscription Group with different name
    if (existing == null || !existing.getName().equals(api.getName())) {
      ensure(subscriptionGroupManager.findByPredicate(
          Predicate.EQ("name", api.getName())).size() == 0,
          ERR_DUPLICATE_NAME);
    }
  }

  @Override
  protected void prepareUpdatedDto(final ThirdEyePrincipal principal,
      final SubscriptionGroupDTO existing,
      final SubscriptionGroupDTO updated) {
    // Always set a default cron if not present.
    if (updated.getCronExpression() == null) {
      updated.setCronExpression(CRON_EVERY_5MIN);
    }
  }

  @Override
  protected SubscriptionGroupDTO toDto(final SubscriptionGroupApi api) {
    return ApiBeanMapper.toSubscriptionGroupDTO(api);
  }

  @Override
  protected SubscriptionGroupApi toApi(final SubscriptionGroupDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @POST
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}/reset")
  public Response reset(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") Long id) {
    final SubscriptionGroupDTO sg = get(id);
    sg.setVectorClocks(null);
    subscriptionGroupManager.save(sg);

    return Response.ok(toApi(sg)).build();
  }
}
