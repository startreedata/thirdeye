/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
