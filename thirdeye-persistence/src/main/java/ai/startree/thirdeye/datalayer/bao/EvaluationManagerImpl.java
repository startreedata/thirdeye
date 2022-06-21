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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.EvaluationManager;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EvaluationManagerImpl extends AbstractManagerImpl<EvaluationDTO> implements
    EvaluationManager {

  @Inject
  public EvaluationManagerImpl(GenericPojoDao genericPojoDao) {
    super(EvaluationDTO.class, genericPojoDao);
  }
}
