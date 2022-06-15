/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceFactory;
import ai.startree.thirdeye.plugins.datasource.pinotsql.PinotSqlDataSourceFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

@AutoService(Plugin.class)
public class PinotDataSourcePlugin implements Plugin {
  @Override
  public Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return ImmutableList.of(
        new PinotSqlDataSourceFactory(),
        new PinotThirdEyeDataSourceFactory()
    );
  }
}
