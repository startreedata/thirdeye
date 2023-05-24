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

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.EnumerationItemFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EnumerationItemManagerImpl extends AbstractManagerImpl<EnumerationItemDTO>
    implements EnumerationItemManager {

  private static final Logger LOG = LoggerFactory.getLogger(EnumerationItemManagerImpl.class);

  @Inject
  public EnumerationItemManagerImpl(final GenericPojoDao genericPojoDao) {
    super(EnumerationItemDTO.class, genericPojoDao);
  }

  @Override
  public List<EnumerationItemDTO> filter(final EnumerationItemFilter filter) {
    requireNonNull(filter, "Filter cannot be null");
    requireNonNull(filter.getAlertId(), "Alert id cannot be null");

    return filter(new DaoFilter().setPredicate(Predicate.EQ("alertId", filter.getAlertId())));
  }
}
