/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datasource.csv;

import static org.apache.pinot.thirdeye.spi.dataframe.Series.SeriesType.STRING;

import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.Grouping;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.Series.LongConditional;
import org.apache.pinot.thirdeye.spi.dataframe.Series.StringConditional;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.MetricFunction;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.detection.MetricAggFunction;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.TimeSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
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
    this.translator = new DAOTranslator(context.getMetricConfigManager());
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

  /**
   * Execute the request of querying CSV ThirdEye data source.
   * Supports filter operation using time stamp and dimensions.
   * Supports group by time stamp and dimensions.
   * Only supports SUM as the aggregation function for now.
   *
   * @return a ThirdEyeResponse that contains the result of executing the request.
   */
  @Override
  public ThirdEyeResponse execute(final ThirdEyeRequest request) throws Exception {
    DataFrame df = new DataFrame();
    for (MetricFunction function : request.getMetricFunctions()) {
      final String inputName = translator.translate(function.getMetricId());
      final String outputName = function.toString();

      final MetricAggFunction aggFunction = function.getFunctionName();
      if (aggFunction != MetricAggFunction.SUM) {
        throw new IllegalArgumentException(
            String.format("Aggregation function '%s' not supported yet.", aggFunction));
      }

      DataFrame data = datasets.get(function.getDataset());

      // filter constraints
      if (request.getStartTimeInclusive() != null) {
        data = data.filter(
            (LongConditional) values -> values[0] >= request.getStartTimeInclusive().getMillis(),
            COL_TIMESTAMP);
      }

      if (request.getEndTimeExclusive() != null) {
        data = data.filter(
            (LongConditional) values -> values[0] < request.getEndTimeExclusive().getMillis(),
            COL_TIMESTAMP);
      }

      if (request.getFilterSet() != null) {
        Multimap<String, String> filters = request.getFilterSet();
        for (final Map.Entry<String, Collection<String>> filter : filters.asMap().entrySet()) {
          data = data.filter(makeFilter(filter.getValue()), filter.getKey());
        }
      }

      data = data.dropNull(inputName);

      //
      // with grouping
      //
      if (request.getGroupBy() != null && request.getGroupBy().size() != 0) {
        Grouping.DataFrameGrouping dataFrameGrouping = data.groupByValue(request.getGroupBy());
        List<String> aggregationExps = new ArrayList<>();
        final String[] groupByColumns = request.getGroupBy().toArray(new String[0]);
        for (String groupByCol : groupByColumns) {
          aggregationExps.add(groupByCol + ":first");
        }
        aggregationExps.add(inputName + ":sum");

        if (request.getGroupByTimeGranularity() != null) {
          // group by both time granularity and column
          List<DataFrame.Tuple> tuples =
              dataFrameGrouping.aggregate(aggregationExps).getSeries().get("key").getObjects()
                  .toListTyped();
          for (final DataFrame.Tuple key : tuples) {
            DataFrame filteredData = data.filter((StringConditional) values -> {
              for (int i = 0; i < groupByColumns.length; i++) {
                if (values[i] != key.getValues()[i]) {
                  return false;
                }
              }
              return true;
            }, groupByColumns);
            filteredData = filteredData.dropNull()
                .groupByInterval(COL_TIMESTAMP, request.getGroupByTimeGranularity().toMillis())
                .aggregate(aggregationExps);
            if (df.size() == 0) {
              df = filteredData;
            } else {
              df = df.append(filteredData);
            }
          }
          df.renameSeries(inputName, outputName);
        } else {
          // group by columns only
          df = dataFrameGrouping.aggregate(aggregationExps);
          df.dropSeries("key");
          df.renameSeries(inputName, outputName);
          df = df.sortedBy(outputName).reverse();

          if (request.getLimit() > 0) {
            df = df.head(request.getLimit());
          }
        }

        //
        // without dimension grouping
        //
      } else {
        if (request.getGroupByTimeGranularity() != null) {
          // group by time granularity only
          // TODO handle non-UTC time zone gracefully
          df = data.groupByInterval(COL_TIMESTAMP, request.getGroupByTimeGranularity().toMillis())
              .aggregate(inputName + ":sum");
          df.renameSeries(inputName, outputName);
        } else {
          // aggregation only
          df.addSeries(outputName, data.getDoubles(inputName).sum());
          df.addSeries(COL_TIMESTAMP, LongSeries.buildFrom(-1));
        }
      }

      df = df.dropNull(outputName);
    }

    // TODO handle non-dataset granularity gracefully
    TimeSpec timeSpec = new TimeSpec("timestamp", new TimeGranularity(1, TimeUnit.HOURS),
        TimeSpec.SINCE_EPOCH_FORMAT);
    if (request.getGroupByTimeGranularity() != null) {
      timeSpec = new TimeSpec("timestamp", request.getGroupByTimeGranularity(),
          TimeSpec.SINCE_EPOCH_FORMAT);
    }

    return new CSVThirdEyeResponse(request, timeSpec, df);
  }

  @Override
  public DataTable fetchDataTable(final ThirdEyeRequestV2 request) throws Exception {
    // fixme cyril implement this
    LOG.error("fetchDataTable not implemented in CSVThirdEyeDataSource but returns an empty Df for e2e tests.");
    DataFrame dataFrame = new DataFrame()
        .addSeries("ts", new double[0])
        .addSeries("met", new long[0]);
    return SimpleDataTable.fromDataFrame(dataFrame);
  }

  @Override
  public List<String> getDatasets() throws Exception {
    return new ArrayList<>(datasets.keySet());
  }

  @Override
  public void clear() throws Exception {

  }

  @Override
  public void close() throws Exception {

  }

  @Override
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    if (!datasets.containsKey(datasetConfig.getName())) {
      throw new IllegalArgumentException();
    }
    return datasets.get(datasetConfig.getName()).getLongs(COL_TIMESTAMP).max().longValue();
  }

  @Override
  public Map<String, List<String>> getDimensionFilters(final DatasetConfigDTO datasetConfig)
      throws Exception {
    String dataset = datasetConfig.getName();
    if (!datasets.containsKey(dataset)) {
      throw new IllegalArgumentException();
    }
    Map<String, Series> data = datasets.get(dataset).getSeries();
    Map<String, List<String>> output = new HashMap<>();
    for (Map.Entry<String, Series> entry : data.entrySet()) {
      if (entry.getValue().type() == STRING) {
        output.put(entry.getKey(), entry.getValue().unique().getStrings().toList());
      }
    }
    return output;
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


