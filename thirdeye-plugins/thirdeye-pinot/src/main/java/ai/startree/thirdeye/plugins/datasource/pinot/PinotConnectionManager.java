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

package ai.startree.thirdeye.plugins.datasource.pinot;

import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotConnectionBuilder;

public class PinotConnectionManager {

  private final PinotThirdEyeDataSourceConfig config;
  private Connection connection;

  public PinotConnectionManager(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
  }

  public Connection get() {
    if (connection == null) {
      connection = new PinotConnectionBuilder().createConnection(config);
    }
    return connection;
  }

  public void close() {
    if (connection != null) {
      connection.close();
    }
  }
}
