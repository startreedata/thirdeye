/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;

public interface DatasetConfigManager extends AbstractManager<DatasetConfigDTO> {

  DatasetConfigDTO findByDataset(String dataset);

  List<DatasetConfigDTO> findActive();

  void updateLastRefreshTime(String dataset, long lastRefreshTime, long lastEventTime);
}
