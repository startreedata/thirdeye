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

package org.apache.pinot.thirdeye.spi.datasource;

import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;

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
