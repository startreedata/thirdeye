/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import java.util.List;
import java.util.Set;

public interface RcaInvestigationManager extends AbstractManager<RcaInvestigationDTO> {

  List<RcaInvestigationDTO> findByName(String name);

  List<RcaInvestigationDTO> findByNameLike(Set<String> nameFragments);

  List<RcaInvestigationDTO> findByOwner(String owner);

  List<RcaInvestigationDTO> findByAnomalyRange(long start, long end);

  List<RcaInvestigationDTO> findByCreatedRange(long start, long end);

  List<RcaInvestigationDTO> findByUpdatedRange(long start, long end);

  List<RcaInvestigationDTO> findByAnomalyId(long id);
}
