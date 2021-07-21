/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datalayer.bao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;

@Singleton
public class MetricConfigManagerImpl extends AbstractManagerImpl<MetricConfigDTO>
    implements MetricConfigManager {

  private static final String FIND_BY_NAME_OR_ALIAS_LIKE = " WHERE active = :active and (alias like :name or name like :name)";

  private static final String FIND_BY_ALIAS_LIKE = " WHERE active = :active";
  private static final String FIND_BY_ALIAS_LIKE_PART = " AND alias LIKE :alias__%d";

  @Inject
  public MetricConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(MetricConfigDTO.class, genericPojoDao);
  }

  @Override
  public List<MetricConfigDTO> findByDataset(String dataset) {
    Predicate predicate = Predicate.EQ("dataset", dataset);
    return findByPredicate(predicate);
  }

  @Override
  public List<MetricConfigDTO> findActiveByDataset(String dataset) {
    Predicate datasetPredicate = Predicate.EQ("dataset", dataset);
    Predicate activePredicate = Predicate.EQ("active", true);
    Predicate predicate = Predicate.AND(datasetPredicate, activePredicate);
    return findByPredicate(predicate);
  }

  @Override
  public MetricConfigDTO findByMetricAndDataset(String metricName, String dataset) {
    Predicate datasetPredicate = Predicate.EQ("dataset", dataset);
    Predicate metricNamePredicate = Predicate.EQ("name", metricName);
    List<MetricConfigDTO> list = genericPojoDao
        .get(Predicate.AND(datasetPredicate, metricNamePredicate),
            MetricConfigDTO.class);
    if (CollectionUtils.isNotEmpty(list)) {
      return list.get(0);
    }
    return null;
  }

  public List<MetricConfigDTO> findByMetricName(String metricName) {
    Predicate metricNamePredicate = Predicate.EQ("name", metricName);
    return findByPredicate(metricNamePredicate);
  }

  @Override
  public List<MetricConfigDTO> findWhereNameOrAliasLikeAndActive(String name) {
    Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("name", name);
    parameterMap.put("active", true);
    return genericPojoDao.executeParameterizedSQL(FIND_BY_NAME_OR_ALIAS_LIKE, parameterMap,
        MetricConfigDTO.class);
  }

  @Override
  public List<MetricConfigDTO> findWhereAliasLikeAndActive(Set<String> aliasParts) {
    StringBuilder query = new StringBuilder();
    query.append(FIND_BY_ALIAS_LIKE);

    Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("active", true);
    int i = 0;
    for (String n : aliasParts) {
      query.append(String.format(FIND_BY_ALIAS_LIKE_PART, i));
      parameterMap
          .put(String.format("alias__%d", i), "%" + n + "%"); // using field name decomposition
      i++;
    }

    return genericPojoDao
        .executeParameterizedSQL(query.toString(), parameterMap, MetricConfigDTO.class);
  }
}
