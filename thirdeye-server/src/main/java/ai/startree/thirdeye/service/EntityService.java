/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.ResourceUtils.ensureExists;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class EntityService {

  private final GenericPojoDao genericPojoDao;
  private final AuthorizationManager authorizationManager;
  
  @Inject
  public EntityService(final GenericPojoDao genericPojoDao,
      final AuthorizationManager authorizationManager) {
    this.genericPojoDao = genericPojoDao;
    this.authorizationManager = authorizationManager;
  }

  public AbstractDTO getRawEntity(final ThirdEyeServerPrincipal principal, final Long id) {
    authorizationManager.ensureHasRootAccess(principal);
    return ensureExists(genericPojoDao.getRaw(id));
  }

  @NonNull
  public Map<String, Long> countEntitiesByType(final ThirdEyeServerPrincipal principal) {
    authorizationManager.ensureHasRootAccess(principal);
    final Map<String, Long> entityCountMap = new TreeMap<>();
    final Set<Class<? extends AbstractDTO>> beanClasses = genericPojoDao.getAllBeanClasses();
    for (Class<? extends AbstractDTO> beanClass : beanClasses) {
      final long count = genericPojoDao.count(beanClass);
      entityCountMap.put(beanClass.getName(), count);
    }
    return entityCountMap;
  }

  public List<String> getBeanFields(final ThirdEyeServerPrincipal principal, final String beanClass)
      throws ClassNotFoundException {
    authorizationManager.ensureHasRootAccess(principal);
    return genericPojoDao.getIndexedColumns(Class.forName(beanClass));
  }

  public List<? extends AbstractDTO> getEntity(final ThirdEyeServerPrincipal principal,
      final String beanClassRef, final UriInfo uriInfo) throws ClassNotFoundException {
    authorizationManager.ensureHasRootAccess(principal);
    final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    int limit = 10;
    int offset = 0;

    if (queryParameters.getFirst("limit") != null) {
      limit = Integer.parseInt(queryParameters.getFirst("limit"));
    }
    if (queryParameters.getFirst("offset") != null) {
      offset = Integer.parseInt(queryParameters.getFirst("offset"));
    }

    final Class<? extends AbstractDTO> beanClass = (Class<? extends AbstractDTO>) Class.forName(
        beanClassRef);
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
      final DaoFilter daoFilter = new DaoFilter().setBeanClass(beanClass)
          .setPredicate(Predicate.AND(predicates.toArray(new Predicate[]{})));
      abstractBeans = genericPojoDao.get(daoFilter);
    } else {
      abstractBeans = genericPojoDao.list(beanClass, limit, offset);
    }
    return abstractBeans;
  }
}
