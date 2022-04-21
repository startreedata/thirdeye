/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseSessionManager;
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
public class RootcauseSessionManagerImpl extends AbstractManagerImpl<RcaInvestigationDTO> implements
    RootcauseSessionManager {

  private static final String FIND_BY_LIKE_TEMPLATE = "WHERE %s";
  private static final String FIND_BY_LIKE_JOINER = " AND ";
  private static final String FIND_BY_LIKE_VALUE = "%%%s%%";

  private static final String FIND_BY_NAME_LIKE_TEMPLATE = "name LIKE :name__%d";
  private static final String FIND_BY_NAME_LIKE_KEY = "name__%d";

  @Inject
  public RootcauseSessionManagerImpl(GenericPojoDao genericPojoDao) {
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
  public List<RcaInvestigationDTO> findByPreviousId(long id) {
    return findByPredicate(Predicate.EQ("previousId", id));
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
