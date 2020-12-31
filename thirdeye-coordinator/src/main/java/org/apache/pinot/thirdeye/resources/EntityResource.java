package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;

import io.swagger.annotations.Api;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.pinot.thirdeye.ThirdEyeStatus;
import org.apache.pinot.thirdeye.datalayer.DaoFilter;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.datalayer.pojo.AbstractBean;
import org.apache.pinot.thirdeye.datalayer.util.Predicate;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "Entity")
public class EntityResource {

  private static final Package PACKAGE = EntityResource.class.getPackage();

  private final GenericPojoDao genericPojoDao;

  @Inject
  public EntityResource(final GenericPojoDao genericPojoDao) {
    this.genericPojoDao = genericPojoDao;
  }

  @GET
  @Path("{id}")
  public Response getRawEntity(@PathParam("id") Long id) {
    return Response.ok(ensureExists(genericPojoDao.getRaw(id))).build();
  }

  @GET
  @Path("entity_types")
  public Response listEntities() {
    Map<String, Long> entityCountMap = new TreeMap<>();
    Set<Class<? extends AbstractBean>> beanClasses = genericPojoDao.getAllBeanClasses();
    for (Class<? extends AbstractBean> beanClass : beanClasses) {
      long count = genericPojoDao.count(beanClass);
      entityCountMap.put(beanClass.getName(), count);
    }
    return Response.ok(entityCountMap).build();
  }

  @GET
  @Path("entity_types/{entity_type}/info")
  public Response getEntityInfo(@PathParam("entity_type") String entityType) {
    try {
      List<String> indexedColumns = genericPojoDao.getIndexedColumns(Class.forName(entityType));
      return Response.ok(indexedColumns).build();
    } catch (Exception e) {
      throw ResourceUtils.serverError(ThirdEyeStatus.ERR_UNKNOWN, e);
    }
  }

  @GET
  @Path("entities/{entity_type}")
  public Response getEntity(@PathParam("entity_type") String entityType, @Context UriInfo uriInfo) {
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

      Class<? extends AbstractBean> beanClass = (Class<? extends AbstractBean>) Class
          .forName(entityType);
      List<String> indexedColumns = genericPojoDao.getIndexedColumns(beanClass);

      List<Predicate> predicates = new ArrayList<>();
      for (Map.Entry<String, List<String>> e : queryParameters.entrySet()) {
        final String qParam = e.getKey();
        if (indexedColumns.contains(qParam)) {
          final Object[] objects = e.getValue().toArray();
          predicates.add(Predicate.IN(qParam, objects));
        }
      }

      List<? extends AbstractBean> abstractBeans;

      if (!predicates.isEmpty()) {
        final DaoFilter daoFilter = new DaoFilter();
        daoFilter
            .setBeanClass(beanClass)
            .setPredicate(Predicate.AND(predicates.toArray(new Predicate[]{})));
        abstractBeans = genericPojoDao.filter(daoFilter);
      } else {
        abstractBeans = genericPojoDao.list(beanClass, limit, offset);
      }
      return Response.ok(abstractBeans).build();
    } catch (Exception e) {
      throw ResourceUtils.serverError(ThirdEyeStatus.ERR_UNKNOWN, e);
    }
  }
}
