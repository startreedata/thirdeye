package org.apache.pinot.thirdeye.datalayer.bao;

import com.google.inject.Inject;
import java.util.List;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;

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
