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
package ai.startree.thirdeye;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.api.DataSourceApi;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.testcontainer.AddTable;
import org.apache.pinot.testcontainer.ImportData;
import org.apache.pinot.testcontainer.PinotContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests that need Pinot can use this shared instance.
 */
public class PinotDataSourceManager {

  public static final String PINOT_DATA_SOURCE_NAME = "PinotContainer";
  public static final String PINOT_DATASET_NAME = "pageviews";
  public static final String PINOT_DATA_SOURCE_TYPE = "pinot";

  private static final Logger LOG = LoggerFactory.getLogger(PinotDataSourceManager.class);

  private static final String INGESTION_JOB_SPEC_FILENAME = "batch-job-spec.yml";
  private static final String SCHEMA_FILENAME = "schema.json";
  private static final String TABLE_CONFIG_FILENAME = "table-config.json";
  private static final String DATA_FILENAME = "data.csv";
  private static PinotContainer instance;

  private PinotDataSourceManager() {
  }

  private synchronized static PinotContainer getInstance() {
    if (instance == null) {
      instance = createPinotContainer();
      try {
        instance.start();
        instance.addTables();
      } catch (final IOException | InterruptedException e) {
        throw new RuntimeException("Could not launch Pinot for integration tests.");
      }
    }

    return instance;
  }

  private static PinotContainer createPinotContainer() {
    final URL datasetsBaseResource = PinotDataSourceManager.class.getResource("/datasets");
    requireNonNull(datasetsBaseResource);

    final String datasetsBasePath = datasetsBaseResource.getFile();
    final File[] directories = new File(datasetsBasePath).listFiles(File::isDirectory);
    final List<AddTable> addTableList = new ArrayList<>();
    final List<ImportData> importDataList = new ArrayList<>();
    for (final File dir : requireNonNull(directories, "directories is null")) {
      final String tableName = dir.getName();
      final File schemaFile = Paths.get(datasetsBasePath, tableName, SCHEMA_FILENAME).toFile();
      final File tableConfigFile = Paths.get(datasetsBasePath, tableName, TABLE_CONFIG_FILENAME)
          .toFile();
      addTableList.add(new AddTable(schemaFile, tableConfigFile));

      final File batchJobSpecFile = Paths.get(datasetsBasePath,
              tableName,
              INGESTION_JOB_SPEC_FILENAME)
          .toFile();
      final File dataFile = Paths.get(datasetsBasePath, tableName, DATA_FILENAME).toFile();
      importDataList.add(new ImportData(batchJobSpecFile, dataFile));
    }
    return new PinotContainer(addTableList, importDataList);
  }

  public static synchronized DataSourceApi getPinotDataSourceApi() {
    final String property = System.getProperty("thirdeye.test.useLocalPinotInstance");
    if (property != null) {
      LOG.warn("Using local pinot instance for testing!");
      return localPinotDataSourceApi();
    }
    /* Create the pinot instance if required */
    final PinotContainer instance = getInstance();

    return getPinotDataSourceApi(instance);
  }

  private static DataSourceApi localPinotDataSourceApi() {
    return new DataSourceApi().setName(PINOT_DATA_SOURCE_NAME)
        .setType(PINOT_DATA_SOURCE_TYPE)
        .setProperties(ImmutableMap.<String, Object>builder()
            .put("zookeeperUrl", "localhost:2123")
            .put("clusterName", "QuickStartCluster")
            .put("controllerConnectionScheme", "http")
            .put("controllerHost", "localhost")
            .put("controllerPort", "9000")
            .build()
        );
  }

  private static synchronized DataSourceApi getPinotDataSourceApi(PinotContainer pinotContainer) {
    return new DataSourceApi().setName(PINOT_DATA_SOURCE_NAME)
        .setType(PINOT_DATA_SOURCE_TYPE)
        .setProperties(ImmutableMap.<String, Object>builder()
            .put("zookeeperUrl", "localhost:" + pinotContainer.getZookeeperPort())
            .put("brokerUrl", pinotContainer.getPinotBrokerUrl().replace("http://", ""))
            .put("clusterName", "QuickStartCluster")
            .put("controllerConnectionScheme", "http")
            .put("controllerHost", "localhost")
            .put("controllerPort", pinotContainer.getControllerPort())
            .build()
        );
  }
}
