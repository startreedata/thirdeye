/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.RequestCache.buildCache;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.badRequest;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.DaoFilterBuilder;
import ai.startree.thirdeye.RequestCache;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Api(tags = "Anomaly", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource extends CrudResource<AnomalyApi, MergedAnomalyResultDTO> {

  private static final String ALERT_ID = "alertId";
  private static final String DATASET = "dataset";
  private static final String METRIC = "metric";
  public static final ImmutableMap<String, String> API_TO_BEAN_MAP = ImmutableMap.<String, String>builder()
      .put(ALERT_ID, "detectionConfigId")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .put("isChild", "child")
      .build();
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager alertManager;

  @Inject
  public AnomalyResource(
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager) {
    super(mergedAnomalyResultManager, API_TO_BEAN_MAP);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertManager = alertManager;
  }

  private static AnomalyFeedbackDTO toAnomalyFeedbackDTO(AnomalyFeedbackApi api) {
    final AnomalyFeedbackDTO dto = new AnomalyFeedbackDTO();
    dto.setComment(api.getComment());
    dto.setFeedbackType(api.getType());

    return dto;
  }

  @Override
  protected RequestCache createRequestCache() {
    return super.createRequestCache()
        .setAlerts(buildCache(alertManager::findById));
  }

  @Override
  protected MergedAnomalyResultDTO createDto(final ThirdEyePrincipal principal,
      final AnomalyApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
  }

  @Override
  protected MergedAnomalyResultDTO toDto(final AnomalyApi api) {
    // For now, anomalies are to be created/edited by the system.
    throw badRequest(ERR_OPERATION_UNSUPPORTED);
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

  private boolean checkFilters(final AlertDTO alert, final MultivaluedMap<String, String> params) {
    try {
      return checkDataset(alert, params) &&
          checkMetric(alert, params);
    } finally {
      // remove the query params as they are dealt with
      // they are not part of Anomaly API_TO_BEAN_MAP
      params.remove(METRIC);
      params.remove(DATASET);
    }
  }

  private boolean checkDataset(final AlertDTO alert, final MultivaluedMap<String, String> params) {
    String query = params.getFirst(DATASET);
    if (query != null) {
      return queryToList(query).contains(renderProperty(
          alert.getTemplate().getMetadata().getDataset().getName(),
          alert.getTemplateProperties()).toString());
    }
    return true;
  }

  private boolean checkMetric(final AlertDTO alert, final MultivaluedMap<String, String> params) {
    String query = params.getFirst(METRIC);
    if (query != null) {
      return queryToList(query).contains(renderProperty(
          alert.getTemplate().getMetadata().getMetric().getName(),
          alert.getTemplateProperties()).toString());
    }
    return true;
  }

  // extract propertyName from ${propertyName} and return its value from properties
  private Object renderProperty(final String name, final Map<String, Object> properties) {
    return properties.get(name.substring(2, name.length() - 1));
  }

  private List queryToList(final String query) {
    return Arrays.asList(query.substring(query.indexOf("]") + 1).split(","));
  }

  private String listToQuery(final List params, final String operator) {
    return String.format("%s%s", operator, Joiner.on(',').join(params));
  }

  private RequestCache prepareRequestCache(
      final MultivaluedMap<String, String> queryParameters) {
    final List<AlertDTO> alerts;
    final RequestCache cache = createRequestCache();

    // get request specific alerts
    if (queryParameters.containsKey(ALERT_ID)) {
      final MultivaluedMap<String, String> alertParams = new MultivaluedHashMap<>();
      queryParameters.get(ALERT_ID).forEach(param -> alertParams.add("id", param));
      alerts = alertManager.filter(new DaoFilterBuilder(AlertResource.API_TO_BEAN_MAP)
          .buildFilter(alertParams));
    } else {
      alerts = alertManager.findAll();
    }

    // filter and load alerts into cache
    alerts.forEach(alert -> {
      if (checkFilters(alert, queryParameters)) {
        cache.getAlerts().put(alert.getId(), alert);
      }
    });
    return cache;
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @Context UriInfo uriInfo) {
    final MultivaluedMap<String, String> finalQueryParameters = new MultivaluedHashMap<>(uriInfo.getQueryParameters());
    final RequestCache requestCache = prepareRequestCache(finalQueryParameters);

    // fetch all the ids of alerts which satisfy the filters
    final Set<Long> alertQuery = requestCache.getAlerts().asMap().keySet();

    if (alertQuery.isEmpty()) {
      return respondOk(Collections.emptyList());
    } else {
      finalQueryParameters.putSingle(ALERT_ID,
          listToQuery(Arrays.asList(alertQuery.toArray()), "[in]"));
      final List<MergedAnomalyResultDTO> results = dtoManager.filter(new DaoFilterBuilder(
          apiToBeanMap).buildFilter(finalQueryParameters));
      return respondOk(results.stream().map(anomalyDto -> toApi(anomalyDto, requestCache)));
    }
  }

  @Path("{id}/feedback")
  @POST
  @Timed
  public Response setFeedback(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") Long id,
      AnomalyFeedbackApi api) {
    final MergedAnomalyResultDTO dto = get(id);

    final AnomalyFeedbackDTO feedbackDTO = toAnomalyFeedbackDTO(api);
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
}
