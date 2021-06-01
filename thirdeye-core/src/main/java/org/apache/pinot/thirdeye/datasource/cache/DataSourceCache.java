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

package org.apache.pinot.thirdeye.datasource.cache;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.util.ThirdeyeMetricsUtil;

@Singleton
public class DataSourceCache {

  private final ExecutorService executorService;
  private final Map<String, ThirdEyeDataSource> dataSourceMap;

  @Inject
  public DataSourceCache(final DataSourcesLoader dataSourcesLoader) {
    this.executorService = Executors.newCachedThreadPool();
    this.dataSourceMap = dataSourcesLoader.getDataSourceMap();
  }

  public ThirdEyeDataSource getDataSource(String dataSource) {
    checkState(dataSourceMap.size() > 0, "No data sources loaded!");
    return dataSourceMap.get(dataSource);
  }

  public ThirdEyeResponse getQueryResult(ThirdEyeRequest request) throws Exception {
    long tStart = System.nanoTime();
    try {
      String dataSource = request.getDataSource();

      return getDataSource(dataSource).execute(request);
    } catch (Exception e) {
      ThirdeyeMetricsUtil.datasourceExceptionCounter.inc();
      throw e;
    } finally {
      ThirdeyeMetricsUtil.datasourceCallCounter.inc();
      ThirdeyeMetricsUtil.datasourceDurationCounter.inc(System.nanoTime() - tStart);
    }
  }

  public Future<ThirdEyeResponse> getQueryResultAsync(final ThirdEyeRequest request)
      throws Exception {
    return executorService.submit(() -> getQueryResult(request));
  }

  public Map<ThirdEyeRequest, Future<ThirdEyeResponse>> getQueryResultsAsync(
      final List<ThirdEyeRequest> requests) throws Exception {
    Map<ThirdEyeRequest, Future<ThirdEyeResponse>> responseFuturesMap = new LinkedHashMap<>();
    for (final ThirdEyeRequest request : requests) {
      Future<ThirdEyeResponse> responseFuture =
          executorService.submit(() -> getQueryResult(request));
      responseFuturesMap.put(request, responseFuture);
    }
    return responseFuturesMap;
  }

  public void clear() throws Exception {
    dataSourceMap.clear();
  }
}
