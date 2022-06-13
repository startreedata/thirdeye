/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

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
