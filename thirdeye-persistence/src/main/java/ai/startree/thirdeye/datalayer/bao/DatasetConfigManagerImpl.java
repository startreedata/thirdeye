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
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class DatasetConfigManagerImpl extends AbstractManagerImpl<DatasetConfigDTO>
    implements DatasetConfigManager {

  @Inject
  public DatasetConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(DatasetConfigDTO.class, genericPojoDao);
  }

  @Override
  public List<DatasetConfigDTO> findByName(final String name) {
    return findByPredicate(Predicate.EQ("dataset", name));
  }

  @Override
  public DatasetConfigDTO findByDataset(String dataset) {
    Predicate predicate = Predicate.EQ("dataset", dataset);
    List<DatasetConfigDTO> list = findByPredicate(predicate);
    if (list.size() == 1) {
      return list.iterator().next();
    }
    return null;
  }

  @Override
  public List<DatasetConfigDTO> findActive() {
    Predicate activePredicate = Predicate.EQ("active", true);
    return findByPredicate(activePredicate);
  }

  @Override
  public void updateLastRefreshTime(String dataset, long refreshTime, long eventTime) {
    DatasetConfigDTO datasetConfigDTO = findByDataset(dataset);
    datasetConfigDTO.setLastRefreshTime(refreshTime);
    datasetConfigDTO.setLastRefreshEventTime(eventTime);
    update(datasetConfigDTO);
  }
}
