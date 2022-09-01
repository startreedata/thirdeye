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
package ai.startree.thirdeye;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.testcontainer.AddTable;
import org.apache.pinot.testcontainer.ImportData;
import org.apache.pinot.testcontainer.PinotContainer;

/**
 * Integration tests that need Pinot can use this shared instance.
 */
public class PinotContainerManager {

  public static final String PINOT_DATA_SOURCE_NAME = "PinotContainer";
  public static final String PINOT_DATASET_NAME = "pageviews";
  private static final String INGESTION_JOB_SPEC_FILENAME = "batch-job-spec.yml";
  private static final String SCHEMA_FILENAME = "schema.json";
  private static final String TABLE_CONFIG_FILENAME = "table-config.json";
  private static final String DATA_FILENAME = "data.csv";

  private static final PinotContainerManager instance = createInstance();
  private final PinotContainer pinotContainer;

  private PinotContainerManager(final PinotContainer pinotContainer) {
    this.pinotContainer = pinotContainer;
  }

  private synchronized static PinotContainerManager createInstance() {
    final PinotContainer pinotContainer = createPinotContainer();

    try {
      pinotContainer.start();
      pinotContainer.addTables();
    } catch (final IOException | InterruptedException e) {
      throw new RuntimeException("Could not launch Pinot for integration tests.");
    }
    return new PinotContainerManager(pinotContainer);
  }

  public static PinotContainerManager getInstance() {
    return instance;
  }

  private static PinotContainer createPinotContainer() {
    final URL datasetsBaseResource = PinotContainerManager.class.getClassLoader()
        .getResource("datasets");
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

  public PinotContainer getPinotContainer() {
    return pinotContainer;
  }
}
