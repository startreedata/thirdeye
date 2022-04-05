/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import com.codahale.metrics.MetricRegistry;

public interface ThirdEyeDataSourceFactory {

  String name();

  ThirdEyeDataSource build(ThirdEyeDataSourceContext context);

  default void setMetricRegistry(final MetricRegistry metricRegistry) {

  }
}
