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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class RcaInvestigationManagerImpl extends AbstractManagerImpl<RcaInvestigationDTO> implements
    RcaInvestigationManager {

  private static final String FIND_BY_LIKE_TEMPLATE = "WHERE %s";
  private static final String FIND_BY_LIKE_JOINER = " AND ";
  private static final String FIND_BY_LIKE_VALUE = "%%%s%%";

  private static final String FIND_BY_NAME_LIKE_TEMPLATE = "name LIKE :name__%d";
  private static final String FIND_BY_NAME_LIKE_KEY = "name__%d";

  @Inject
  public RcaInvestigationManagerImpl(GenericPojoDao genericPojoDao) {
    super(RcaInvestigationDTO.class, genericPojoDao);
  }

  @Override
  public List<RcaInvestigationDTO> findByName(String name) {
    return findByPredicate(Predicate.EQ("name", name));
  }

  @Override
  public List<RcaInvestigationDTO> findByNameLike(Set<String> nameFragments) {
    return findByLike(nameFragments, FIND_BY_NAME_LIKE_TEMPLATE, FIND_BY_NAME_LIKE_KEY);
  }

  @Override
  public List<RcaInvestigationDTO> findByOwner(String owner) {
    return findByPredicate(Predicate.EQ("owner", owner));
  }

  @Override
  public List<RcaInvestigationDTO> findByAnomalyRange(long start, long end) {
    return findByPredicate(Predicate
        .AND(Predicate.GT("anomalyRangeEnd", start), Predicate.LT("anomalyRangeStart", end)));
  }

  @Override
  public List<RcaInvestigationDTO> findByCreatedRange(long start, long end) {
    return findByPredicate(
        Predicate.AND(Predicate.GE("created", start), Predicate.LT("created", end)));
  }

  @Override
  public List<RcaInvestigationDTO> findByUpdatedRange(long start, long end) {
    return findByPredicate(
        Predicate.AND(Predicate.GE("updated", start), Predicate.LT("updated", end)));
  }

  @Override
  public List<RcaInvestigationDTO> findByAnomalyId(long id) {
    return findByPredicate(Predicate.EQ("anomalyId", id));
  }

  private List<RcaInvestigationDTO> findByLike(Set<String> fragments, String template, String key) {
    return findByLike(fragments, template, key, RcaInvestigationDTO.class);
  }

  private <B extends AbstractDTO, D> List<D> findByLike(Set<String> fragments, String template,
      String key,
      Class<B> beanClass) {
    List<String> conditions = new ArrayList<>();
    Map<String, Object> params = new HashMap<>();

    int i = 0;
    for (String fragment : fragments) {
      conditions.add(String.format(template, i));
      params.put(String.format(key, i), String.format(FIND_BY_LIKE_VALUE, fragment));
      i++;
    }

    String query = String
        .format(FIND_BY_LIKE_TEMPLATE, StringUtils.join(conditions, FIND_BY_LIKE_JOINER));

    return (List<D>) genericPojoDao.executeParameterizedSQL(query, params, beanClass);
  }
}
