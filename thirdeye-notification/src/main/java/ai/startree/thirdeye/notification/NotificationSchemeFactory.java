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
package ai.startree.thirdeye.notification;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.subscriptiongroup.filter.SubscriptionGroupFilterResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NotificationSchemeFactory {

  private final SubscriptionGroupFilter subscriptionGroupFilter;

  @Inject
  public NotificationSchemeFactory(final AnomalyManager anomalyManager,
      final SubscriptionGroupFilter subscriptionGroupFilter) {
    this.subscriptionGroupFilter = subscriptionGroupFilter;
  }

  public SubscriptionGroupFilterResult getDetectionAlertFilterResult(
      final SubscriptionGroupDTO subscriptionGroup) throws Exception {
    // Load all the anomalies along with their recipients
    requireNonNull(subscriptionGroup, "subscription Group is null");
    SubscriptionGroupFilterResult result = subscriptionGroupFilter.filter(subscriptionGroup,
        System.currentTimeMillis());

    return result;
  }
}
