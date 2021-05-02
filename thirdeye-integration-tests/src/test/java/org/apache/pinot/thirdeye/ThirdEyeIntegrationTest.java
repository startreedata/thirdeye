package org.apache.pinot.thirdeye;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.api.AlertNodeApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.api.TimeColumnApi;
import org.apache.pinot.thirdeye.constant.MetricAggFunction;
import org.apache.pinot.thirdeye.datalayer.pojo.AlertNodeType;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class ThirdEyeIntegrationTest {

    public static final Logger log = LoggerFactory.getLogger(ThirdEyeIntegrationTest.class);
    public static final String THIRDEYE_CONFIG = "./src/test/resources/e2e/config";
    private static final String INGESTION_JOB_SPEC_FILENAME = "batch-job-spec.yml";
    private static final String SCHEMA_FILENAME = "schema.json";
    private static final String TABLE_CONFIG_FILENAME = "table-config.json";
    private static final String DATA_FILENAME = "data.csv";
    private static final String DATASOURCES_CONFIG_FILE_PATH = "config/data-sources/data-sources-config.yml";
    private static final String PINOT_DATASOURCE_CLASS = "org.apache.pinot.thirdeye.datasource.pinot.PinotThirdEyeDataSource";

    public static final DropwizardTestSupport<ThirdEyeCoordinatorConfiguration> SUPPORT =
            new DropwizardTestSupport<>(ThirdEyeCoordinator.class,
                    resourceFilePath("e2e/config/coordinator.yaml"),
                    config("configPath", THIRDEYE_CONFIG),
                    config("server.connector.port", "0"), // port: 0 implies any port
                    config("database.url", ThirdEyeH2DatabaseServer.DB_CONFIG.getUrl()),
                    config("database.user", ThirdEyeH2DatabaseServer.DB_CONFIG.getUser()),
                    config("database.password", ThirdEyeH2DatabaseServer.DB_CONFIG.getPassword()),
                    config("database.driver", ThirdEyeH2DatabaseServer.DB_CONFIG.getDriver())
            );
    private static PinotContainer container;

    private Client client;
    private ThirdEyeH2DatabaseServer db;

    private static String thirdEyeEndPoint(final String pathFragment) {
        return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
    }

    private PinotContainer startPinot() {
        URL baseResourceForTestDatasets = getClass().getClassLoader().getResource("datasets");
        com.google.common.base.Preconditions.checkNotNull(baseResourceForTestDatasets);
        File[] directories = new File(baseResourceForTestDatasets.getFile()).listFiles(File::isDirectory);
        List<AddTable> addTableList = new ArrayList<>();
        List<ImportData> importDataList = new ArrayList<>();
        for (File dir : directories) {
            String tableName = dir.getName();

            File schemaFile = Paths.get(baseResourceForTestDatasets.getFile(), tableName, SCHEMA_FILENAME).toFile();
            File tableConfigFile = Paths.get(baseResourceForTestDatasets.getFile(), tableName, TABLE_CONFIG_FILENAME).toFile();
            addTableList.add(new AddTable(schemaFile, tableConfigFile));

            File batchJobSpecFile = Paths.get(baseResourceForTestDatasets.getFile(), tableName, INGESTION_JOB_SPEC_FILENAME).toFile();
            File dataFile = Paths.get(baseResourceForTestDatasets.getFile(), tableName, DATA_FILENAME).toFile();
            importDataList.add(new ImportData(batchJobSpecFile, dataFile));
        }
        container = new PinotContainer(addTableList, importDataList);
        container.start();
        return container;
    }

    private void modifyDataSourceConfig() throws IOException, URISyntaxException {
      //  URL e2eConfigurationResources = this.getClass().getClassLoader().getResource("e2e");
        Path resourceDirectory = Paths.get("src","test","resources", "e2e");
        File dataSourcesConfigFile = Paths.get(resourceDirectory.toFile().getAbsolutePath(), DATASOURCES_CONFIG_FILE_PATH).toFile();
        InputStream inputStream = new FileInputStream(dataSourcesConfigFile);
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml dataSourcesYamlReader = new Yaml();
        Map<String, Object> dataSourcesConfigurationMap = (Map<String, Object> ) dataSourcesYamlReader.load(inputStream);
        List<Map<String, Object>> dataSourceConfigs = (List<Map<String, Object>>)dataSourcesConfigurationMap.get("dataSourceConfigs");
        int index = findIndex(dataSourceConfigs, PINOT_DATASOURCE_CLASS);
        assert index >= 0;
        Map<String, Object> dataSourceConfig = dataSourceConfigs.get(index);
        Map<String, Object> dataSourceConfigProperties = (Map<String, Object>)dataSourceConfig.get("properties");
        dataSourceConfigProperties
                .put("zookeeperUrl", "localhost:" + container.getZookeeperPort());
        dataSourceConfigProperties
                .put("controllerPort", container.getBrokerPort());
        PrintWriter writer = new PrintWriter(dataSourcesConfigFile);
        Yaml dataSourcesYamlWriter = new Yaml(options);
        dataSourcesYamlWriter.dump(dataSourcesConfigurationMap, writer);
        inputStream.close();
        writer.close();
    }

    private int findIndex(List<Map<String, Object>> dataSourceConfigs, String expectedDataSourceClassName) {
        for(int i=0; i<dataSourceConfigs.size(); i++) {
            Map<String, Object> datasourceConfig = dataSourceConfigs.get(i);
            String actualDataSourceClassName = (String) datasourceConfig.get("className");
            if(actualDataSourceClassName.equals(expectedDataSourceClassName)) {
                return i;
            }
        }
        return -1;
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        db = new ThirdEyeH2DatabaseServer();
        db.start();
        container = startPinot();
        container.addTables();
        modifyDataSourceConfig();
        SUPPORT.before();
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTimeout(io.dropwizard.util.Duration.minutes(1)); // for timeout issues
        client = new JerseyClientBuilder(SUPPORT.getEnvironment())
                .using(jerseyClientConfiguration)
                .build("test client");
    }

    @AfterClass
    public void afterClass() throws Exception {
        log.info("Pinot container port: {}", container.getPinotBrokerUrl());
        log.info("Thirdeye port: {}", SUPPORT.getLocalPort());
        SUPPORT.after();
        container.stop();
        db.stop();
    }

    @Test
    public void testPing() {
        Response response = client.target(thirdEyeEndPoint("internal/ping"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
    }


    @Test(dependsOnMethods = "testPing")
    public void testDataset() {
        final DatasetApi requestDatasetApi =
                new DatasetApi()
                        .setName("pageviews")
                        .setActive(true)
                        .setAdditive(true)
                        .setDimensions(
                                Arrays.asList(
                                        "country",
                                        "gender",
                                        "browser",
                                        "version",
                                        "device",
                                        "os"
                                )
                        )
                        .setTimeColumn(
                                new TimeColumnApi()
                                        .setName("date")
                                        .setInterval(Duration.parse("P1D"))
                                        .setFormat("SIMPLE_DATE_FORMAT:yyyyMMdd")
                                        .setTimezone("US/Pacific")
                        )
                        .setExpectedDelay(Duration.of(86400, ChronoUnit.SECONDS))
                        .setDataSource("PinotThirdEyeDataSource");

        Response response = client.target(thirdEyeEndPoint("api/datasets"))
                .request()
                .post(Entity.json(Arrays.asList(requestDatasetApi)));
        assertThat(response.getStatus()).isEqualTo(200);


        // final List<DatasetApi> responseDatasetApi = response.readEntity(new GenericType<List<DatasetApi>>() {});
        //  assertThat(responseDatasetApi).isNotNull();
    }

    @Test(dependsOnMethods = "testDataset")
    public void testDataSourcesLoaded() {
        // A single datasource must exist in the db for the tests to proceed
        Assertions.assertThat(db.executeSql("SELECT * From dataset_config_index").length())
                .isGreaterThan(0);
    }

    @Test(dependsOnMethods = "testDataSourcesLoaded")
    public void testMetrics() {
        final MetricApi requestMetricApi1 =
                new MetricApi()
                        .setName("metric1")
                        .setDataset(
                                new DatasetApi()
                                        .setName("pageviews")
                        )
                        .setActive(true)
                        .setWhere("browser='chrome' AND country='US'")
                        .setAggregationColumn("views")
                        .setAggregationFunction(MetricAggFunction.SUM);

        final MetricApi requestMetricApi2 =
                new MetricApi()
                        .setName("metric2")
                        .setDataset(
                                new DatasetApi()
                                        .setName("pageviews")
                        )
                        .setActive(true)
                        .setWhere("country='US'")
                        .setAggregationColumn("views")
                        .setAggregationFunction(MetricAggFunction.SUM);

        Response response = client.target(thirdEyeEndPoint("api/metrics"))
                .request()
                .post(Entity.json(Arrays.asList(requestMetricApi1, requestMetricApi2)));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test(dependsOnMethods = "testMetrics")
    public void testDerivedMetrics() {
        final MetricApi derivedMetricApi =
                new MetricApi()
                        .setName("metric_ratio")
                        .setDataset(
                                new DatasetApi()
                                        .setName("pageviews")
                        )
                        .setActive(true)
                        .setDerivedMetricExpression("metric1/metric2")
                        .setAggregationFunction(MetricAggFunction.SUM);

        Response response = client.target(thirdEyeEndPoint("api/metrics"))
                .request()
                .post(Entity.json(Arrays.asList(derivedMetricApi)));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test(dependsOnMethods = "testDerivedMetrics")
    public void testEvaluate() {
        final AlertEvaluationApi requestAlertEvaluationApi = new AlertEvaluationApi();
        final AlertApi alertApi = new AlertApi();
        Map<String, AlertNodeApi> nodes = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("offset", "wo1w");
        params.put("percentageChange", 0.2);

        AlertNodeApi alertNodeApi =
                new AlertNodeApi()
                        .setType(AlertNodeType.DETECTION)
                        .setSubType("PERCENTAGE_RULE")
                        .setMetric(
                                new MetricApi()
                                        .setName("metric_ratio")
                                        .setDataset(
                                                new DatasetApi()
                                                        .setName("pageviews")
                                        )
                        ).setParams(params);

        nodes.put("d1", alertNodeApi);

        alertApi
                .setName("derived_metric_alert")
                .setDescription("Test Evaluate")
                .setNodes(nodes)
                .setLastTimestamp(Date.from(Instant.ofEpochMilli(0L)));

        requestAlertEvaluationApi
                .setAlert(alertApi)
                .setStart(Date.from(Instant.ofEpochMilli(1577865600000L)))
                .setEnd(Date.from(Instant.ofEpochMilli(1590994800000L)));

        Response response = client.target(thirdEyeEndPoint("api/alerts/evaluate"))
                .request()
                .post(Entity.json(requestAlertEvaluationApi));
        assertThat(response.getStatus()).isEqualTo(200);

/*        final List<AlertEvaluationApi> responseAlertEvaluationApi = response.readEntity(new GenericType<List<AlertEvaluationApi>>() {});
        assertThat(responseAlertEvaluationApi).isNotNull(); */
    }
}


