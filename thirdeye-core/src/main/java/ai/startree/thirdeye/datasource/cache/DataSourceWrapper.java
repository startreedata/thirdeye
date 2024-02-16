/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.Constants.METRICS_TIMER_PERCENTILES;

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
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class intercepts all data source calls and helps with telemetry, etc.
 */
public class DataSourceWrapper implements ThirdEyeDataSource {

  private final ThirdEyeDataSource delegate;

  @Deprecated
  private final Meter fetchTableExceptionMeter;
  @Deprecated
  private final Timer fetchTableTimer;
  private final io.micrometer.core.instrument.Timer fetchTableTimer2;

  public DataSourceWrapper(final ThirdEyeDataSource delegate, final MetricRegistry metricRegistry) {
    this.delegate = delegate;
    
    // deprecated with no replacement - should not be used anymore - metric should be inside the delegate
    fetchTableExceptionMeter = metricRegistry.meter("fetchTableExceptionMeter");
    // deprecated - use thirdeye_fetch_data_table
    fetchTableTimer = metricRegistry.timer("fetchTableTimer");
    fetchTableTimer2 = io.micrometer.core.instrument.Timer.builder(
            "thirdeye_fetch_data_table")
        .description("Start: an input SQL query string is passed to the DataSource implementation. End: the result of the query is returned as a dataframe OR an exception is thrown.")
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .register(Metrics.globalRegistry);
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
    return fetchTableTimer.time(fetchTableTimer2.wrap((Callable<? extends DataTable>) () -> fetchDataTable0(request)));
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
