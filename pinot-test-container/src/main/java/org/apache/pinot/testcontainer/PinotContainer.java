/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
    private static final String DEFAULT_TAG = "0.7.0-SNAPSHOT-b9b31e5d2-20210413-jdk8";
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
        dockerImageName.assertCompatibleWith(DockerImageName.parse("apachepinot/pinot"));
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
                .withStrategy(Wait.forLogMessage(".*You can always go to http://localhost:9000 to play around in the query console.*", 1))
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
                    "-values"
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
