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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;

@Singleton
public class MetricConfigManagerImpl extends AbstractManagerImpl<MetricConfigDTO>
    implements MetricConfigManager {

  @Inject
  public MetricConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(MetricConfigDTO.class, genericPojoDao);
  }

  @Override
  public MetricConfigDTO findBy(final String metricName,
      final String dataset, final String namespace) {
    final Predicate datasetPredicate = Predicate.EQ("dataset", dataset);
    final Predicate metricNamePredicate = Predicate.EQ("name", metricName);
    final Predicate predicate = Predicate.AND(datasetPredicate, metricNamePredicate);
    final List<MetricConfigDTO> list = findByPredicate(predicate)
        // TODO CYRIL authz - filter in the sql read, not in app
        .stream().filter(d -> Objects.equals(d.namespace(), namespace)).toList();
    if (CollectionUtils.isNotEmpty(list)) {
      // TODO CYRIL behavior is different in AbstractManager#findUniqueByNameAndNamespace --> clarify
      return list.getFirst();
    }
    return null;
  }
}
