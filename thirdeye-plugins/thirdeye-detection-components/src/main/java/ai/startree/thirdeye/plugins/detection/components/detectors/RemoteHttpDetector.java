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
package ai.startree.thirdeye.plugins.detection.components.detectors;

import static ai.startree.thirdeye.spi.Constants.COL_ANOMALY;
import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.detection.components.SimpleAnomalyDetectorResult;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.BaselineProvider;
import ai.startree.thirdeye.spi.detection.DetectorException;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Http Detector
 */
public class RemoteHttpDetector implements AnomalyDetector<RemoteHttpDetectorSpec>,
    BaselineProvider<RemoteHttpDetectorSpec> {

  private static final Logger log = LoggerFactory.getLogger(RemoteHttpDetector.class);

  private RemoteHttpDetectorSpec spec;

  private static DataFrameApi toDataFrameApi(final DataFrame currentDf) {
    final Map<String, List<Serializable>> seriesMap = new LinkedHashMap<>();
    for (final Map.Entry<String, Series> e : currentDf.getSeries().entrySet()) {
      seriesMap.put(e.getKey(), e.getValue().getObjects().toListTyped());
    }
    return new DataFrameApi().setSeriesMap(seriesMap);
  }

  @Override
  public void init(final RemoteHttpDetectorSpec spec) {
    this.spec = spec;
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval interval,
      final Map<String, DataTable> timeSeriesMap
  ) throws DetectorException {
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame currentDf = current.getDataFrame();
    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_VALUE)
        .addSeries(COL_CURRENT, currentDf.get(COL_VALUE).copy())
        .addSeries(COL_LOWER_BOUND, currentDf.get(COL_VALUE).copy())
        .addSeries(COL_UPPER_BOUND, currentDf.get(COL_VALUE).copy())
        .addSeries(COL_ANOMALY, BooleanSeries.fillValues(currentDf.size(), false))
        .setIndex(COL_TIME);

    final HttpDetectorApi api = buildPayload(interval, currentDf);

    final HttpDetectorApi responseApi = runDetection(api);
    try {
      System.out.println(new ObjectMapper().writeValueAsString(responseApi));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return new SimpleAnomalyDetectorResult(buildDataFrameFromResponse(responseApi));
  }

  private DataFrame buildDataFrameFromResponse(final HttpDetectorApi responseApi) {
    final DataFrameApi dataframeApi = responseApi.getDataframe();
    final Map<String, List<Serializable>> seriesMap = dataframeApi.getSeriesMap();
    return new DataFrame()
        .addSeriesObjects(COL_TIME, seriesMap.get(COL_TIME).toArray(new Object[]{}))
        .addSeriesObjects(COL_CURRENT, seriesMap.get(COL_CURRENT).toArray(new Object[]{}))
        .addSeriesObjects(COL_VALUE, seriesMap.get(COL_VALUE).toArray(new Object[]{}))
        .addSeriesObjects(COL_LOWER_BOUND, seriesMap.get(COL_LOWER_BOUND).toArray(new Object[]{}))
        .addSeriesObjects(COL_UPPER_BOUND, seriesMap.get(COL_UPPER_BOUND).toArray(new Object[]{}))
        .addSeriesObjects(COL_ANOMALY, seriesMap.get(COL_ANOMALY).toArray(new Object[]{}));
  }

  private HttpDetectorApi runDetection(final HttpDetectorApi api) {
    final String url = spec.getUrl();

    // base url must end with '/'. url itself can be a full path.
    final String baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
    final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    final HttpDetectorService service = retrofit.create(HttpDetectorService.class);
    final Call<HttpDetectorApi> call = service.evaluate(url, api);
    try {
      Response<HttpDetectorApi> response = call.execute();
      if (!response.isSuccessful()) {
        log.error("Unable to fetch oidc info! code: {}, message: {}",
            response.code(),
            response.message());
        throw new ThirdEyeException(ThirdEyeStatus.ERR_UNKNOWN,
            String.format("HTTP detector failed. code: %d msg: %s",
                response.code(),
                response.message()));
      }

      return response.body();
    } catch (IOException e) {
      throw new ThirdEyeException(ThirdEyeStatus.ERR_UNKNOWN,
          String.format("HTTP detector failed. msg: %s", e.getMessage()));
    }
  }

  private HttpDetectorApi buildPayload(final Interval interval, final DataFrame currentDf) {
    return new HttpDetectorApi()
        .setDataframe(toDataFrameApi(currentDf))
        .setSpec(spec)
        .setStartMillis(interval.getStartMillis())
        .setEndMillis(interval.getEndMillis());
  }
}
