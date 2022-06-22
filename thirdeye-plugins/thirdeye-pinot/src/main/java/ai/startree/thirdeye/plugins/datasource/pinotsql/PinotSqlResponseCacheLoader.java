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

import com.google.common.cache.CacheLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;

public abstract class PinotSqlResponseCacheLoader extends
    CacheLoader<PinotSqlQuery, ResultSet> {

  /**
   * Initializes the cache loader using the given property map.
   *
   * @param properties the property map that provides the information to connect to the data
   *     source.
   * @throws Exception when an error occurs connecting to the Pinot controller.
   */
  public abstract void init(Map<String, Object> properties) throws Exception;

  public abstract Connection getConnection();

  public abstract void close();
}
