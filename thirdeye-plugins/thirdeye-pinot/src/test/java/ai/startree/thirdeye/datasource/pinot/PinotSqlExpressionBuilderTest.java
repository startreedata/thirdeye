/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import static ai.startree.thirdeye.datasource.pinot.PinotSqlExpressionBuilder.removeSimpleDateFormatPrefix;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class PinotSqlExpressionBuilderTest {

  //fixme cyril add test for timezone
  @Test
  public void testCyril() {
    String simpleDateFormatString = removeSimpleDateFormatPrefix("yyyyMMdd");
    final DateTimeFormatter utcDataDateTimeFormatter = DateTimeFormat.forPattern(
        simpleDateFormatString).withZoneUTC();
    final DateTimeFormatter cetDataDateTimeFormatter = DateTimeFormat.forPattern(
        simpleDateFormatString).withZone(DateTimeZone.forID("Europe/Amsterdam"));
    String utcBound = utcDataDateTimeFormatter.print(1651791600000L);
    String cetBound = cetDataDateTimeFormatter.print(1651791600000L);
    String lol ="lol";
  }

  @Test
  public void TestRemoveSimpleDateFormatPrefixWithNoPrefix() {
    final String timeColumnFormat = "yyyyMMdd";
    final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
    final String expected = "yyyyMMdd";

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void TestRemoveSimpleDateFormatPrefixWithSimpleDateFormatPrefix() {
    final String timeColumnFormat = "SIMPLE_DATE_FORMAT:yyyyMMdd";
    final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
    final String expected = "yyyyMMdd";

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void TestRemoveSimpleDateFormatPrefixWithFullPrefix() {
    final String timeColumnFormat = "1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd";
    final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
    final String expected = "yyyyMMdd";

    assertThat(output).isEqualTo(expected);
  }
}
