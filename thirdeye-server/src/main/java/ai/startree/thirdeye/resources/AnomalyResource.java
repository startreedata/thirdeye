/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.badRequest;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.DaoFilterBuilder;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AlertApi;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  protected AnomalyApi toApi(final MergedAnomalyResultDTO dto) {
    final AnomalyApi anomalyApi = ApiBeanMapper.toApi(dto);
    optional(anomalyApi.getAlert())
        .filter(alertApi -> alertApi.getId() != null)
        .ifPresent(this::populateNameIfPossible);
    return anomalyApi;
  }

  private AnomalyApi toApi(final MergedAnomalyResultDTO dto,
    final HashMap<Long, AlertDTO> requestCache) {
    final AnomalyApi anomalyApi = ApiBeanMapper.toApi(dto);
    optional(anomalyApi.getAlert())
      .filter(alertApi -> alertApi.getId() != null)
      .ifPresent(alertApi -> alertApi.setName(requestCache.get(alertApi.getId()).getName()));
    return anomalyApi;
  }

  private void populateNameIfPossible(final AlertApi alertApi) {
    optional(alertManager.findById(alertApi.getId()))
        .ifPresent(alert -> alertApi.setName(alert.getName()));
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

  private Object renderProperty(final String name, final Map<String, Object> properties) {
    return properties.get(name.substring(2,name.length()-1));
  }

  private List<String> queryToList(final String query) {
    return Arrays.asList(query.substring(query.indexOf("]") + 1).split(","));
  }

  private String listToQuery(final List<String> params, final String operator) {
    return String.format("%s%s", operator, Joiner.on(',').join(params));
  }

  private HashMap<Long, AlertDTO> prepareRequestCache(
    final MultivaluedMap<String, String> queryParameters) {
    final HashMap<Long, AlertDTO> requestCache = new HashMap<>();
    if (queryParameters.containsKey(ALERT_ID)) {
      alertManager.filter(new DaoFilterBuilder(AlertResource.API_TO_BEAN_MAP)
          .buildFilter(new MultivaluedHashMap<>(Map.of("id", queryParameters.getFirst(ALERT_ID)))))
        .forEach(alert -> requestCache.put(alert.getId(), alert));
    } else {
      alertManager.findAll().forEach(alert -> requestCache.put(alert.getId(), alert));
    }
    return requestCache;
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(
    @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
    @Context UriInfo uriInfo) {
    final MultivaluedMap<String, String> finalQueryParameters = new MultivaluedHashMap<>(uriInfo.getQueryParameters());
    final HashMap<Long, AlertDTO> requestCache = prepareRequestCache(finalQueryParameters);

    // fetch all the ids of alerts which satisfy the filters
    final List<String> alertQuery = requestCache.values().stream()
      .filter(alertDTO -> checkFilters(alertDTO, finalQueryParameters))
      .map(alertDTO -> alertDTO.getId().toString())
      .collect(Collectors.toList());

    if (alertQuery.isEmpty()) {
      return respondOk(Collections.emptyList());
    } else {
      finalQueryParameters.putSingle(ALERT_ID, listToQuery(alertQuery, "[in]"));
      final List<MergedAnomalyResultDTO> results = dtoManager.filter(new DaoFilterBuilder(apiToBeanMap).buildFilter(finalQueryParameters));
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
