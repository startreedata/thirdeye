/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DetectionStatusManager;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

@Singleton
public class DetectionStatusManagerImpl extends AbstractManagerImpl<DetectionStatusDTO>
    implements DetectionStatusManager {

  @Inject
  public DetectionStatusManagerImpl(GenericPojoDao genericPojoDao) {
    super(DetectionStatusDTO.class, genericPojoDao);
  }

  @Override
  public DetectionStatusDTO findLatestEntryForFunctionId(long functionId) {
    Predicate predicate = Predicate.EQ("functionId", functionId);
    List<DetectionStatusDTO> list = genericPojoDao.get(predicate, DetectionStatusDTO.class);
    if (CollectionUtils.isNotEmpty(list)) {
      Collections.sort(list);
      return list.get(list.size() - 1);
    }
    return null;
  }

  @Override
  public List<DetectionStatusDTO> findAllInTimeRangeForFunctionAndDetectionRun(long startTime,
      long endTime,
      long functionId, boolean detectionRun) {
    Predicate predicate = Predicate.AND(
        Predicate.EQ("functionId", functionId),
        Predicate.LE("dateToCheckInMS", endTime),
        Predicate.GE("dateToCheckInMS", startTime),
        Predicate.EQ("detectionRun", detectionRun));

    return findByPredicate(predicate);
  }
}
