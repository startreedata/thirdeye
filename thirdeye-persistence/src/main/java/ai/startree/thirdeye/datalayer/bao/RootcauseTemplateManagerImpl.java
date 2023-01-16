/*
 * Copyright 2023 StarTree Inc
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
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.RootcauseTemplateDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class RootcauseTemplateManagerImpl extends
    AbstractManagerImpl<RootcauseTemplateDTO> implements
    RootcauseTemplateManager {

  @Inject
  public RootcauseTemplateManagerImpl(GenericPojoDao genericPojoDao) {
    super(RootcauseTemplateDTO.class, genericPojoDao);
  }

  @Override
  public List<RootcauseTemplateDTO> findByMetricId(long metricId) {
    Predicate predicate = Predicate.EQ("metricId", metricId);
    return findByPredicate(predicate);
  }

  @Override
  public Long saveOrUpdate(RootcauseTemplateDTO rootcauseTemplateDTO) {
    Predicate predicate = Predicate.EQ("name", rootcauseTemplateDTO.getName());
    List<RootcauseTemplateDTO> list = findByPredicate(predicate);
    if (!list.isEmpty()) {
      rootcauseTemplateDTO.setId(list.get(0).getId());
      super.update(rootcauseTemplateDTO);
      return rootcauseTemplateDTO.getId();
    } else {
      return super.save(rootcauseTemplateDTO);
    }
  }
}
