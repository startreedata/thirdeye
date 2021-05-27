/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.auto.onboard;

import static org.apache.pinot.thirdeye.util.ConfigurationLoader.readConfig;
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.time.Duration;
import org.apache.pinot.thirdeye.datasource.DataSourcesConfiguration;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.testng.annotations.Test;

public class AutoOnboardServiceTest {

  /**
   * Uses {@link AutoOnboardAnotherRandomDataSource}
   * @throws Exception
   */
  @Test
  public void testAutoOnboardService() throws Exception {
    final URL url = AutoOnboardServiceTest.class.getResource(
        "/data-sources/data-sources-config-1.yml");

    final AutoOnboardService autoOnboardService = new AutoOnboardService(
        new AutoOnboardConfiguration()
            .setFrequency(Duration.ofSeconds(1)),
        mock(MetricConfigManager.class),
        mock(DatasetConfigManager.class),
        readConfig(url, DataSourcesConfiguration.class));

    autoOnboardService.start();
    Thread.sleep(2000);
    autoOnboardService.shutdown();
  }
}
