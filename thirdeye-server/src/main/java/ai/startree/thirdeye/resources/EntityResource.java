/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "Entity", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
public class EntityResource {

  private final GenericPojoDao genericPojoDao;

  @Inject
  public EntityResource(final GenericPojoDao genericPojoDao) {
    this.genericPojoDao = genericPojoDao;
  }

  @GET
  @Path("{id}")
  public Response getRawEntity(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("id") Long id) {
    return Response.ok(ensureExists(genericPojoDao.getRaw(id))).build();
  }

  @GET
  @Path("types")
  public Response listEntities(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    final Map<String, Long> entityCountMap = new TreeMap<>();
    final Set<Class<? extends AbstractDTO>> beanClasses = genericPojoDao.getAllBeanClasses();
    for (Class<? extends AbstractDTO> beanClass : beanClasses) {
      final long count = genericPojoDao.count(beanClass);
      entityCountMap.put(beanClass.getName(), count);
    }
    return Response.ok(entityCountMap).build();
  }

  @GET
  @Path("types/{bean_class}/info")
  public Response getEntityInfo(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("bean_class") String beanClass) {
    try {
      List<String> indexedColumns = genericPojoDao.getIndexedColumns(Class.forName(beanClass));
      return Response.ok(indexedColumns).build();
    } catch (Exception e) {
      throw serverError(ThirdEyeStatus.ERR_UNKNOWN, e);
    }
  }

  @SuppressWarnings("unchecked")
  @GET
  @Path("types/{bean_class}")
  public Response getEntity(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("bean_class") String beanClassRef,
      @Context UriInfo uriInfo
  ) {
    try {
      final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      int limit = 10;
      int offset = 0;

      if (queryParameters.getFirst("limit") != null) {
        limit = Integer.parseInt(queryParameters.getFirst("limit"));
      }
      if (queryParameters.getFirst("offset") != null) {
        offset = Integer.parseInt(queryParameters.getFirst("offset"));
      }

      final Class<? extends AbstractDTO> beanClass =
          (Class<? extends AbstractDTO>) Class.forName(beanClassRef);
      final List<String> indexedColumns = genericPojoDao.getIndexedColumns(beanClass);

      final List<Predicate> predicates = new ArrayList<>();
      for (Map.Entry<String, List<String>> e : queryParameters.entrySet()) {
        final String qParam = e.getKey();
        if (indexedColumns.contains(qParam)) {
          final Object[] objects = e.getValue().toArray();
          predicates.add(Predicate.IN(qParam, objects));
        }
      }

      final List<? extends AbstractDTO> abstractBeans;
      if (!predicates.isEmpty()) {
        final DaoFilter daoFilter = new DaoFilter()
            .setBeanClass(beanClass)
            .setPredicate(Predicate.AND(predicates.toArray(new Predicate[]{})));
        abstractBeans = genericPojoDao.filter(daoFilter);
      } else {
        abstractBeans = genericPojoDao.list(beanClass, limit, offset);
      }
      return Response.ok(abstractBeans).build();
    } catch (Exception e) {
      throw serverError(ThirdEyeStatus.ERR_UNKNOWN, e);
    }
  }
}
