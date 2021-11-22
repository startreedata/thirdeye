package org.apache.pinot.thirdeye.detection.v2.components.filler;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.pinot.thirdeye.detection.v2.components.filler.TimeIndexFiller.TimeLimitInferenceStrategy;
import org.apache.pinot.thirdeye.detection.v2.spec.TimeIndexFillerSpec;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.testng.annotations.Test;

public class TimeIndexFillerTest {

  private static final DateTimeFormatter DATE_PARSER = DateTimeFormat.forPattern(
      "yyyy-MM-dd HH:mm:ss.SSS z");

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
        .setMonitoringGranularity("1_DAYS")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

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
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
  }

  @Test
  public void testFillIndexWithDataMissingInTheMiddleAndRightWithMinTimeFromDataWithMaxTimeFromDetectionTime() {
    // this test corresponds to the filling of data for of a single-input detector - with data missing in the middle and right
    // this is the recommended default configuration for single input detectors data
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMonitoringGranularity("1_DAYS")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_26_MILLIS);

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
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
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
        .setMonitoringGranularity("1_DAYS")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

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
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
  }

  @Test
  public void testFillIndexWithDataMissingRightWithMinTimeFromDetectionTimeWithMaxTimeFromDetectionTime() {
    // this test corresponds to the filling of current data for a 2-inputs detector  - with data missing right
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString())
        .setMonitoringGranularity("1_DAYS")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

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
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
  }

  @Test
  public void testFillIndexFillWithNull() {
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec
        .setMinTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        //keep null when filling index
        .setFillNullMethod("KEEP_NULL")
        .setMonitoringGranularity("1_DAYS")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

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
      Assert.fail("Index filling failed: " + e);
    }
    assert output.getRowCount() == 7;
    // (met,OCTOBER_19_MILLIS) should be null
    Object october19Value = output.getObject(1, 1);
    assert october19Value == null;
    // (met,OCTOBER_23_MILLIS) should be filled with a zero
    Object october23Value = output.getObject(5, 1);
    assert october23Value instanceof Double && (double) october23Value == ZERO_FILLER;
    // other days should have value METRIC_VALUE
    final DataTable finalOutput = output;
    Stream.of(0, 2, 3, 4, 6).forEach(rowIdx -> {
      Object value = finalOutput.getObject(rowIdx, 1);
      assert value instanceof Double && (double) value == METRIC_VALUE;
    });
  }

  @Test
  public void testFillIndexWithAllPropertiesInDataTable() {
    // test filling of data without config - all info obtained from the datatable properties
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), Long.toString(inputInterval.getStartMillis()),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), Long.toString(inputInterval.getEndMillis()),
        MacroMetadataKeys.TIME_COLUMN.toString(), "ts",
        MacroMetadataKeys.GRANULARITY.toString(), "P1D");
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
  }

  @Test
  public void testFillIndexWithTimePropertiesInDataTableGranularityInSpec() {
    // test filling of data with granularity in config and time info obtained from the datatable properties
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setMonitoringGranularity("1_DAYS");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Map<String, String> inputProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), Long.toString(inputInterval.getStartMillis()),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), Long.toString(inputInterval.getEndMillis()),
        MacroMetadataKeys.TIME_COLUMN.toString(), "ts");
    inputDataTable.getProperties().putAll(inputProperties);

    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);

    DataTable output = null;
    try {
      output = timeIndexFiller.fillIndex(inputInterval, inputDataTable);
    } catch (Exception e) {
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
  }

  @Test
  public void testFillIndexWithGranularityPropertiesInDataTableTimeInSpec() {
    // test filling of data with time in config and granularity info obtained from the datatable properties
    final TimeIndexFillerSpec spec = new TimeIndexFillerSpec();
    spec.setMinTimeInference(TimeLimitInferenceStrategy.FROM_DETECTION_TIME_WITH_LOOKBACK.toString())
        .setMaxTimeInference(TimeLimitInferenceStrategy.FROM_DATA.toString())
        .setLookback("P4D")
        .setTimestamp("ts");
    final TimeIndexFiller timeIndexFiller = new TimeIndexFiller();
    timeIndexFiller.init(spec);

    final Interval inputInterval = new Interval(OCTOBER_22_MILLIS, OCTOBER_25_MILLIS);

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
      Assert.fail("Index filling failed: " + e);
    }
    Assert.assertEquals(expectedDataFrame, output.getDataFrame());
  }

  @Test
  public void testGetFirstIndexValueForConstraintEqualToFloor() {
    final long inputMinTimeConstraint = 1634860800000L; //2021-10-22 00:00:00.000 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-22 00:00:00.000 UTC");
    final DateTime output = TimeIndexFiller.getFirstIndexValue(inputMinTimeConstraint, inputPeriod);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testGetFirstIndexValueForConstraintBiggerThanFloor() {
    // firstIndex must be bigger than time constraint and respect the period
    final long inputMinTimeConstraint = 1634860800001L; //2021-10-22 00:00:00.001 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-23 00:00:00.000 UTC");
    final DateTime output = TimeIndexFiller.getFirstIndexValue(inputMinTimeConstraint, inputPeriod);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testGetLastIndexValueForConstraintEqualToFloor() {
    // lastIndex must be strictly smaller than time constraint and respect the period
    final long inputMinTimeConstraint = 1634860800000L; //2021-10-22 00:00:00.000 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-21 00:00:00.000 UTC");
    final DateTime output = TimeIndexFiller.getLastIndexValue(inputMinTimeConstraint, inputPeriod);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testGetLastIndexValueForConstraintBiggerThanFloor() {
    // firstIndex must be bigger than time constraint and respect period
    final long inputMinTimeConstraint = 1634860800001L; //2021-10-22 00:00:00.001 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-22 00:00:00.000 UTC");
    final DateTime output = TimeIndexFiller.getLastIndexValue(inputMinTimeConstraint, inputPeriod);

    Assert.assertEquals(expected, output);
  }

  // Tests of floorByPeriod
  @Test
  public void testFloorByPeriodRoundByYear() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-01-01 00:00:00.000 UTC");
    final Period oneYear = Period.years(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneYear);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy2Years() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2020-01-01 00:00:00.000 UTC");
    final Period twoYears = Period.years(2);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, twoYears);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundByMonth() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-01 00:00:00.000 UTC");
    final Period oneMonth = Period.months(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneMonth);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy2Months() {
    // rounding by 2 months falls on odd numbered months: Jan, Mar, May, etc...
    final DateTime input = DATE_PARSER.parseDateTime("2021-10-01 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-9-01 00:00:00.000 UTC");
    final Period twoMonths = Period.months(2);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, twoMonths);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundByWeek() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-24 11:22:33.444 UTC"); //wednesday
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 00:00:00.000 UTC"); //monday
    final Period oneWeek = Period.weeks(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneWeek);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy2Weeks() {
    // rounding by 2 months falls on odd numbered months: Jan, Mar, May, etc...
    final DateTime input = DATE_PARSER.parseDateTime("2021-12-1 11:22:33.444 UTC");  //wednesday december
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 00:00:00.000 UTC"); //monday nov
    final Period twoWeeks = Period.weeks(2);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, twoWeeks);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundByDay() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 00:00:00.000 UTC");
    final Period oneDay = Period.days(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneDay);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy2Days() {
    // rounding by 2 days falls on odd numbered days
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-21 00:00:00.000 UTC");
    final Period twoDays = Period.days(2);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, twoDays);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundByHour() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:00:00.000 UTC");
    final Period oneHour = Period.hours(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneHour);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy2Hours() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 10:00:00.000 UTC");
    final Period twoHours = Period.hours(2);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, twoHours);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundByMinute() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:00.000 UTC");
    final Period oneMinute = Period.minutes(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneMinute);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy15Minutes() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:15:00.000 UTC");
    final Period fifteenMinutes = Period.minutes(15);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, fifteenMinutes);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBySecond() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.000 UTC");
    final Period oneSecond = Period.seconds(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneSecond);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy30Seconds() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:30.000 UTC");
    final Period thirtySeconds = Period.seconds(30);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, thirtySeconds);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundByMilli() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = input;
    final Period oneMilli = Period.millis(1);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, oneMilli);

    Assert.assertEquals(expected, output);
  }

  @Test
  public void testFloorByPeriodRoundBy100Milli() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.400 UTC");
    final Period hundredMillis = Period.millis(100);
    final DateTime output = TimeIndexFiller.floorByPeriod(input, hundredMillis);

    Assert.assertEquals(expected, output);
  }
}
