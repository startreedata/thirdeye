/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AlertManagerImpl extends AbstractManagerImpl<AlertDTO> implements
    AlertManager {

  @Inject
  public AlertManagerImpl(GenericPojoDao genericPojoDao) {
    super(AlertDTO.class, genericPojoDao);
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
      return genericPojoDao.update(alertDTO);
    }
  }

  @Override
  public Long save(AlertDTO alertDTO) {
    if (alertDTO.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(alertDTO);
      return alertDTO.getId();
    }

    Long id = genericPojoDao.put(alertDTO);
    alertDTO.setId(id);
    return id;
  }

  @Override
  public List<AlertDTO> findAllActive() {
    List<AlertDTO> detectionConfigs = findAll();
    return detectionConfigs.stream().filter(AlertDTO::isActive)
        .collect(Collectors.toList());
  }
}
