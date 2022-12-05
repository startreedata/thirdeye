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
package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.List;

/**
 * This class intercepts all data source calls and helps with telemetry, etc.
 */
public class DataSourceWrapper implements ThirdEyeDataSource {

  private final ThirdEyeDataSource delegate;

  private final Meter fetchTableExceptionMeter;
  private final Timer fetchTableTimer;

  public DataSourceWrapper(final ThirdEyeDataSource delegate, final MetricRegistry metricRegistry) {
    this.delegate = delegate;

    fetchTableExceptionMeter = metricRegistry.meter("fetchTableExceptionMeter");
    fetchTableTimer = metricRegistry.timer("fetchTableTimer");
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    delegate.init(context);
  }

  @Override
  public List<DatasetConfigDTO> getDatasets() {
    return delegate.getDatasets();
  }

  @Override
  public DatasetConfigDTO getDataset(final String datasetName) {
    return delegate.getDataset(datasetName);
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    return fetchTableTimer.time(() -> fetchDataTable0(request));
  }

  private DataTable fetchDataTable0(final DataSourceRequest request) throws Exception {
    try {
      return delegate.fetchDataTable(request);
    } catch (Exception e) {
      // track exceptions
      fetchTableExceptionMeter.mark();
      throw e;
    }
  }

  @Override
  public boolean validate() {
    return delegate.validate();
  }

  @Override
  public SqlLanguage getSqlLanguage() {
    return delegate.getSqlLanguage();
  }

  @Override
  public SqlExpressionBuilder getSqlExpressionBuilder() {
    return delegate.getSqlExpressionBuilder();
  }

  @Override
  public void clear() throws Exception {
    delegate.clear();
  }

  @Override
  public void close() throws Exception {
    delegate.close();
  }
}
