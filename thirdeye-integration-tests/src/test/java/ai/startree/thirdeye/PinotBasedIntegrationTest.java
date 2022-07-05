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
 * */
public abstract class PinotBasedIntegrationTest {

  private static final String INGESTION_JOB_SPEC_FILENAME = "batch-job-spec.yml";
  private static final String SCHEMA_FILENAME = "schema.json";
  private static final String TABLE_CONFIG_FILENAME = "table-config.json";
  private static final String DATA_FILENAME = "data.csv";

  protected static final String PINOT_DATA_SOURCE_NAME = "PinotContainer";
  protected static final String PINOT_DATASET_NAME = "pageviews";

  protected static PinotContainer pinotContainer;

  static {
    pinotContainer = startPinot();
    try {
      pinotContainer.addTables();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      throw new RuntimeException("Could not launch Pinot for integration tests.");
    }
  }


  private static PinotContainer startPinot() {
    URL datasetsBaseResource = PinotBasedIntegrationTest.class.getClassLoader().getResource("datasets");
    com.google.common.base.Preconditions.checkNotNull(datasetsBaseResource);

    final String datasetsBasePath = datasetsBaseResource.getFile();
    File[] directories = new File(datasetsBasePath).listFiles(File::isDirectory);
    List<AddTable> addTableList = new ArrayList<>();
    List<ImportData> importDataList = new ArrayList<>();
    for (File dir : directories) {
      String tableName = dir.getName();
      File schemaFile = Paths.get(datasetsBasePath, tableName, SCHEMA_FILENAME).toFile();
      File tableConfigFile = Paths.get(datasetsBasePath, tableName, TABLE_CONFIG_FILENAME).toFile();
      addTableList.add(new AddTable(schemaFile, tableConfigFile));

      File batchJobSpecFile = Paths.get(datasetsBasePath, tableName, INGESTION_JOB_SPEC_FILENAME)
          .toFile();
      File dataFile = Paths.get(datasetsBasePath, tableName, DATA_FILENAME).toFile();
      importDataList.add(new ImportData(batchJobSpecFile, dataFile));
    }
    pinotContainer = new PinotContainer(addTableList, importDataList);
    pinotContainer.start();

    return pinotContainer;
  }
}
