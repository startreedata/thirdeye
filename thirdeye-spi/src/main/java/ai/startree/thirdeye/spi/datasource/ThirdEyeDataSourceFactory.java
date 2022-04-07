/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

public interface ThirdEyeDataSourceFactory {

  String name();

  ThirdEyeDataSource build(ThirdEyeDataSourceContext context);
}
