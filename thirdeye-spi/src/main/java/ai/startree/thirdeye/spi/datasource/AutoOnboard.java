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
