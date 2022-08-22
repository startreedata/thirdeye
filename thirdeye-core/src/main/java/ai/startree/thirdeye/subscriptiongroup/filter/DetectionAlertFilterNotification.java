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
package ai.startree.thirdeye.subscriptiongroup.filter;

import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Objects;

/**
 * Container class for notification properties
 */
public class DetectionAlertFilterNotification {

  SubscriptionGroupDTO subsConfig;
  Multimap<String, String> dimensionFilters;

  public DetectionAlertFilterNotification(SubscriptionGroupDTO subsConfig) {
    this(subsConfig, ArrayListMultimap.create());
  }

  public DetectionAlertFilterNotification(SubscriptionGroupDTO subsConfig,
      Multimap<String, String> dimensionFilters) {
    this.subsConfig = subsConfig;
    this.dimensionFilters = dimensionFilters;
  }

  public SubscriptionGroupDTO getSubscriptionConfig() {
    return subsConfig;
  }

  public void setSubscriptionConfig(SubscriptionGroupDTO subsConfig) {
    this.subsConfig = subsConfig;
  }

  public Multimap<String, String> getDimensionFilters() {
    return dimensionFilters;
  }

  public void setDimensionFilters(Multimap<String, String> dimensions) {
    this.dimensionFilters = dimensions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DetectionAlertFilterNotification that = (DetectionAlertFilterNotification) o;
    return Objects.equals(subsConfig, that.subsConfig) && Objects
        .equals(dimensionFilters, that.dimensionFilters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subsConfig, dimensionFilters);
  }
}
