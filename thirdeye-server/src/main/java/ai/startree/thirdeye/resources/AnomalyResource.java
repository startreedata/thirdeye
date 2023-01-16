/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.RequestCache.buildCache;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.RequestCache;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.core.AppAnalyticsService;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Anomaly", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource extends CrudResource<AnomalyApi, MergedAnomalyResultDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("alert.id", "detectionConfigId")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .put("isChild", "child")
      .put("metadata.metric.name", "metric")
      .put("metadata.dataset.name", "collection")
      .put("enumerationItem.id", "enumerationItemId")
      .put("feedback.id", "anomalyFeedbackId")
      .build();
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;
  private final AppAnalyticsService analyticsService;

  @Inject
  public AnomalyResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager,
      final AppAnalyticsService analyticsService,
      final AuthorizationManager authorizationManager) {
    super(mergedAnomalyResultManager, API_TO_INDEX_FILTER_MAP, authorizationManager);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
    this.analyticsService = analyticsService;
  }

  @Override
  protected RequestCache createRequestCache() {
    return super.createRequestCache()
        .setAlerts(buildCache(alertManager::findById));
  }

  @Override
  protected MergedAnomalyResultDTO createDto(final ThirdEyePrincipal principal,
      final AnomalyApi api) {
    return toDto(api);
  }

  @Override
  protected MergedAnomalyResultDTO toDto(final AnomalyApi api) {
    return ApiBeanMapper.toDto(api);
  }

  @Override
  protected AnomalyApi toApi(final MergedAnomalyResultDTO dto, RequestCache cache) {
    final AnomalyApi anomalyApi = ApiBeanMapper.toApi(dto);
    optional(anomalyApi.getAlert())
        .filter(alertApi -> alertApi.getId() != null)
        .ifPresent(alertApi -> alertApi.setName(cache.getAlerts()
            .getUnchecked(alertApi.getId())
            .getName()));
    return anomalyApi;
  }

  @Path("{id}/feedback")
  @POST
  @Timed
  public Response setFeedback(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") Long id,
      AnomalyFeedbackApi api) {
    final MergedAnomalyResultDTO dto = get(id);

    final AnomalyFeedbackDTO feedbackDTO = ApiBeanMapper.toAnomalyFeedbackDTO(api);
    dto.setFeedback(feedbackDTO);
    mergedAnomalyResultManager.updateAnomalyFeedback(dto);

    if (dto.isChild()) {
      optional(mergedAnomalyResultManager.findParent(dto))
          .ifPresent(p -> {
            p.setFeedback(feedbackDTO);
            mergedAnomalyResultManager.updateAnomalyFeedback(p);
          });
    }

    return Response
        .ok()
        .build();
  }

  @GET
  @Path("stats")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnomalyStats(
      @QueryParam("startTime") final Long startTime,
      @QueryParam("endTime") final Long endTime
  ) {
    final List<Predicate> predicates = new ArrayList<>();
    optional(startTime).ifPresent(start -> predicates.add(Predicate.GE("startTime", startTime)));
    optional(endTime).ifPresent(end -> predicates.add(Predicate.LE("endTime", endTime)));
    final DaoFilter filter = predicates.isEmpty()
        ? null : new DaoFilter().setPredicate(Predicate.AND(predicates.toArray(Predicate[]::new)));
    return Response.ok(analyticsService.computeAnomalyStats(filter)).build();
  }
}
