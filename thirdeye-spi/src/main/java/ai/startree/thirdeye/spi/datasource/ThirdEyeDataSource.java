/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.List;

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

  /**
   * Onboard all datasets available in the data source.
   *
   * @return ThirdEye dataset describing available dimensions and metrics.
   */
  default List<DatasetConfigDTO> getDatasets() {
    throw new UnsupportedOperationException();
  }

  /**
   * Fetch metadata about the dataset.
   *
   * @param datasetName name of the table
   * @return ThirdEye dataset describing available dimensions and metrics.
   */
  default DatasetConfigDTO getDataset(String datasetName) {
    throw new UnsupportedOperationException();
  }

  DataTable fetchDataTable(DataSourceRequest request) throws Exception;

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
