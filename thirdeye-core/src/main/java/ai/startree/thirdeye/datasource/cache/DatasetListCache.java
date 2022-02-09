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

package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetListCache {

  private List<String> datasetListRef;
  private final DatasetConfigManager datasetConfigDAO;
  private static final Logger LOG = LoggerFactory.getLogger(DatasetListCache.class);

  private final Object updateLock = new Object();

  private final long refreshInterval;

  private long nextUpdate = Long.MIN_VALUE;

  public DatasetListCache(DatasetConfigManager datasetConfigDAO, long refreshInterval) {
    this.datasetListRef = new ArrayList<>();
    this.datasetConfigDAO = datasetConfigDAO;
    this.refreshInterval = refreshInterval;
  }

  public List<String> getDatasets() {
    // test and test and set
    if (this.nextUpdate <= System.currentTimeMillis()) {
      synchronized (this.updateLock) {
        if (this.nextUpdate <= System.currentTimeMillis()) {
          this.nextUpdate = System.currentTimeMillis() + this.refreshInterval;

          List<String> datasets = new ArrayList<>();
          for (DatasetConfigDTO dataset : this.datasetConfigDAO.findAll()) {
            datasets.add(dataset.getDataset());
          }

          this.datasetListRef = datasets;
        }
      }
    }

    return this.datasetListRef;
  }

  public void expire() {
    this.nextUpdate = Long.MIN_VALUE;
  }
}

