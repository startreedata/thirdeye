/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.rootcause.entity.AnomalyEventEntity;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseSessionManager;
import ai.startree.thirdeye.spi.datalayer.dto.RootCauseSessionDTO;
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
import java.util.Objects;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;

@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RootCauseSessionResource {

  private final RootcauseSessionManager sessionDAO;
  private final ObjectMapper mapper;

  @Inject
  public RootCauseSessionResource(RootcauseSessionManager sessionDAO, ObjectMapper mapper) {
    this.sessionDAO = sessionDAO;
    this.mapper = mapper;
  }

  @GET
  @Path("/{sessionId}")
  @ApiOperation(value = "Get RootCauseSession by sessionId")
  public RootCauseSessionDTO get(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("sessionId") Long sessionId) {
    if (sessionId == null) {
      throw new IllegalArgumentException("Must provide sessionId");
    }

    RootCauseSessionDTO session = this.sessionDAO.findById(sessionId);

    if (session == null) {
      throw new IllegalArgumentException(String.format("Could not resolve session id %d",
          sessionId));
    }

    return session;
  }

  @POST
  @Path("/")
  @ApiOperation(value = "Post a session")
  public Long post(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal, String jsonString)
      throws Exception {
    RootCauseSessionDTO session = this.mapper.readValue(jsonString,
        new TypeReference<RootCauseSessionDTO>() {});

    final long timestamp = DateTime.now().getMillis();
    final String username = principal.getName();

    session.setUpdated(timestamp);

    if (session.getId() == null) {
      session.setCreated(timestamp);
      session.setOwner(username);
      session.setAnomalyId(extractAnomalyId(session.getAnomalyUrns()));
    } else {
      RootCauseSessionDTO existing = this.sessionDAO.findById(session.getId());
      if (existing == null) {
        throw new IllegalArgumentException(String.format("Could not resolve session id %d",
            session.getId()));
      }

      if (Objects.equals(existing.getPermissions(),
          RootCauseSessionDTO.PermissionType.READ.toString()) &&
          !Objects.equals(existing.getOwner(), username)) {
        throw new IllegalAccessException(String.format(
            "No write permissions for '%s' on session id %d",
            username,
            existing.getId()));
      }

      session = merge(existing, session);
    }

    return this.sessionDAO.save(session);
  }

  @GET
  @Path("/query")
  @ApiOperation(value = "Query")
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

    return this.sessionDAO.findByPredicate(Predicate.AND(predicates.toArray(new Predicate[predicates
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

  /**
   * Merges attributes of an existing session with incoming updates. Does NOT update all values.
   *
   * @param session existing rootcause session
   * @param other updated rootcause session
   * @return modified, existing session
   */
  private static RootCauseSessionDTO merge(RootCauseSessionDTO session, RootCauseSessionDTO other) {
    if (other.getName() != null) {
      session.setName(other.getName());
    }

    if (other.getText() != null) {
      session.setText(other.getText());
    }

    if (other.getCompareMode() != null) {
      session.setCompareMode(other.getCompareMode());
    }

    if (other.getGranularity() != null) {
      session.setGranularity(other.getGranularity());
    }

    if (other.getAnalysisRangeStart() != null) {
      session.setAnalysisRangeStart(other.getAnalysisRangeStart());
    }

    if (other.getAnalysisRangeEnd() != null) {
      session.setAnalysisRangeEnd(other.getAnalysisRangeEnd());
    }

    if (other.getAnomalyRangeStart() != null) {
      session.setAnomalyRangeStart(other.getAnomalyRangeStart());
    }

    if (other.getAnomalyRangeEnd() != null) {
      session.setAnomalyRangeEnd(other.getAnomalyRangeEnd());
    }

    if (other.getContextUrns() != null) {
      session.setContextUrns(other.getContextUrns());
    }

    if (other.getSelectedUrns() != null) {
      session.setSelectedUrns(other.getSelectedUrns());
    }

    if (other.getUpdated() != null) {
      session.setUpdated(other.getUpdated());
    }

    if (other.getPermissions() != null) {
      session.setPermissions(other.getPermissions());
    }

    if (other.getIsUserCustomizingRequest() != null) {
      session.setIsUserCustomizingRequest(other.getIsUserCustomizingRequest());
    }

    if (other.getCustomTableSettings() != null) {
      session.setCustomTableSettings(other.getCustomTableSettings());
    }

    return session;
  }

  /**
   * Returns the first anomaly entity id from a set of URNs, or {@code null} if none can be found.
   *
   * @param urns urns to scan
   * @return first anomaly entity id, or null
   */
  private static Long extractAnomalyId(Iterable<String> urns) {
    for (String urn : urns) {
      if (AnomalyEventEntity.TYPE.isType(urn)) {
        return AnomalyEventEntity.fromURN(urn, 1.0).getId();
      }
    }
    return null;
  }
}
