package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureNull;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_CRON_INVALID;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.spi.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.spi.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.util.Predicate;
import org.apache.pinot.thirdeye.spi.util.ApiBeanMapper;
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
    return toDto(api);
  }

  @Override
  protected void validate(final SubscriptionGroupApi api) {
    optional(api.getCron()).ifPresent(cron ->
        ensure(CronExpression.isValidExpression(cron), ERR_CRON_INVALID, api.getCron()));
    ensure(subscriptionGroupManager.findByPredicate(
        Predicate.EQ("name", api.getName())).size() == 0,
        ERR_DUPLICATE_NAME);
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
}
