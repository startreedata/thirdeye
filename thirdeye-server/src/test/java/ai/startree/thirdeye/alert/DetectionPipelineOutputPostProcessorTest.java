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
package ai.startree.thirdeye.alert;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.detection.v2.AnomalyDetectorOperatorResult;
import ai.startree.thirdeye.detectionpipeline.operator.CombinerResult;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.testng.annotations.Test;
import java.io.StringReader;

public class DetectionPipelineOutputPostProcessorTest {

  @Test
  public void testProcessNoCombiner() throws IOException {
    final Map<String, OperatorResult> operatorResults = new HashMap<>() {{
      put("detectorResult", AnomalyDetectorOperatorResult.builder().setTimeseries(
          TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
              "timestamp,value,current\n" +
                  "1,1,1\n" +
                  "2,2,2\n" +
                  "3,3,3\n" +
                  "4,4,4\n"
          )))
      ).build());
    }};

    final AlertEvaluationApi request = new AlertEvaluationApi()
        .setEnd(new Date(2))
        .setStart(new Date(1));

    final Map<String, OperatorResult> gotPostResults = new DetectionPipelineOutputPostProcessor()
        .process(operatorResults, request);

    final TimeSeries gotSeries = Objects.requireNonNull(
        gotPostResults.get("detectorResult").getTimeseries()
    );
    assertThat(gotSeries.toString()).isEqualTo(
        TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
            "timestamp,value,current\n" +
                "1,1,1\n" +
                "2,2,2\n"
        ))).toString()
    );
  }

  @Test
  public void testProcessWithCombiner() throws IOException {
    final Map<String, OperatorResult> operatorResults = new HashMap<>() {{
      put("detectorResult", AnomalyDetectorOperatorResult.builder().setTimeseries(
          TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
              "timestamp,value,current\n" +
                  "1,1,1\n" +
                  "2,2,2\n" +
                  "3,3,3\n" +
                  "4,4,4\n"
          )))
      ).build());
    }};
    final Map<String, OperatorResult> combinerResults = new HashMap<>() {{
      put("combinerResults", new CombinerResult(operatorResults));
    }};

    final AlertEvaluationApi request = new AlertEvaluationApi()
        .setEnd(new Date(2))
        .setStart(new Date(1));

    final Map<String, OperatorResult> gotPostResults = new DetectionPipelineOutputPostProcessor()
        .process(combinerResults, request);

    final TimeSeries gotSeries = Objects.requireNonNull(
        ((CombinerResult) gotPostResults.get("combinerResults"))
            .getResults()
            .get("detectorResult")
            .getTimeseries()
    );
    assertThat(gotSeries.toString()).isEqualTo(
        TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
            "timestamp,value,current\n" +
                "1,1,1\n" +
                "2,2,2\n"
        ))).toString()
    );
  }

  @Test
  public void testProcessManySeries() throws IOException {
    final Map<String, OperatorResult> operatorResults = new HashMap<>() {{
      put("detectorResult0", AnomalyDetectorOperatorResult.builder().setTimeseries(
          TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
              "timestamp,value,current\n" +
                  "1,1,1\n" +
                  "2,2,2\n" +
                  "3,3,3\n" +
                  "4,4,4\n"
          )))
      ).build());
      put("detectorResult1", AnomalyDetectorOperatorResult.builder().setTimeseries(
          TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
              "timestamp,value,current\n" +
                  "1,1,1\n" +
                  "2,2,2\n" +
                  "3,3,3\n" +
                  "4,4,4\n"
          )))
      ).build());
    }};

    final AlertEvaluationApi request = new AlertEvaluationApi()
        .setEnd(new Date(2))
        .setStart(new Date(1));

    final Map<String, OperatorResult> gotPostResults = new DetectionPipelineOutputPostProcessor()
        .process(operatorResults, request);

    final TimeSeries gotSeries0 = Objects.requireNonNull(
        gotPostResults.get("detectorResult0").getTimeseries()
    );
    assertThat(gotSeries0.toString()).isEqualTo(
        TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
            "timestamp,value,current\n" +
                "1,1,1\n" +
                "2,2,2\n"
        ))).toString()
    );

    final TimeSeries gotSeries1 = Objects.requireNonNull(
        gotPostResults.get("detectorResult1").getTimeseries()
    );
    assertThat(gotSeries1.toString()).isEqualTo(
        TimeSeries.fromDataFrame(DataFrame.fromCsv(new StringReader(
            "timestamp,value,current\n" +
                "1,1,1\n" +
                "2,2,2\n"
        ))).toString()
    );
  }
}
