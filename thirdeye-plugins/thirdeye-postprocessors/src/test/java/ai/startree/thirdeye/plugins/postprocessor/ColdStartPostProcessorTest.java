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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.plugins.postprocessor.ColdStartPostProcessor.labelName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperatorResult;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ColdStartPostProcessorTest {

  // time is not used
  private static final Interval UTC_DETECTION_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  private static final String TABLE_NAME = "tableName";
  private static final String NAMESPACE = "namespaceName";
  private static final long JANUARY_1_2022 = 1640995200000L;
  private static final long JANUARY_3_2022 = 1641168000000L;
  private static final long JANUARY_5_2022 = 1641340800000L;
  private static final long JANUARY_27_2022 = 1643241600000L;
  private static final long JANUARY_31_2022 = 1643587200000L;
  private static final long FEBRUARY_2_2022 = 1643760000000L;
  private static final long FEBRUARY_3_2022 = 1643846400000L;
  private static final String RES_1_KEY = "res1";
  private static final String RES_2_KEY = "res2";
  public static final String DATASOURCE_1 = "datasource1";

  private DatasetConfigManager datasetDao;
  private MinMaxTimeLoader minMaxTimeLoader;
  private ColdStartPostProcessorSpec spec;
  private DataSourceManager dataSourceDao;

  @BeforeClass
  public void initMocks() throws Exception {
    datasetDao = mock(DatasetConfigManager.class);
    final DatasetConfigDTO datasetDto = new DatasetConfigDTO().setDataSource(
        DATASOURCE_1);
    datasetDto.setAuth(new AuthorizationConfigurationDTO().setNamespace(NAMESPACE));
    when(datasetDao.findByNameAndNamespaceOrUnsetNamespace(TABLE_NAME, NAMESPACE)).thenReturn(datasetDto);
    dataSourceDao = mock(DataSourceManager.class);
    final DataSourceDTO datasourceDto = new DataSourceDTO();
    when(dataSourceDao.findUniqueByNameAndNamespace(DATASOURCE_1, NAMESPACE))
        .thenReturn(datasourceDto);
    minMaxTimeLoader = mock(MinMaxTimeLoader.class);
    when(minMaxTimeLoader.fetchMinTimeAsync(eq(datasourceDto), eq(datasetDto), any())).thenReturn(new FutureMinTime(
        JANUARY_1_2022));
  }

  @BeforeMethod
  public void initSpec() {
    spec = new ColdStartPostProcessorSpec()
        .setTableName(TABLE_NAME)
        .setDatasetConfigManager(datasetDao)
        .setDataSourceManager(dataSourceDao)
        .setNamespace(NAMESPACE)
        .setMinMaxTimeLoader(minMaxTimeLoader);
  }

  @Test
  public void testPostProcess28DaysColdStart() throws Exception {
    spec.setColdStartPeriod("P28D").setIgnore(true);
    final ColdStartPostProcessor postProcessor = new ColdStartPostProcessor(spec);

    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(anomalyJan3Jan5(), anomalyFeb2Feb3()))
        .build();
    final OperatorResult res2 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(anomalyJan27Jan31()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1, RES_2_KEY, res2);
    postProcessor.postProcess(UTC_DETECTION_INTERVAL, resultMap);

    assertThat(resultMap).hasSize(2);
    final List<AnomalyDTO> res1Anomalies = resultMap.get(RES_1_KEY).getAnomalies();
    assertThat(res1Anomalies).hasSize(2);
    final AnomalyDTO firstAnomaly = res1Anomalies.get(0);
    // first anomaly is in cold start zone
    assertThat(firstAnomaly.getAnomalyLabels()).hasSize(1);
    final AnomalyLabelDTO label = firstAnomaly.getAnomalyLabels().get(0);
    assertThat(label.getName()).isEqualTo(labelName(Period.days(28)));
    assertThat(label.isIgnore()).isTrue();
    // second anomaly is out of cold start zone
    final AnomalyDTO secondAnomaly = res1Anomalies.get(1);
    assertThat(secondAnomaly.getAnomalyLabels()).isNull();

    final List<AnomalyDTO> res2Anomalies = resultMap.get(RES_2_KEY).getAnomalies();
    assertThat(res2Anomalies).hasSize(1);
    final AnomalyDTO res2SingleAnomaly = res2Anomalies.get(0);
    // first anomaly is in cold start zone
    assertThat(res2SingleAnomaly.getAnomalyLabels()).hasSize(1);
    final AnomalyLabelDTO res2Label = res2SingleAnomaly.getAnomalyLabels().get(0);
    assertThat(res2Label.getName()).isEqualTo(labelName(Period.days(28)));
    assertThat(res2Label.isIgnore()).isTrue();
  }

  @Test
  public void testPostProcess28DaysColdStartIgnoreFalse() throws Exception {
    spec.setColdStartPeriod("P28D").setIgnore(false);
    final ColdStartPostProcessor postProcessor = new ColdStartPostProcessor(spec);

    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(anomalyJan3Jan5()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);
    postProcessor.postProcess(UTC_DETECTION_INTERVAL, resultMap);

    final List<AnomalyDTO> res1Anomalies = resultMap.get(RES_1_KEY).getAnomalies();
    final AnomalyDTO firstAnomaly = res1Anomalies.get(0);
    final AnomalyLabelDTO label = firstAnomaly.getAnomalyLabels().get(0);
    assertThat(label.isIgnore()).isFalse();
  }

  @Test
  public void testPostProcess28DaysColdStartAnomaliesWithExistingLabels() throws Exception {
    // check existing labels are not overridden
    // fixme cyril just test LabelUtils$addLabels
    spec.setColdStartPeriod("P28D").setIgnore(true);
    final ColdStartPostProcessor postProcessor = new ColdStartPostProcessor(spec);

    final List<AnomalyLabelDTO> anomalyLabels = new ArrayList<>();
    anomalyLabels.add(new AnomalyLabelDTO().setName("existingLabel"));
    final AnomalyDTO anomalyWithExistingLabel = anomalyJan3Jan5().setAnomalyLabels(
        anomalyLabels);

    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(anomalyWithExistingLabel))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);
    postProcessor.postProcess(UTC_DETECTION_INTERVAL, resultMap);

    final List<AnomalyDTO> res1Anomalies = resultMap.get(RES_1_KEY).getAnomalies();
    final AnomalyDTO firstAnomaly = res1Anomalies.get(0);
    final List<AnomalyLabelDTO> labels = firstAnomaly.getAnomalyLabels();
    assertThat(labels).hasSize(2);
    final AnomalyLabelDTO label1 = labels.get(0);
    assertThat(label1.getName()).isEqualTo("existingLabel");
    final AnomalyLabelDTO label2 = labels.get(1);
    assertThat(label2.getName()).isEqualTo(labelName(Period.days(28)));
  }

  @Test
  public void testPostProcessWithOperatorResultThatThrowsErrorAtGetAnomalies() throws Exception {
    spec.setColdStartPeriod("P28D").setIgnore(true);
    final ColdStartPostProcessor postProcessor = new ColdStartPostProcessor(spec);
    final OperatorResult res1 = SimpleDataTable.fromDataFrame(
        new DataFrame().addSeries("col1", DoubleSeries.empty()));
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);
    postProcessor.postProcess(UTC_DETECTION_INTERVAL, resultMap);
    // not failing is ok for test
  }

  private AnomalyDTO anomalyJan3Jan5() {
    return new AnomalyDTO().setStartTime(JANUARY_3_2022).setEndTime(JANUARY_5_2022);
  }

  private AnomalyDTO anomalyJan27Jan31() {
    return new AnomalyDTO().setStartTime(JANUARY_27_2022).setEndTime(JANUARY_31_2022);
  }

  private AnomalyDTO anomalyFeb2Feb3() {
    return new AnomalyDTO().setStartTime(FEBRUARY_2_2022).setEndTime(FEBRUARY_3_2022);
  }

  private static final class FutureMinTime implements Future<Long> {

    private final Long time;

    public FutureMinTime(final Long time) {
      this.time = time;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Long get() {
      return time;
    }

    @Override
    public Long get(final long timeout, final @NonNull TimeUnit unit) {
      return get();
    }
  }
}
