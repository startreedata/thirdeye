/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.RootCauseSessionApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseSessionManager;
import ai.startree.thirdeye.spi.datalayer.dto.RootCauseSessionDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

@Api(tags = "Root Cause Analysis Session", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RootCauseSessionResource extends CrudResource<RootCauseSessionApi, RootCauseSessionDTO> {

  public static final ImmutableMap<String, String> API_TO_BEAN_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("anomalyId", "anomalyId")
      .build();

  @Inject
  public RootCauseSessionResource(final RootcauseSessionManager rootCauseSessionDAO) {
    super(rootCauseSessionDAO, API_TO_BEAN_FILTER_MAP);
  }

  @Override
  protected RootCauseSessionDTO createDto(final ThirdEyePrincipal principal,
      final RootCauseSessionApi api) {
    final RootCauseSessionDTO rootCauseSessionDTO = ApiBeanMapper.toDto(api);
    rootCauseSessionDTO.setCreatedBy(principal.getName());
    return rootCauseSessionDTO;
  }

  @Override
  protected RootCauseSessionDTO toDto(final RootCauseSessionApi api) {
    return ApiBeanMapper.toDto(api);
  }

  @Override
  protected RootCauseSessionApi toApi(final RootCauseSessionDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Override
  protected void validate(final RootCauseSessionApi api, final RootCauseSessionDTO existing) {
    super.validate(api, existing);
    ensureExists(api.getAnomalyId(), "AnomalyId must be present");
  }

  @GET
  @Path("/query")
  @ApiOperation(value = "Query")
  @Deprecated
  public List<RootCauseSessionDTO> query(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("id") String idsString,
      @QueryParam("name") String namesString,
      @QueryParam("owner") String ownersString,
      @QueryParam("previousId") String previousIdsString,
      @QueryParam("anomalyId") String anomalyIdsString,
      @QueryParam("anomalyRangeStart") Long anomalyRangeStart,
      @QueryParam("anomalyRangeEnd") Long anomalyRangeEnd,
      @QueryParam("createdRangeStart") Long createdRangeStart,
      @QueryParam("createdRangeEnd") Long createdRangeEnd,
      @QueryParam("updatedRangeStart") Long updatedRangeStart,
      @QueryParam("updatedRangeEnd") Long updatedRangeEnd) {

    List<Predicate> predicates = new ArrayList<>();

    if (!StringUtils.isBlank(idsString)) {
      predicates.add(Predicate.IN("baseId", split(idsString)));
    }

    if (!StringUtils.isBlank(namesString)) {
      predicates.add(Predicate.IN("name", split(namesString)));
    }

    if (!StringUtils.isBlank(ownersString)) {
      predicates.add(Predicate.IN("owner", split(ownersString)));
    }

    if (!StringUtils.isBlank(previousIdsString)) {
      predicates.add(Predicate.IN("previousId", split(previousIdsString)));
    }

    if (!StringUtils.isBlank(anomalyIdsString)) {
      predicates.add(Predicate.IN("anomalyId", split(anomalyIdsString)));
    }

    if (anomalyRangeStart != null) {
      predicates.add(Predicate.GT("anomalyRangeEnd", anomalyRangeStart));
    }

    if (anomalyRangeEnd != null) {
      predicates.add(Predicate.LT("anomalyRangeStart", anomalyRangeEnd));
    }

    if (createdRangeStart != null) {
      predicates.add(Predicate.GE("created", createdRangeStart));
    }

    if (createdRangeEnd != null) {
      predicates.add(Predicate.LT("created", createdRangeEnd));
    }

    if (updatedRangeStart != null) {
      predicates.add(Predicate.GE("updated", updatedRangeStart));
    }

    if (updatedRangeEnd != null) {
      predicates.add(Predicate.LT("updated", updatedRangeEnd));
    }

    if (predicates.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one property");
    }

    return this.dtoManager.findByPredicate(Predicate.AND(predicates.toArray(new Predicate[predicates
        .size()])));
  }

  /**
   * Returns query param value split by comma and trimmed of empty entries.
   *
   * @param str query param value string (comma separated)
   * @return array of non-empty values
   */
  private static String[] split(String str) {
    List<String> args = new ArrayList<>(Arrays.asList(str.split(",")));
    Iterator<String> itStr = args.iterator();
    while (itStr.hasNext()) {
      if (itStr.next().length() <= 0) {
        itStr.remove();
      }
    }
    return args.toArray(new String[args.size()]);
  }
}
