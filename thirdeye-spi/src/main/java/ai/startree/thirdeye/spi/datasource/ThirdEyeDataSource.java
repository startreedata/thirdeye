/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
