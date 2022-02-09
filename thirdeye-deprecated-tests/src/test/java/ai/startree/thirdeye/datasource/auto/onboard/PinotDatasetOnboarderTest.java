/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.startree.thirdeye.datasource.auto.onboard;

import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.pinot.PinotThirdEyeDataSource;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.metric.MetricType;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.spi.data.DateTimeFormatSpec;
import org.apache.pinot.spi.data.DimensionFieldSpec;
import org.apache.pinot.spi.data.FieldSpec;
import org.apache.pinot.spi.data.MetricFieldSpec;
import org.apache.pinot.spi.data.Schema;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
  public void testAddNewDataset() throws Exception {
    Assert.assertEquals(datasetConfigDAO.findAll().size(), 1);
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findByDataset(dataset);
    Assert.assertEquals(datasetConfig.getDataset(), dataset);
    Assert.assertEquals(datasetConfig.getDimensions(), schema.getDimensionNames());
    Assert.assertEquals(datasetConfig.getTimeColumn(), oldTimeColumnName);
    DateTimeFieldSpec dateTimeFieldSpec = schema.getSpecForTimeColumn(oldTimeColumnName);
    DateTimeFormatSpec formatSpec = new DateTimeFormatSpec(dateTimeFieldSpec.getFormat());
    Assert
        .assertEquals(datasetConfig.bucketTimeGranularity().getUnit(), formatSpec.getColumnUnit());
    Assert
        .assertEquals(datasetConfig.bucketTimeGranularity().getSize(), formatSpec.getColumnSize());
    Assert.assertEquals(datasetConfig.getTimeFormat(), "EPOCH");
    Assert.assertEquals(datasetConfig.getTimezone(), "US/Pacific");
    Assert.assertEquals(datasetConfig.getExpectedDelay().getUnit(), TimeUnit.HOURS);

    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findByDataset(dataset);
    List<String> schemaMetricNames = schema.getMetricNames();
    List<Long> metricIds = new ArrayList<>();
    Assert.assertEquals(metricConfigs.size(), schemaMetricNames.size());
    for (MetricConfigDTO metricConfig : metricConfigs) {
      Assert.assertTrue(schemaMetricNames.contains(metricConfig.getName()));
      metricIds.add(metricConfig.getId());
      if (metricConfig.getName().equals("latency_tdigest")) {
        Assert.assertEquals(metricConfig.getDefaultAggFunction(),
            MetricConfigDTO.DEFAULT_TDIGEST_AGG_FUNCTION);
        Assert.assertEquals(metricConfig.getDatatype(), MetricType.DOUBLE);
      } else {
        Assert.assertEquals(metricConfig.getDefaultAggFunction(),
            MetricConfigDTO.DEFAULT_AGG_FUNCTION);
      }
    }
  }

  @Test(dependsOnMethods = {"testAddNewDataset"})
  public void testRefreshDataset() throws Exception {
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
    Assert.assertEquals(datasetConfigDAO.findAll().size(), 1);
    DatasetConfigDTO newDatasetConfig1 = datasetConfigDAO.findByDataset(dataset);
    Assert.assertEquals(newDatasetConfig1.getDataset(), dataset);
    Assert.assertEquals(Sets.newHashSet(newDatasetConfig1.getDimensions()),
        Sets.newHashSet(schema.getDimensionNames()));
    Assert.assertEquals(newDatasetConfig1.getProperties(), pinotCustomConfigs);

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

    Assert.assertEquals(datasetConfigDAO.findAll().size(), 1);
    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findByDataset(dataset);
    List<String> schemaMetricNames = schema.getMetricNames();
    List<Long> metricIds = new ArrayList<>();
    Assert.assertEquals(metricConfigs.size(), schemaMetricNames.size());
    for (MetricConfigDTO metricConfig : metricConfigs) {
      Assert.assertTrue(schemaMetricNames.contains(metricConfig.getName()));
      metricIds.add(metricConfig.getId());
    }

    // Get the updated dataset config and check custom configs
    datasetConfig = datasetConfigDAO.findByDataset(dataset);
    Map<String, String> datasetCustomConfigs = datasetConfig.getProperties();
    for (Map.Entry<String, String> pinotCustomCnofig : pinotCustomConfigs.entrySet()) {
      String configKey = pinotCustomCnofig.getKey();
      String configValue = pinotCustomCnofig.getValue();
      Assert.assertTrue(datasetCustomConfigs.containsKey(configKey));
      Assert.assertEquals(datasetCustomConfigs.get(configKey), configValue);
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
    Assert.assertEquals(datasetConfigDAO.findAll().size(), 1);
    datasetConfig = datasetConfigDAO.findByDataset(dataset);
    Assert.assertEquals(datasetConfig.bucketTimeGranularity().getUnit(), TimeUnit.MINUTES);
    Assert.assertEquals(datasetConfig.bucketTimeGranularity().getSize(), 5);
    Assert.assertEquals(datasetConfig.getTimeUnit(), TimeUnit.MILLISECONDS);
    Assert.assertEquals(datasetConfig.getTimeDuration().intValue(), 1);
    Assert.assertEquals(datasetConfig.getTimeFormat(), "EPOCH");
    Assert.assertEquals(datasetConfig.getTimezone(), "US/Pacific");
    Assert.assertEquals(datasetConfig.getExpectedDelay().getUnit(), TimeUnit.HOURS);
  }

  @Test(dependsOnMethods = {"testRefreshDataset"})
  public void testDeactivate() throws Exception {
    Assert.assertEquals(datasetConfigDAO.findAll().size(), 1);
    pinotDatasetOnboarder.deactivateDatasets(Collections.emptyList(), DATA_SOURCE_NAME);
    List<DatasetConfigDTO> datasets = datasetConfigDAO.findAll();
    Assert.assertEquals(datasets.size(), 1);
    Assert.assertFalse(datasets.get(0).isActive());
  }
}
