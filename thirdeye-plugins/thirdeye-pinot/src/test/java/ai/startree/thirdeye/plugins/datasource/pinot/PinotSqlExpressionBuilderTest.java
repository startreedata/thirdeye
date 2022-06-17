/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotSqlExpressionBuilder.removeSimpleDateFormatPrefix;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotSqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import org.joda.time.Period;
import org.testng.annotations.Test;

public class PinotSqlExpressionBuilderTest implements SqlExpressionBuilder {

  @Test
  public void testRemoveSimpleDateFormatPrefixWithNoPrefix() {
    final String timeColumnFormat = "yyyyMMdd";
    final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
    final String expected = "yyyyMMdd";

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testRemoveSimpleDateFormatPrefixWithSimpleDateFormatPrefix() {
    final String timeColumnFormat = "SIMPLE_DATE_FORMAT:yyyyMMdd";
    final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
    final String expected = "yyyyMMdd";

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testRemoveSimpleDateFormatPrefixWithFullPrefix() {
    final String timeColumnFormat = "1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd";
    final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
    final String expected = "yyyyMMdd";

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetTimeGroupExpressionWithEscapedLiteralQuote() {
    final SqlExpressionBuilder pinotExpressionBuilder = new PinotSqlExpressionBuilder();
    final String output = pinotExpressionBuilder.getTimeGroupExpression("timeCol",
        "yyyyMMdd'T'HH:mm:ss'Z'",
        Period.days(1),
        null);
    final String expected = " DATETIMECONVERT(timeCol, '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd''T''HH:mm:ss''Z''', '1:MILLISECONDS:EPOCH', '1:DAYS') ";

    assertThat(output).isEqualTo(expected);
  }
}
