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
import ai.startree.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import ai.startree.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;
import com.google.inject.Inject;
import java.util.List;

public class OnlineDetectionDataManagerImpl extends AbstractManagerImpl<OnlineDetectionDataDTO>
    implements OnlineDetectionDataManager {

  @Inject
  public OnlineDetectionDataManagerImpl(GenericPojoDao genericPojoDao) {
    super(OnlineDetectionDataDTO.class, genericPojoDao);
  }

  @Override
  public List<OnlineDetectionDataDTO> findByDatasetAndMetric(String dataset, String metric) {
    Predicate datasetPredicate = Predicate.EQ("dataset", dataset);
    Predicate metricPredicate = Predicate.EQ("metric", metric);
    Predicate predicate = Predicate.AND(datasetPredicate, metricPredicate);

    return findByPredicate(predicate);
  }
}
