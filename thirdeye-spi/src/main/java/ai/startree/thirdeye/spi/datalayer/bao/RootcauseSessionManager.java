/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.RootcauseSessionDTO;
import java.util.List;
import java.util.Set;

public interface RootcauseSessionManager extends AbstractManager<RootcauseSessionDTO> {

  List<RootcauseSessionDTO> findByName(String name);

  List<RootcauseSessionDTO> findByNameLike(Set<String> nameFragments);

  List<RootcauseSessionDTO> findByOwner(String owner);

  List<RootcauseSessionDTO> findByAnomalyRange(long start, long end);

  List<RootcauseSessionDTO> findByCreatedRange(long start, long end);

  List<RootcauseSessionDTO> findByUpdatedRange(long start, long end);

  List<RootcauseSessionDTO> findByPreviousId(long id);

  List<RootcauseSessionDTO> findByAnomalyId(long id);
}
