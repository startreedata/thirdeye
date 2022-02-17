/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;

/**
 * This is the abstract parent class for all auto-onboard metadata services for various datasources
 */
public abstract class AutoOnboard {

  protected final DataSourceMetaBean meta;
  protected MetricConfigManager metricConfigManager;
  protected DatasetConfigManager datasetConfigManager;

  public AutoOnboard(DataSourceMetaBean meta) {
    this.meta = meta;
  }

  public DataSourceMetaBean getMeta() {
    return meta;
  }

  public void init(ThirdEyeDataSourceContext context) {
    metricConfigManager = context.getMetricConfigManager();
    datasetConfigManager = context.getDatasetConfigManager();
  }

  /**
   * Method which contains implementation of what needs to be done as part of onboarding for this
   * data source
   */
  public abstract void run();

  /**
   * Method for triggering adhoc run of the onboard logic
   */
  public abstract void runAdhoc();
}
