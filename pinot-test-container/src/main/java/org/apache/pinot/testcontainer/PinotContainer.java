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
package org.apache.pinot.testcontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class PinotContainer extends GenericContainer<PinotContainer> {

  public static final Logger LOG = LoggerFactory.getLogger(PinotContainer.class);

  public static final int DEFAULT_ZOOKEEPER_PORT = 2123;
  public static final int DEFAULT_CONTROLLER_HTTP_PORT = 9000;
  public static final int DEFAULT_BROKER_HTTP_PORT = 8000;
  private final DockerImageName dockerImageName;

  public enum PinotVersion {
    v0_11_1("0.11.0", "0.11.0-arm64"),
    v0_12_1("0.12.1", "0.12.1"),
    v1_0_0("1.0.0", "1.0.0"),
    v1_1_0("1.1.0", "1.1.0"),
    v1_2_0("1.2.0", "1.2.0");
    
    private final String amdTag;
    private final String armTag;
    
    PinotVersion(final String amdTag, final String armTag) {
      this.amdTag = amdTag;
      this.armTag = armTag;
    }
    
    public String getTag() {
      if (hostArch().endsWith("arm64")) {
        return armTag;
      }
      return amdTag;
    }
    
    // if the test does not run for every supported Pinot version, use this version
    public static PinotVersion recommendedVersion() {
      return v1_2_0;
    }
  }
  
  private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(
      "apachepinot/pinot");
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private List<AddTable> addTables;
  private List<ImportData> importDataList;

  // Return the cpu arch for the Docker host.
  private static String hostArch() {
    if (System.getProperty("os.name").equals("Mac OS X")) {
      // Java 11 for M1 cpu virtualizes the x86_64 arch, so java and uname reports os.arch as x86_64.
      // Use sysctl to get cpu brand and infer the arch.
      final ProcessBuilder processBuilder = new ProcessBuilder("sysctl", "-n",
          "machdep.cpu.brand_string");
      try {
        final Process process = processBuilder.start();
        final InputStreamReader inputStream = new InputStreamReader(process.getInputStream());
        final BufferedReader reader = new BufferedReader(inputStream);
        try (inputStream; reader) {
          final String cpuBrand = reader.readLine().trim();
          if (cpuBrand.startsWith("Apple M")) {
            return "arm64";
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return System.getProperty("os.arch");
  }

  private PinotContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
    dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
    withExposedPorts(DEFAULT_ZOOKEEPER_PORT, DEFAULT_CONTROLLER_HTTP_PORT,
        DEFAULT_BROKER_HTTP_PORT);
    this.dockerImageName = dockerImageName;
  }

  public PinotContainer(final PinotVersion pinotVersion, final List<AddTable> addTables, final List<ImportData> importDataList) {
    this(DEFAULT_IMAGE_NAME.withTag(pinotVersion.getTag()));
    this.addTables = addTables;
    this.importDataList = importDataList;
  }

  @Override
  protected void configure() {
    super.configure();

    if (this.addTables != null) {
      for (AddTable addTable : this.addTables) {
        withCopyFileToContainer(
            MountableFile.forHostPath(addTable.getSchemaFile().getAbsolutePath()),
            "/tmp/" + addTable.getSchemaFile().getName());
        withCopyFileToContainer(
            MountableFile.forHostPath(addTable.getTableConfigFile().getAbsolutePath()),
            "/tmp/" + addTable.getTableConfigFile().getName());
      }
    }

    if (this.importDataList != null) {
      for (ImportData importData : this.importDataList) {
        withCopyFileToContainer(
            MountableFile.forHostPath(importData.getBatchJobSpecFile().getAbsolutePath()),
            "/tmp/" + importData.getBatchJobSpecFile().getName());
        withCopyFileToContainer(
            MountableFile.forHostPath(importData.getDataFile().getAbsolutePath()),
            "/tmp/" + importData.getDataFile().getName());
      }
    }

    withCreateContainerCmdModifier(
        createContainerCmd -> createContainerCmd.withName("pinot-quickstart-test-" + dockerImageName.getVersionPart()));
    withCommand("QuickStart", "-type", "EMPTY");
    waitingFor(
        new WaitAllStrategy()
            .withStartupTimeout(Duration.ofMinutes(5))
            .withStrategy(waitStrategy)
            .withStrategy(Wait.forLogMessage(".*Offline quickstart setup complete.*", 1))
    );
  }

  public void addTables() throws IOException, InterruptedException {
    for (AddTable table : this.addTables) {
      final Container.ExecResult addTableResult = this.execInContainer(
          "/opt/pinot/bin/pinot-admin.sh",
          "AddTable",
          "-tableConfigFile", "/tmp/" + table.getTableConfigFile().getName(),
          "-schemaFile", "/tmp/" + table.getSchemaFile().getName(),
          "-controllerHost", "localhost",
          "-controllerPort", "9000",
          "-exec"
      );
      final String stdout = addTableResult.getStdout();
      LOG.info(stdout);
    }
    for (ImportData importData : this.importDataList) {
      final Container.ExecResult launchDataIngestionJobResult = this.execInContainer(
          "/opt/pinot/bin/pinot-admin.sh",
          "LaunchDataIngestionJob",
          "-jobSpecFile", "/tmp/" + importData.getBatchJobSpecFile().getName(),
          "-values", "-controllerHost", "localhost",
          "-controllerPort", "9000"
      );
      final String stdout = launchDataIngestionJobResult.getStdout();
      LOG.info(stdout);
    }
    for (AddTable table : this.addTables) {
      final String tableName = findTableName(table);
      long tableSize = getTableSize(tableName);
      while (tableSize == 0) {
        LOG.info("Waiting for {} ingestion to be complete.", tableName);
        Thread.sleep(1000);
        tableSize = getTableSize(tableName);
      }
    }
  }

  private long getTableSize(final String tableName) throws IOException, InterruptedException {
    final ExecResult checkIngestionIsFinished = this.execInContainer(
        "curl",
        "-X",
        "GET",
        String.format("http://localhost:9000/tables/%s_OFFLINE/size?detailed=false", tableName)
    );
    final String stdout = checkIngestionIsFinished.getStdout();
    return OBJECT_MAPPER.readTree(stdout).get("estimatedSizeInBytes").longValue();
  }

  private String findTableName(final AddTable table) throws IOException {
    // assumes the tableName is the same as the schema name
    return OBJECT_MAPPER.readTree(table.getSchemaFile()).get("schemaName").asText();
  }

  public String getPinotBrokerUrl() {
    return String.format("http://%s:%s", getHost(), getBrokerPort());
  }

  public Integer getBrokerPort() {
    return getMappedPort(DEFAULT_BROKER_HTTP_PORT);
  }

  public Integer getControllerPort() {
    return getMappedPort(DEFAULT_CONTROLLER_HTTP_PORT);
  }

  public Integer getZookeeperPort() {
    return getMappedPort(DEFAULT_ZOOKEEPER_PORT);
  }
}
