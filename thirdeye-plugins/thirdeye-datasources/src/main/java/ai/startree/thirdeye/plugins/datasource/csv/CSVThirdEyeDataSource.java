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
package ai.startree.thirdeye.plugins.datasource.csv;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.Collections2;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type CSV third eye data source, which can make CSV file the data source of ThirdEye.
 * Can be used for testing purposes. The CSV file must have a column called 'timestamp', which is
 * the timestamp of the time series.
 */
public class CSVThirdEyeDataSource implements ThirdEyeDataSource {

  private static final Logger LOG = LoggerFactory.getLogger(CSVThirdEyeDataSource.class);

  /**
   * The constant COL_TIMESTAMP. The name of the time stamp column.
   */
  public static final String COL_TIMESTAMP = "timestamp";

  private Map<String, DataFrame> datasets;
  private TranslateDelegator translator; // The Translator from metric Id to metric name.
  private String name; // datasource name

  @SuppressWarnings("unused")
  public CSVThirdEyeDataSource() {
    // Used to instantiate the class.
  }

  /**
   * This constructor is invoked by fromUrl
   *
   * @param datasets the data sets
   * @param metricNameMap the static metric Id to metric name mapping.
   */
  CSVThirdEyeDataSource(Map<String, DataFrame> datasets, Map<Long, String> metricNameMap) {
    this.datasets = datasets;
    this.translator = new StaticTranslator(metricNameMap);
    this.name = CSVThirdEyeDataSource.class.getSimpleName();
  }

  @Override
  public void init(ThirdEyeDataSourceContext context) {
    Map<String, Object> properties = context.getDataSourceDTO().getProperties();
    Map<String, DataFrame> dataframes = new HashMap<>();
    for (Map.Entry<String, Object> property : properties.entrySet()) {
      try (InputStreamReader reader = new InputStreamReader(
          makeUrlFromPath(property.getValue().toString()).openStream())) {
        dataframes.put(property.getKey(), DataFrame.fromCsv(reader));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    this.datasets = dataframes;
    this.translator = new DAOTranslator(null);
    this.name = MapUtils.getString(properties, "name", CSVThirdEyeDataSource.class.getSimpleName());
  }

  /**
   * Factory method of CSVThirdEyeDataSource. Construct a CSVThirdEyeDataSource using data frames.
   *
   * @param dataSets the data sets
   * @param metricNameMap the metric name map
   * @return the CSVThirdEyeDataSource
   */
  public static CSVThirdEyeDataSource fromDataFrame(Map<String, DataFrame> dataSets,
      Map<Long, String> metricNameMap) {
    return new CSVThirdEyeDataSource(dataSets, metricNameMap);
  }

  /**
   * Factory method of CSVThirdEyeDataSource. Construct a CSVThirdEyeDataSource from data set URLs.
   *
   * @param dataSets the data sets in URL
   * @param metricNameMap the metric name map
   * @return the CSVThirdEyeDataSource
   * @throws Exception the exception
   */
  public static CSVThirdEyeDataSource fromUrl(Map<String, URL> dataSets,
      Map<Long, String> metricNameMap)
      throws Exception {
    Map<String, DataFrame> dataframes = new HashMap<>();
    for (Map.Entry<String, URL> source : dataSets.entrySet()) {
      try (InputStreamReader reader = new InputStreamReader(source.getValue().openStream())) {
        dataframes.put(source.getKey(), DataFrame.fromCsv(reader));
      }
    }

    return new CSVThirdEyeDataSource(dataframes, metricNameMap);
  }

  /**
   * Return the name of CSVThirdEyeDataSource.
   *
   * @return the name of this CSVThirdEyeDataSource
   */
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    // fixme cyril implement this
    LOG.error(
        "fetchDataTable not implemented in CSVThirdEyeDataSource but returns an empty Df for e2e tests.");
    DataFrame dataFrame = new DataFrame()
        .addSeries("ts", new double[0])
        .addSeries("met", new long[0]);
    return SimpleDataTable.fromDataFrame(dataFrame);
  }

  @Override
  public void clear() throws Exception {

  }

  @Override
  public void close() throws Exception {

  }

  private URL makeUrlFromPath(String input) {
    try {
      return new URL(input);
    } catch (MalformedURLException ignore) {
      // ignore
    }
    return this.getClass().getResource(input);
  }

  /**
   * Returns a filter function with inclusion and exclusion support
   *
   * @param values dimension filter values
   * @return StringConditional
   */
  private Series.StringConditional makeFilter(Collection<String> values) {
    final Set<String> exclusions = new HashSet<>(
        Collections2.filter(values, s -> s != null && s.startsWith("!")));

    final Set<String> inclusions = new HashSet<>(values);
    inclusions.removeAll(exclusions);

    return values1 -> (inclusions.isEmpty() || inclusions.contains(values1[0])) && !exclusions
        .contains("!" + values1[0]);
  }

  private interface TranslateDelegator {

    /**
     * translate a metric id to metric name
     *
     * @param metricId the metric id
     * @return the metric name as a string
     */
    String translate(Long metricId);
  }

  private static class DAOTranslator implements TranslateDelegator {

    private final MetricConfigManager metricConfigManager;

    public DAOTranslator(final MetricConfigManager metricConfigManager) {
      this.metricConfigManager = metricConfigManager;
    }

    /**
     * The translator that maps metric id to metric name based on a configDTO.
     */

    @Override
    public String translate(Long metricId) {
      MetricConfigDTO configDTO = metricConfigManager.findById(metricId);
      if (configDTO == null) {
        throw new IllegalArgumentException(String.format("Can not find metric id %d", metricId));
      }
      return configDTO.getName();
    }
  }

  private static class StaticTranslator implements TranslateDelegator {

    /**
     * The Static translator that maps metric id to metric name based on a static map.
     */
    Map<Long, String> staticMap;

    /**
     * Instantiates a new Static translator.
     *
     * @param staticMap the static map
     */
    public StaticTranslator(Map<Long, String> staticMap) {
      this.staticMap = staticMap;
    }

    @Override
    public String translate(Long metricId) {
      return staticMap.get(metricId);
    }
  }
}


