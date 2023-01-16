/*
 * Copyright 2023 StarTree Inc
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
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class OverrideConfigManagerImpl extends AbstractManagerImpl<OverrideConfigDTO> implements
    OverrideConfigManager {

  @Inject
  public OverrideConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(OverrideConfigDTO.class, genericPojoDao);
  }

  @Override
  public List<OverrideConfigDTO> findAllConflictByTargetType(String entityTypeName,
      long windowStart, long windowEnd) {
    Predicate predicate =
        Predicate.AND(Predicate.LE("startTime", windowEnd), Predicate.GE("endTime", windowStart),
            Predicate.EQ("targetEntity", entityTypeName));

    return findByPredicate(predicate);
  }
}
