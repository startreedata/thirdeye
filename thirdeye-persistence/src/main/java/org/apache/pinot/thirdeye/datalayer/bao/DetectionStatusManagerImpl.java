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
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DetectionStatusManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DetectionStatusDTO;

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
