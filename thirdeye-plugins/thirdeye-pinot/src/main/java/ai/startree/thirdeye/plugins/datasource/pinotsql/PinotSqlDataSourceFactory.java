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
package ai.startree.thirdeye.plugins.datasource.pinotsql;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.pinot.client.PinotDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlDataSourceFactory implements ThirdEyeDataSourceFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlDataSourceFactory.class);

  @Override
  public String name() {
    return "pinot-sql";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    try {
      DriverManager.registerDriver(new PinotDriver());
    } catch (SQLException e) {
      LOG.error("Pinot Sql datasource driver registry failed!");
    }
    final ThirdEyeDataSource ds = new PinotSqlThirdEyeDataSource();
    ds.init(context);
    return ds;
  }
}
