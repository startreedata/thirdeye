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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import ai.startree.thirdeye.detectionpipeline.components.TimeIndexFiller.TimeLimitInferenceStrategy;
import ai.startree.thirdeye.detectionpipeline.spec.TimeIndexFillerSpec;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.stream.Stream;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class TimeIndexFillerTest {

  private static final long OCTOBER_18_MILLIS = 1634515200000L;
  private static final long OCTOBER_19_MILLIS = 1634601600000L;
  private static final long OCTOBER_20_MILLIS = 1634688000000L;
  private static final long OCTOBER_21_MILLIS = 1634774400000L;
  private static final long OCTOBER_22_MILLIS = 1634860800000L;
  private static final long OCTOBER_23_MILLIS = 1634947200000L;
  private static final long OCTOBER_24_MILLIS = 1635033600000L;
  private static final long OCTOBER_25_MILLIS = 1635120000000L;
  private static final long OCTOBER_26_MILLIS = 1635206400000L;

  private static final double METRIC_VALUE = 1.1;
  private static final double ZERO_FILLER = 0;

  @Test
  public void testFillIndexWithDataMissingInTheMiddleWithMinTimeFromDataWithMaxTimeFromData() {
    // this test corresponds to the filling of data for a single-input detector - with data missing in the middle
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithDataMissingInTheMiddleAndRightWithMinTimeFromDataWithMaxTimeFromDetectionTime() {
    // this test corresponds to the filling of data for of a single-input detector - with data missing in the middle and right
    // this is the recommended default configuration for single input detectors data
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_26_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 23 and october 25 missing
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts",
        OCTOBER_22_MILLIS,
        OCTOBER_23_MILLIS,
        OCTOBER_24_MILLIS,
        OCTOBER_25_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE, ZERO_FILLER);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
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
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 18 missing left
    dataFrame.addSeries("ts", OCTOBER_19_MILLIS, OCTOBER_20_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_18_MILLIS, OCTOBER_19_MILLIS, OCTOBER_20_MILLIS);
    expectedDataFrame.addSeries("met", ZERO_FILLER, METRIC_VALUE, METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
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
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 24 missing right
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE, ZERO_FILLER);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexFillWithNull() {
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        //keep null when filling index
        .setFillNullMethod("KEEP_NULL")
        .setMonitoringGranularity("P1D")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 19 missing before detection period - october 23 missing in the detection period
    dataFrame.addSeries("ts",
        OCTOBER_18_MILLIS,
        OCTOBER_20_MILLIS,
        OCTOBER_21_MILLIS,
        OCTOBER_22_MILLIS,
        OCTOBER_24_MILLIS);
    dataFrame.addSeries("met",
        METRIC_VALUE,
        METRIC_VALUE,
        METRIC_VALUE,
        METRIC_VALUE,
        METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
    final DataFrame outputDf = output.getDataFrame();
    assertThat(outputDf.size()).isEqualTo(7);
    // (met,OCTOBER_19_MILLIS) should be null
    Object october19Value = outputDf.getObject("met", 1);
    assertThat(october19Value).isNull();
    // (met,OCTOBER_23_MILLIS) should be filled with a zero
    Object october23Value = outputDf.getObject("met", 5);
    assertThat(october23Value).isInstanceOf(Double.class);
    assertThat((double) october23Value).isEqualTo(ZERO_FILLER);
    // other days should have value METRIC_VALUE
    Stream.of(0, 2, 3, 4, 6).forEach(rowIdx -> {
      Object value = outputDf.getObject("met", rowIdx);
      assertThat(value).isInstanceOf(Double.class);
      assertThat((double) value).isEqualTo(METRIC_VALUE);
    });
  }

  @Test
  public void testFillIndexWithAllPropertiesInDataTable() {
    // test filling of data without config - all info obtained from the datatable properties
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), Long.toString(inputInterval.getStartMillis()),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), Long.toString(inputInterval.getEndMillis()),
        MacroMetadataKeys.GRANULARITY.toString(), "P1D");
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithTimePropertiesInDataTableGranularityInSpec() {
    // test filling of data with time info obtained from the datatable properties and granularity in config
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setTimestamp("ts");
    spec.setMonitoringGranularity("P1D");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), Long.toString(inputInterval.getStartMillis()),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), Long.toString(inputInterval.getEndMillis()));
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testFillIndexWithGranularityPropertiesInDataTableTimeInSpec() {
    // test filling of data with granularity info obtained from the datatable properties and time in config
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setMinTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setLookback("P4D")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS,
        OCTOBER_25_MILLIS,
        DateTimeZone.UTC);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle - lookback missing
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(), "P1D");
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts",
        OCTOBER_18_MILLIS,
        OCTOBER_19_MILLIS,
        OCTOBER_20_MILLIS,
        OCTOBER_21_MILLIS,
        OCTOBER_22_MILLIS,
        OCTOBER_23_MILLIS,
        OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met",
        ZERO_FILLER,
        ZERO_FILLER,
        ZERO_FILLER,
        ZERO_FILLER,
        METRIC_VALUE,
        ZERO_FILLER,
        METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      fail("Index filling failed: " + e);
    }
    assertThat(output.getDataFrame()).isEqualTo(expectedDataFrame);
  }
}
