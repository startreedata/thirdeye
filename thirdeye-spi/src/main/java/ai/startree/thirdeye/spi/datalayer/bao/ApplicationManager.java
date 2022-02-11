/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.ApplicationDTO;
import java.util.List;

public interface ApplicationManager extends AbstractManager<ApplicationDTO> {

  List<ApplicationDTO> findByName(String name);
}
