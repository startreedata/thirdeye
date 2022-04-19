/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.RootCauseSessionDTO;
import java.util.List;
import java.util.Set;

public interface RootcauseSessionManager extends AbstractManager<RootCauseSessionDTO> {

  List<RootCauseSessionDTO> findByName(String name);

  List<RootCauseSessionDTO> findByNameLike(Set<String> nameFragments);

  List<RootCauseSessionDTO> findByOwner(String owner);

  List<RootCauseSessionDTO> findByAnomalyRange(long start, long end);

  List<RootCauseSessionDTO> findByCreatedRange(long start, long end);

  List<RootCauseSessionDTO> findByUpdatedRange(long start, long end);

  List<RootCauseSessionDTO> findByPreviousId(long id);

  List<RootCauseSessionDTO> findByAnomalyId(long id);
}
