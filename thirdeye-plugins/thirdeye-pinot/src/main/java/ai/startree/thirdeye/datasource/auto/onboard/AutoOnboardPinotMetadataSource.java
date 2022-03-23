/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.auto.onboard;

import ai.startree.thirdeye.datasource.pinot.PinotThirdEyeDataSource;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datasource.AutoOnboard;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a service to onboard datasets automatically to thirdeye from pinot
 * The run method is invoked periodically by the AutoOnboardService, and it checks for new tables in
 * pinot, to add to thirdeye
 * It also looks for any changes in dimensions or metrics to the existing tables
 */
public class AutoOnboardPinotMetadataSource extends AutoOnboard {

  private static final Logger LOG = LoggerFactory.getLogger(AutoOnboardPinotMetadataSource.class);

  private final ThirdEyePinotClient thirdEyePinotClient;
  private final String dataSourceName;

  public AutoOnboardPinotMetadataSource(DataSourceMetaBean dataSourceMeta) {
    super(dataSourceMeta);
    thirdEyePinotClient = new ThirdEyePinotClient(dataSourceMeta, "pinot");
    dataSourceName = MapUtils.getString(dataSourceMeta.getProperties(), "name",
        PinotThirdEyeDataSource.class.getSimpleName());
  }

  public AutoOnboardPinotMetadataSource(DataSourceMetaBean dataSourceMeta,
      final ThirdEyePinotClient thirdEyePinotClient,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    super(dataSourceMeta);
    this.thirdEyePinotClient = thirdEyePinotClient;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;

    dataSourceName = MapUtils.getString(dataSourceMeta.getProperties(), "name",
        PinotThirdEyeDataSource.class.getSimpleName());
  }

  public void run() {
    try {
      LOG.info("Checking all pinot tables");
      final PinotDatasetOnboarder pinotDatasetOnboarder = new PinotDatasetOnboarder(
          thirdEyePinotClient,
          datasetConfigManager,
          metricConfigManager);
      pinotDatasetOnboarder.onboardAll(dataSourceName);
    } catch (Exception e) {
      LOG.error("Exception in loading datasets", e);
    }
  }

  @Override
  public void runAdhoc() {
    LOG.info("Triggering adhoc run for AutoOnboard Pinot data source");
    run();
  }
}
