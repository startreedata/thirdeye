/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import java.util.List;
import java.util.Set;

public interface RootcauseSessionManager extends AbstractManager<RcaInvestigationDTO> {

  List<RcaInvestigationDTO> findByName(String name);

  List<RcaInvestigationDTO> findByNameLike(Set<String> nameFragments);

  List<RcaInvestigationDTO> findByOwner(String owner);

  List<RcaInvestigationDTO> findByAnomalyRange(long start, long end);

  List<RcaInvestigationDTO> findByCreatedRange(long start, long end);

  List<RcaInvestigationDTO> findByUpdatedRange(long start, long end);

  List<RcaInvestigationDTO> findByPreviousId(long id);

  List<RcaInvestigationDTO> findByAnomalyId(long id);
}
