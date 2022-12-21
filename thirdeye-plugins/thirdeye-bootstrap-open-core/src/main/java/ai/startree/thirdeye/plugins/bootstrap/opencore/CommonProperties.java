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
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;

/**
 * The goal of this class is to maintain a list of description for commonly used properties.
 */
public class CommonProperties {

  // please maintain this list in alphabetical order
  public static final Map<String, String> COMMON_DESCRIPTIONS = ImmutableMap.<String, String>builder()
      .put("aggregationColumn", "The column to aggregate. Can be a derived metric.")
      .put("aggregationFunction", "The aggregation function to apply on the aggregationColumn. Eg AVG.")
      .put("aggregationParameter", "The second argument of the aggregationFunction. Eg for PERCENTILETDIGEST: 95.")
      .put("coldStartIgnore", "Ignore anomalies at the start of the dataset.")
      .put("dataset", "The dataset to query.")
      .put("dataSource", "The Pinot datasource to use.")
      .put("lookback", "Historical time period to use to train the model. In ISO-8601 format. Eg P21D.")
      .put("monitoringGranularity", "The period of aggregation of the timeseries. In ISO-8601 format. Eg PT1H.")
      .put("sensitivity", "The sensitivity of the model. The smaller, the less anomaly are detected.")
      .put("timeColumn",
          "TimeColumn used to group by time. If set to AUTO (the default value), the Pinot primary time column is used.")
      // TODO CYRIL add once markdown rendering is implemented
      //.put("timeColumnFormat", "Required if timeColumn is not AUTO. [Learn more](https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/reference/operators/data-fetcher#timeformat-strings).")
      .put("timezone", "Timezone used to group by time.")
      .build();

  public static void enrichCommonProperties(final List<AlertTemplateApi> templates) {
    for (final AlertTemplateApi template : templates) {
      final List<TemplatePropertyMetadata> properties = template.getProperties();
      if (properties == null) {
        continue;
      }
      for (final TemplatePropertyMetadata p : properties) {
        if (p.getDescription() == null) {
          p.setDescription(COMMON_DESCRIPTIONS.get(p.getName()));
        }
      }
    }
  }
}
