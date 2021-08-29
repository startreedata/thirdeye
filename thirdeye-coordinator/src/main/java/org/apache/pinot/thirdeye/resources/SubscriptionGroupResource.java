package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureNull;

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
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;
import org.apache.pinot.thirdeye.spi.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.quartz.CronExpression;

@Api(tags = "Subscription Group")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class SubscriptionGroupResource extends
    CrudResource<SubscriptionGroupApi, SubscriptionGroupDTO> {

  private static final String CRON_EVERY_5MIN = "0 */5 * * * ?";
  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public SubscriptionGroupResource(
      final SubscriptionGroupManager subscriptionGroupManager,
      final AuthService authService) {
    super(authService, subscriptionGroupManager, ImmutableMap.of());
    this.subscriptionGroupManager = subscriptionGroupManager;
  }

  @Override
  protected SubscriptionGroupDTO createDto(final ThirdEyePrincipal principal,
      final SubscriptionGroupApi api) {
    ensureNull(api.getId(), ERR_ID_UNEXPECTED_AT_CREATION);
    if (api.getCron() == null) {
      api.setCron(CRON_EVERY_5MIN);
    }
    SubscriptionGroupDTO dto = toDto(api);
    if(dto.getNotificationSchemes().getWebhookScheme() != null) {
      dto.getNotificationSchemes().getWebhookScheme().generateSecret();
    }
    return dto;
  }

  @Override
  protected void validate(final SubscriptionGroupApi api, final SubscriptionGroupDTO existing) {
    super.validate(api, existing);
    optional(api.getCron()).ifPresent(cron ->
        ensure(CronExpression.isValidExpression(cron), ERR_CRON_INVALID, api.getCron()));

    // For new Subscription Group or existing Subscription Group with different name
    if (existing == null || !existing.getName().equals(api.getName())) {
      ensure(subscriptionGroupManager.findByPredicate(
          Predicate.EQ("name", api.getName())).size() == 0,
          ERR_DUPLICATE_NAME);
    }

    if(api.getNotificationSchemes() != null && api.getNotificationSchemes().getWebhook() !=null){
      ensureNull(api.getNotificationSchemes().getWebhook().getHashKey(), ThirdEyeStatus.ERR_OBJECT_UNEXPECTED, "secret");
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
    final WebhookSchemeDto existingWebhook = existing.getNotificationSchemes().getWebhookScheme();
    final WebhookSchemeDto updatedWebhook = updated.getNotificationSchemes().getWebhookScheme();
    if(existingWebhook == null ) {
      // new webhook notification creation
      if(updatedWebhook != null) {
        updatedWebhook.generateSecret();
      }
    } else {
      // update existing webhook notification
      // webhook secret propagated to updated webhook notification
      if(updatedWebhook != null) {
        existingWebhook.setUrl(updatedWebhook.getUrl());
        updated.getNotificationSchemes().setWebhookScheme(existingWebhook);
      }
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
  @Path("webhook/refresh-secret")
  public Response refreshWebhookSecret(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @FormParam("id") Long id ) {
    authService.authenticate(authHeader);
    ensureExists(id, "Invalid id!");
    SubscriptionGroupDTO dto = get(id);
    ensureExists(dto.getNotificationSchemes().getWebhookScheme(), "webhook");
    dto.getNotificationSchemes().getWebhookScheme().generateSecret();
    dtoManager.save(dto);
    return Response.ok(toApi(dto).getNotificationSchemes().getWebhook()).build();
  }
}
