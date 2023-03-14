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
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class SubscriptionGroupManagerImpl extends
    AbstractManagerImpl<SubscriptionGroupDTO> implements SubscriptionGroupManager {

  @Inject
  public SubscriptionGroupManagerImpl(final GenericPojoDao genericPojoDao,
      final MetricRegistry metricRegistry) {
    super(SubscriptionGroupDTO.class, genericPojoDao);
    metricRegistry.register("subscriptionsCountTotal",
        new CachedGauge<Integer>(15, TimeUnit.MINUTES) {
      @Override
      public Integer loadValue() {
        return findAll().stream()
            .filter(SubscriptionGroupManagerImpl::isPairPresent)
            .map(sg -> sg.getAlertAssociations().size() * sg.getSpecs().size())
            .reduce(0, Integer::sum);
      }
    });
  }
  
  private static boolean isPairPresent(final SubscriptionGroupDTO subscriptionGroup) {
    return subscriptionGroup.getAlertAssociations() != null &&
        !subscriptionGroup.getAlertAssociations().isEmpty() &&
        subscriptionGroup.getSpecs() != null &&
        !subscriptionGroup.getSpecs().isEmpty();
  }
}
