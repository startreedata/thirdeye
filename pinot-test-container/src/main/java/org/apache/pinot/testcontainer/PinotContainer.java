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
package org.apache.pinot.testcontainer;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class PinotContainer extends GenericContainer<PinotContainer> {

    public static final Logger log = LoggerFactory.getLogger(PinotContainer.class);

    public static final int DEFAULT_ZOOKEEPER_PORT = 2123;
    public static final int DEFAULT_CONTROLLER_HTTP_PORT = 9000;
    public static final int DEFAULT_BROKER_HTTP_PORT = 8000;


    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("apachepinot/pinot");
    private static final String DEFAULT_TAG = "0.9.3-jdk11";
    private List<AddTable> addTables;
    private List<ImportData> importDataList;
    private Network network;

    public PinotContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    public PinotContainer(String pinotVersion) {
        this(DEFAULT_IMAGE_NAME.withTag(pinotVersion));
    }

    public PinotContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        withExposedPorts(DEFAULT_ZOOKEEPER_PORT, DEFAULT_CONTROLLER_HTTP_PORT, DEFAULT_BROKER_HTTP_PORT);
    }

    public PinotContainer(List<AddTable> addTables, List<ImportData> importDataList) {
        this();
        this.addTables = addTables;
        this.importDataList = importDataList;
    }

    @Override
    protected void configure() {
        super.configure();

        if(this.addTables != null) {
            for (AddTable addTable : this.addTables) {
                withCopyFileToContainer(MountableFile.forHostPath(addTable.getSchemaFile().getAbsolutePath()), "/tmp/");
                withCopyFileToContainer(MountableFile.forHostPath(addTable.getTableConfigFile().getAbsolutePath()), "/tmp/");
            }
        }

        if(this.importDataList != null) {
            for (ImportData importData : this.importDataList) {
                withCopyFileToContainer(MountableFile.forHostPath(importData.getBatchJobSpecFile().getAbsolutePath()), "/tmp/");
                withCopyFileToContainer(MountableFile.forHostPath(importData.getDataFile().getAbsolutePath()), "/tmp/");
            }
        }

        withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("pinot-quickstart-test"));
        withCommand("QuickStart", "-type", "batch");
        waitingFor(
            new WaitAllStrategy()
                .withStartupTimeout(Duration.ofMinutes(5))
                .withStrategy(waitStrategy)
                .withStrategy(Wait.forLogMessage(".*Bootstrap baseballStats table.*", 1))
        );
    }

    public void addTables() throws IOException, InterruptedException {
        for(AddTable table: this.addTables) {
            Container.ExecResult addTableResult = this.execInContainer(
                "/opt/pinot/bin/pinot-admin.sh",
                "AddTable",
                "-tableConfigFile", "/tmp/"+ table.getTableConfigFile().getName(),
                "-schemaFile", "/tmp/"+ table.getSchemaFile().getName(),
                "-controllerHost", "localhost",
                "-controllerPort", "9000",
                "-exec"
            );
            String stdout = addTableResult.getStdout();
            log.info(stdout);
        }
        for(ImportData importData: this.importDataList) {
            Container.ExecResult launchDataIngestionJobResult = this.execInContainer(
                    "/opt/pinot/bin/pinot-admin.sh",
                    "LaunchDataIngestionJob",
                    "-jobSpecFile", "/tmp/" + importData.getBatchJobSpecFile().getName(),
                    "-values", "-controllerHost", "localhost",
                "-controllerPort", "9000"
            );
            String stdout = launchDataIngestionJobResult.getStdout();
            log.info(stdout);
        }
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
