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
package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.DemoDatasetApi;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.data.Schema;

/**
 * Add demo dataset configuration paths here.
 * Make sure to share the same id in DEMO_DATASETS and DEMO_DATASET_CONFIGS.
 * In the schema and table configurations, use the suffix demo_ in the name.
 */
public class DemoConfigs {

  public static final String DEMO_PAGEVIEWS_ID = "pinot-demo-pageviews";

  public static final List<DemoDatasetApi> DEMO_DATASETS = List.of(
      new DemoDatasetApi()
          .setId(DEMO_PAGEVIEWS_ID)
          .setName("eCommerce Website Pageviews")
          .setDescription("8 months of e-commerce data at daily granularity.")
  );

  public static final Map<String, DemoDatasetConfig> DEMO_DATASET_CONFIGS = Map.of(
      DEMO_PAGEVIEWS_ID,
      DemoDatasetConfig.fromResourceFiles("/demo/pinot/pageviews/schema.json",
          "/demo/pinot/pageviews/table_config.json",
          "/demo/pinot/pageviews/batch_config_map_str.json", 
          "https://public-thirdeye-demo-datasets.s3.us-east-2.amazonaws.com/demo-pageviews/data.csv")
  );

  public record DemoDatasetConfig(Schema schema,
                                  TableConfig tableConfig,
                                  String batchConfigMapStr,
                                  String s3SourceUri) {

    public static DemoDatasetConfig fromResourceFiles(final String schemaPath,
        final String tableConfigPath, final String batchConfigMapStrPath,
        final String s3SourceUri) {
      try {
        return new DemoDatasetConfig(
            Schema.fromString(IOUtils.resourceToString(schemaPath, StandardCharsets.UTF_8)),
            Constants.VANILLA_OBJECT_MAPPER.readValue(
                IOUtils.resourceToString(tableConfigPath, StandardCharsets.UTF_8),
                TableConfig.class),
            IOUtils.resourceToString(batchConfigMapStrPath, StandardCharsets.UTF_8),
            s3SourceUri
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
