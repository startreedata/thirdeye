/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.auto.onboard.pinotsql;

import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.datasource.pinotsql.PinotSqlThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import org.apache.pinot.thirdeye.spi.datasource.AutoOnboard;
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

  private final ThirdEyePinotSqlClient thirdEyePinotSqlClient;
  private final String dataSourceName;

  public AutoOnboardPinotMetadataSource(DataSourceMetaBean dataSourceMeta) {
    super(dataSourceMeta);
    thirdEyePinotSqlClient = new ThirdEyePinotSqlClient(dataSourceMeta);
    dataSourceName = MapUtils.getString(dataSourceMeta.getProperties(), "name",
        PinotSqlThirdEyeDataSource.class.getSimpleName());
  }

  public AutoOnboardPinotMetadataSource(DataSourceMetaBean dataSourceMeta,
      final ThirdEyePinotSqlClient thirdEyePinotSqlClient,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    super(dataSourceMeta);
    this.thirdEyePinotSqlClient = thirdEyePinotSqlClient;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;

    dataSourceName = MapUtils.getString(dataSourceMeta.getProperties(), "name",
        PinotSqlThirdEyeDataSource.class.getSimpleName());
  }

  public void run() {
    try {
      LOG.info("Checking all pinot tables");
      final PinotSqlDatasetOnboarder pinotSqlDatasetOnboarder = new PinotSqlDatasetOnboarder(
          thirdEyePinotSqlClient,
          datasetConfigManager,
          metricConfigManager);
      pinotSqlDatasetOnboarder.onboardAll(dataSourceName);
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
