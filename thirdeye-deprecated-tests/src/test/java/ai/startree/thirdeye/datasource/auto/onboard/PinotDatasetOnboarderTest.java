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
package ai.startree.thirdeye.datasource.auto.onboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.plugins.datasource.auto.onboard.ConfigGenerator;
import ai.startree.thirdeye.plugins.datasource.auto.onboard.PinotDatasetOnboarder;
import ai.startree.thirdeye.plugins.datasource.auto.onboard.ThirdEyePinotClient;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSource;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricType;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.spi.data.DimensionFieldSpec;
import org.apache.pinot.spi.data.FieldSpec;
import org.apache.pinot.spi.data.MetricFieldSpec;
import org.apache.pinot.spi.data.Schema;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// todo cyril the tests here are not deprecated - but putting them in pinot plugins requires refactoring
public class PinotDatasetOnboarderTest {

  public static final String DATA_SOURCE_NAME = PinotThirdEyeDataSource.class.getSimpleName();
  private final String dataset = "test-collection";
  private final String oldTimeColumnName = "time";
  private final String newTimeColumnName = "timestampInEpoch";

  private PinotDatasetOnboarder pinotDatasetOnboarder;
  private Schema schema;

  private TestDbEnv testDAOProvider;
  private DatasetConfigManager datasetConfigDAO;
  private MetricConfigManager metricConfigDAO;

  @BeforeMethod
  void beforeMethod() throws Exception {
    testDAOProvider = new TestDbEnv();
    final Injector injector = testDAOProvider.getInjector();
    datasetConfigDAO = injector.getInstance(DatasetConfigManager.class);
    metricConfigDAO = injector.getInstance(MetricConfigManager.class);

    pinotDatasetOnboarder = new PinotDatasetOnboarder(mock(ThirdEyePinotClient.class),
        datasetConfigDAO,
        metricConfigDAO);
    schema = Schema
        .fromInputSteam(ClassLoader.getSystemResourceAsStream("sample-pinot-schema.json"));
    Map<String, String> pinotCustomConfigs = new HashMap<>();
    pinotCustomConfigs.put("configKey1", "configValue1");
    pinotCustomConfigs.put("configKey2", "configValue2");
    pinotDatasetOnboarder.addPinotDataset(dataset,
        schema,
        oldTimeColumnName,
        pinotCustomConfigs,
        null,
        DATA_SOURCE_NAME);
  }

  @AfterMethod(alwaysRun = true)
  void afterMethod() {
    testDAOProvider.cleanup();
  }

  @Test
  public void testAddNewDataset() {
    Assert.assertEquals(datasetConfigDAO.findAll().size(), 1);
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findByDataset(dataset);
    assertThat(datasetConfig.getDataset()).isEqualTo(dataset);
    assertThat(datasetConfig.getDimensions()).isEqualTo(schema.getDimensionNames());
    assertThat(datasetConfig.getTimeColumn()).isEqualTo(oldTimeColumnName);
    assertThat(datasetConfig.getTimeFormat()).isEqualTo("EPOCH");
    assertThat(datasetConfig.getTimezone()).isEqualTo(Constants.DEFAULT_TIMEZONE_STRING);
    assertThat(datasetConfig.getActive()).isTrue();

    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findByDataset(dataset);
    List<String> schemaMetricNames = schema.getMetricNames();
    // fixme cyril
    List<Long> metricIds = new ArrayList<>();
    assertThat(metricConfigs.size()).isEqualTo(schemaMetricNames.size());
    for (MetricConfigDTO metricConfig : metricConfigs) {
      assertThat(schemaMetricNames.contains(metricConfig.getName())).isTrue();
      metricIds.add(metricConfig.getId());
      if (metricConfig.getName().equals("latency_tdigest")) {
        assertThat(metricConfig.getDefaultAggFunction()).isEqualTo(ConfigGenerator.DEFAULT_TDIGEST_AGG_FUNCTION.toString());
        assertThat(metricConfig.getDatatype()).isEqualTo(MetricType.DOUBLE);
      } else {
        assertThat(metricConfig.getDefaultAggFunction()).isEqualTo(ConfigGenerator.DEFAULT_AGG_FUNCTION.toString());
      }
    }
  }

