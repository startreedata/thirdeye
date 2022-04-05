/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.codahale.metrics.MetricRegistry;

public class PinotThirdEyeDataSourceFactory implements ThirdEyeDataSourceFactory {

  private MetricRegistry metricRegistry;

  @Override
  public String name() {
    return "pinot";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    final ThirdEyeDataSource ds = new PinotThirdEyeDataSource(new PinotSqlExpressionBuilder(),
        new PinotSqlLanguage(), metricRegistry);
    ds.init(context);

    return ds;
  }

  @Override
  public void setMetricRegistry(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }
}
