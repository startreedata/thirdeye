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
package ai.startree.thirdeye.detectionpipeline.components;

import static ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller.TimeLimitInferenceStrategy.FROM_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller.TimeLimitInferenceStrategy;
import ai.startree.thirdeye.detectionpipeline.spec.TimeIndexFillerSpec;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class TimeIndexFillerTest {

  private static final long OCT_18_MS = epoch("2021-10-18T00:00:00Z");
  private static final long OCT_19_MS = epoch("2021-10-19T00:00:00Z");
  private static final long OCT_20_MS = epoch("2021-10-20T00:00:00Z");
  private static final long OCT_21_MS = epoch("2021-10-21T00:00:00Z");
  private static final long OCT_22_MS = epoch("2021-10-22T00:00:00Z");
  private static final long OCT_23_MS = epoch("2021-10-23T00:00:00Z");
  private static final long OCT_24_MS = epoch("2021-10-24T00:00:00Z");
  private static final long OCT_25_MS = epoch("2021-10-25T00:00:00Z");
  private static final long OCT_26_MS = epoch("2021-10-26T00:00:00Z");

  // this is the value of the metric column
  private static final double METRIC_VALUE = 1.1;

  private static final double ZERO_FILLER = 0;

  private static long epoch(final String formattedTime) {
    return Instant.parse(formattedTime).toEpochMilli();
  }

  private static Interval interval(final long startTime, final long endTime) {
    return new Interval(startTime, endTime, DateTimeZone.UTC);
  }

  private static TimeIndexFiller newTimeIndexFiller(final TimeIndexFillerSpec spec) {
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);
    return timeIndexFiller;
  }

  private static DataTable runTimeIndexFiller(final TimeIndexFiller instance,
      final Interval interval,
      final DataTable inputDataTable) {
    try {
      return instance.fillIndex(interval, inputDataTable);
    } catch (final Exception e) {
      fail("Index filling failed: " + e);
      throw e;
    }
  }

  private static DataTable runTimeIndexFiller(final TimeIndexFillerSpec spec,
      final Interval inputInterval,
      final DataTable inputDataTable) {
    final TimeIndexFiller timeIndexFiller = newTimeIndexFiller(spec);
    return runTimeIndexFiller(timeIndexFiller,
        inputInterval,
        inputDataTable);
  }

  /**
   * Test the filling of data for a single-input detector - with data missing in the middle
   */
  @Test
  public void testFillIndexWithDataMissingInTheMiddleWithMinTimeFromDataWithMaxTimeFromData() {
    // this test corresponds to the filling of data for a single-input detector - with data missing in the middle
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(FROM_DATA.toString())
        .setMaxTimeInference(FROM_DATA.toString())
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCT_22_MS, OCT_24_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCT_22_MS, OCT_23_MS, OCT_24_MS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    final DataTable output = runTimeIndexFiller(spec,
        interval(OCT_22_MS, OCT_25_MS),
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  /**
   * this test corresponds to the filling of data for of a single-input detector - with data
   * missing in the middle and right this is the recommended default configuration for single input
   * detectors data
   */
  @Test
  public void testFillIndexWithDataMissingInTheMiddleAndRightWithMinTimeFromDataWithMaxTimeFromDetectionTime() {

    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(FROM_DATA.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");

    final DataFrame dataFrame = new DataFrame();
    // october 23 and october 25 missing
    dataFrame.addSeries("ts", OCT_22_MS, OCT_24_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts",
        OCT_22_MS,
        OCT_23_MS,
        OCT_24_MS,
        OCT_25_MS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE, ZERO_FILLER);

    final DataTable output = runTimeIndexFiller(spec,
        interval(OCT_22_MS, OCT_26_MS),
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithDataMissingLeftWithMinTimeFromDetectionTimeWithLookbackWithMaxTimeFromDetectionTimeWithLookback() {
    // this test corresponds to the filling of baseline data for a 2-inputs detector - with data missing left
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK.toString())
        // lookback 4 days
        .setLookback("P4D")
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");

    final DataFrame dataFrame = new DataFrame();
    // october 18 missing left
    dataFrame.addSeries("ts", OCT_19_MS, OCT_20_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCT_18_MS, OCT_19_MS, OCT_20_MS);
    expectedDataFrame.addSeries("met", ZERO_FILLER, METRIC_VALUE, METRIC_VALUE);

    final DataTable output = runTimeIndexFiller(spec,
        interval(OCT_22_MS, OCT_25_MS),
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithDataMissingRightWithMinTimeFromDetectionTimeWithMaxTimeFromDetectionTime() {
    // this test corresponds to the filling of current data for a 2-inputs detector  - with data missing right
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = newTimeIndexFiller(
        spec);

    final Interval inputInterval = interval(OCT_22_MS, OCT_25_MS);

    final DataFrame dataFrame = new DataFrame();
    // october 24 missing right
    dataFrame.addSeries("ts", OCT_22_MS, OCT_23_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCT_22_MS, OCT_23_MS, OCT_24_MS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE, ZERO_FILLER);

    final DataTable output = runTimeIndexFiller(timeIndexFiller,
        inputInterval,
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexFillWithNull() {
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(FROM_DATA.toString())
        .setMaxTimeInference(FROM_DATA.toString())
        //keep null when filling index
        .setFillNullMethod("KEEP_NULL")
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");

    final DataFrame dataFrame = new DataFrame();
    // october 19 missing before detection period - october 23 missing in the detection period
    dataFrame.addSeries("ts",
        OCT_18_MS,
        OCT_20_MS,
        OCT_21_MS,
        OCT_22_MS,
        OCT_24_MS);
    dataFrame.addSeries("met",
        METRIC_VALUE,
        METRIC_VALUE,
        METRIC_VALUE,
        METRIC_VALUE,
        METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataTable output = runTimeIndexFiller(newTimeIndexFiller(spec),
        interval(OCT_22_MS, OCT_25_MS),
        inputDataTable);
    assertThat(output).isNotNull();
    final DataFrame outputDf = output.getDataFrame();
    assertThat(outputDf.size()).isEqualTo(7);

    // (met,OCT_19_MS) should be null
    final Object october19Value = outputDf.getObject("met", 1);
    assertThat(october19Value).isNull();

    // (met,OCT_23_MS) should be filled with a zero
    final Object october23Value = outputDf.getObject("met", 5);
    assertThat(october23Value).isInstanceOf(Double.class);
    assertThat((double) october23Value).isEqualTo(ZERO_FILLER);

    // other days should have value METRIC_VALUE
    Stream.of(0, 2, 3, 4, 6).forEach(rowIdx -> {
      final Object value = outputDf.getObject("met", rowIdx);
      assertThat(value).isInstanceOf(Double.class);
      assertThat((double) value).isEqualTo(METRIC_VALUE);
    });
  }

  @Test
  public void testFillIndexWithAllPropertiesInDataTable() {
    // test filling of data without config - all info obtained from the datatable properties
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = newTimeIndexFiller(
        spec);

    final Interval inputInterval = interval(OCT_22_MS, OCT_25_MS);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCT_22_MS, OCT_24_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), Long.toString(inputInterval.getStartMillis()),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), Long.toString(inputInterval.getEndMillis()),
        MacroMetadataKeys.GRANULARITY.toString(), "P1D");
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCT_22_MS, OCT_23_MS, OCT_24_MS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    final DataTable output = runTimeIndexFiller(timeIndexFiller,
        inputInterval,
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithTimePropertiesInDataTableGranularityInSpec() {
    // test filling of data with time info obtained from the datatable properties and granularity in config
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setTimestamp("ts");
    spec.setMonitoringGranularity("P1D");
    final TimeIndexFiller timeIndexFiller = newTimeIndexFiller(
        spec);

    final Interval inputInterval = interval(OCT_22_MS, OCT_25_MS);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCT_22_MS, OCT_24_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), Long.toString(inputInterval.getStartMillis()),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), Long.toString(inputInterval.getEndMillis()));
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCT_22_MS, OCT_23_MS, OCT_24_MS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    final DataTable output = runTimeIndexFiller(timeIndexFiller,
        inputInterval,
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithGranularityPropertiesInDataTableTimeInSpec() {
    // test filling of data with granularity info obtained from the datatable properties and time in config
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setMinTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK.toString())
        .setMaxTimeInference(FROM_DATA.toString())
        .setLookback("P4D")
        .setTimestamp("ts");

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle - lookback missing
    dataFrame.addSeries("ts", OCT_22_MS, OCT_24_MS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(), "P1D");
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts",
        OCT_18_MS,
        OCT_19_MS,
        OCT_20_MS,
        OCT_21_MS,
        OCT_22_MS,
        OCT_23_MS,
        OCT_24_MS);
    expectedDataFrame.addSeries("met",
        ZERO_FILLER,
        ZERO_FILLER,
        ZERO_FILLER,
        ZERO_FILLER,
        METRIC_VALUE,
        ZERO_FILLER,
        METRIC_VALUE);

    final DataTable output = runTimeIndexFiller(spec,
        interval(OCT_22_MS, OCT_25_MS),
        inputDataTable);
    assertThat(output).isNotNull();
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testForwardFill() {
    // this test corresponds to the filling of data for a single-input detector - with data missing in the middle
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec()
        .setMinTimeInference(FROM_DATA.toString())
        .setMaxTimeInference(FROM_DATA.toString())
        .setFillNullMethod("FILL_FORWARD");

    spec.setMonitoringGranularity("P1D")
        .setTimestamp("ts");

    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(new DataFrame()
        .addSeries("ts", OCT_22_MS, OCT_24_MS) // october 23 missing in the middle
        .addSeries("met", METRIC_VALUE, METRIC_VALUE)
    );

    final DataTable output = runTimeIndexFiller(newTimeIndexFiller(spec),
        interval(OCT_22_MS, OCT_25_MS),
        inputDataTable);
    assertThat(output).isNotNull();

    final DataFrame expected = new DataFrame()
        .addSeries("ts", OCT_22_MS, OCT_23_MS, OCT_24_MS)
        .addSeries("met", METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
    assertThat(output.getDataFrame()).isEqualTo(expected);
  }
}
