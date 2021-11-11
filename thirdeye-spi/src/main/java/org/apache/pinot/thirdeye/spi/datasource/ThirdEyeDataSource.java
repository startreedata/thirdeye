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

import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlLanguage;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;

public interface ThirdEyeDataSource {

  /**
   * Returns simple name of the class
   */
  String getName();

  /**
   * Initilize the data source
   *
   * @param context Provides initialization parameters and relevant services.
   */
  void init(ThirdEyeDataSourceContext context);

  ThirdEyeResponse execute(ThirdEyeRequest request) throws Exception;

  List<String> getDatasets() throws Exception;

  /**
   * Onboard all datasets available in the data source.
   *
   * @return ThirdEye dataset describing available dimensions and metrics.
   */
  default List<DatasetConfigDTO> onboardAll() {
    throw new UnsupportedOperationException();
  }

  /**
   * Fetch metadata about the dataset.
   *
   * @param datasetName name of the table
   * @return ThirdEye dataset describing available dimensions and metrics.
   */
  default DatasetConfigDTO onboardDataset(String datasetName) {
    throw new UnsupportedOperationException();
  }

  DataTable fetchDataTable(ThirdEyeRequestV2 request) throws Exception;

  /**
   * Returns max dateTime in millis for the dataset
   *
   * @return the time corresponding to the earliest available data point.
   */
  default long getMinDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    return -1L;
  }

  /**
   * Returns max dateTime in millis for the dataset
   */
  long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception;

  /**
   * Returns map of dimension name to dimension values for filters
   *
   * @return dimension map
   */
  Map<String, List<String>> getDimensionFilters(final DatasetConfigDTO datasetConfig)
      throws Exception;

  /**
   * Returns boolean value to validate the health of data source
   *
   * @return health validation boolean
   */
  default boolean validate() {
    throw new UnsupportedOperationException();
  }

  /**
   * Clear any cached values.
   */
  default void clear() throws Exception {
    throw new UnsupportedOperationException();
  }

  void close() throws Exception;

  default SqlLanguage getSqlLanguage() {
    return null;
  }

  default SqlExpressionBuilder getSqlExpressionBuilder() {
    return null;
  }
}
