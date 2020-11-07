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

package org.apache.pinot.thirdeye.datalayer.bao.jdbc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.DetectionConfigBean;

@Singleton
public class AlertManagerImpl extends AbstractManagerImpl<AlertDTO> implements
    AlertManager {

  @Inject
  public AlertManagerImpl(GenericPojoDao genericPojoDao) {
    super(AlertDTO.class, DetectionConfigBean.class, genericPojoDao);
  }

  @Override
  public int update(AlertDTO alertDTO) {
    if (alertDTO.getId() == null) {
      Long id = save(alertDTO);
      if (id > 0) {
        return 1;
      } else {
        return 0;
      }
    } else {
      DetectionConfigBean detectionConfigBean = convertDetectionConfigDTO2Bean(alertDTO);
      return genericPojoDao.update(detectionConfigBean);
    }
  }

  @Override
  public Long save(AlertDTO alertDTO) {
    if (alertDTO.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(alertDTO);
      return alertDTO.getId();
    }

    DetectionConfigBean detectionConfigBean = convertDetectionConfigDTO2Bean(alertDTO);
    Long id = genericPojoDao.put(detectionConfigBean);
    alertDTO.setId(id);
    return id;
  }

  DetectionConfigBean convertDetectionConfigDTO2Bean(AlertDTO alertDTO) {
    alertDTO.setComponents(Collections.emptyMap());
    DetectionConfigBean bean = convertDTO2Bean(alertDTO, DetectionConfigBean.class);
    return bean;
  }

  @Override
  public List<AlertDTO> findAllActive() {
    List<AlertDTO> detectionConfigs = findAll();
    return detectionConfigs.stream().filter(DetectionConfigBean::isActive)
        .collect(Collectors.toList());
  }
}
