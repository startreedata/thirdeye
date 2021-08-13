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

package org.apache.pinot.thirdeye.datasource.pinot;

import com.google.common.cache.CacheLoader;
import java.util.Map;
import org.apache.pinot.client.Connection;
import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSetGroup;

public abstract class PinotResponseCacheLoader extends
    CacheLoader<PinotQuery, ThirdEyeResultSetGroup> {

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