  @Test(dependsOnMethods = {"testAddNewDataset"})
  public void testRefreshDataset() {
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findByDataset(dataset);
    DimensionFieldSpec dimensionFieldSpec = new DimensionFieldSpec("newDimension",
        FieldSpec.DataType.STRING, true);
    schema.addField(dimensionFieldSpec);
    Map<String, String> pinotCustomConfigs = new HashMap<>();
    pinotCustomConfigs.put("configKey1", "configValue1");
    pinotCustomConfigs.put("configKey2", "configValue2");
    pinotDatasetOnboarder.addPinotDataset(dataset,
        schema,
        oldTimeColumnName,
        new HashMap<>(pinotCustomConfigs),
        datasetConfig,
        DATA_SOURCE_NAME);
    assertThat(datasetConfigDAO.findAll().size()).isEqualTo(1);
    DatasetConfigDTO newDatasetConfig1 = datasetConfigDAO.findByDataset(dataset);
    assertThat(newDatasetConfig1.getDataset()).isEqualTo(dataset);
    assertThat(Sets.newHashSet(newDatasetConfig1.getDimensions())).isEqualTo(Sets.newHashSet(schema.getDimensionNames()));
    assertThat(newDatasetConfig1.getProperties()).isEqualTo(pinotCustomConfigs);
    assertThat(newDatasetConfig1.getActive()).isTrue();

    MetricFieldSpec metricFieldSpec = new MetricFieldSpec("newMetric", FieldSpec.DataType.LONG);
    schema.addField(metricFieldSpec);
    pinotCustomConfigs.put("configKey3", "configValue3");
    pinotCustomConfigs.remove("configKey2");
    pinotDatasetOnboarder.addPinotDataset(dataset,
        schema,
        oldTimeColumnName,
        new HashMap<>(pinotCustomConfigs),
        newDatasetConfig1,
        DATA_SOURCE_NAME);

    assertThat(datasetConfigDAO.findAll().size()).isEqualTo(1);
    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findByDataset(dataset);
    List<String> schemaMetricNames = schema.getMetricNames();
    List<Long> metricIds = new ArrayList<>();
    assertThat(metricConfigs.size()).isEqualTo(schemaMetricNames.size());
    for (MetricConfigDTO metricConfig : metricConfigs) {
      assertThat(schemaMetricNames.contains(metricConfig.getName())).isTrue();
      metricIds.add(metricConfig.getId());
    }

    // Get the updated dataset config and check custom configs
    datasetConfig = datasetConfigDAO.findByDataset(dataset);
    Map<String, String> datasetCustomConfigs = datasetConfig.getProperties();
    for (Map.Entry<String, String> pinotCustomCnofig : pinotCustomConfigs.entrySet()) {
      String configKey = pinotCustomCnofig.getKey();
      String configValue = pinotCustomCnofig.getValue();
      assertThat(datasetCustomConfigs.containsKey(configKey)).isTrue();
      assertThat(datasetCustomConfigs.get(configKey)).isEqualTo(configValue);
    }

    DateTimeFieldSpec dateTimeFieldSpec = new DateTimeFieldSpec(newTimeColumnName,
        FieldSpec.DataType.LONG, "1:MILLISECONDS:EPOCH", "1:MILLISECONDS");
    schema.removeField(oldTimeColumnName);
    schema.addField(dateTimeFieldSpec);
    pinotDatasetOnboarder.addPinotDataset(dataset,
        schema,
        newTimeColumnName,
        new HashMap<>(pinotCustomConfigs),
        newDatasetConfig1,
        DATA_SOURCE_NAME);
    assertThat(datasetConfigDAO.findAll().size()).isEqualTo(1);
    datasetConfig = datasetConfigDAO.findByDataset(dataset);
    assertThat(datasetConfig.getTimeFormat()).isEqualTo("EPOCH");
    assertThat(datasetConfig.getTimezone()).isEqualTo(Constants.DEFAULT_TIMEZONE_STRING);
  }

  @Test(dependsOnMethods = {"testRefreshDataset"})
  public void testDeactivate() {
    assertThat(datasetConfigDAO.findAll().size()).isEqualTo(1);
    pinotDatasetOnboarder.deactivateDatasets(Collections.emptyList(), DATA_SOURCE_NAME);
    List<DatasetConfigDTO> datasets = datasetConfigDAO.findAll();
    assertThat(datasets.size()).isEqualTo(1);
    assertThat(datasets.get(0).getActive()).isFalse();
  }
}
