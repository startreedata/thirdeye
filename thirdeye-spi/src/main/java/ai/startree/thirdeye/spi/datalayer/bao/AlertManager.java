/*
 * Copyright 2024 StarTree Inc
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

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AlertManager extends AbstractManager<AlertDTO> {

  // should be internal only
  List<AlertDTO> findAllActive();
  
  List<AlertDTO> findAllActiveInNamespace(final @Nullable String namespace);

  // should be used internaly only. Introduce countActiveInNamespace if you want to expose to users 
  Long countActive();
}
