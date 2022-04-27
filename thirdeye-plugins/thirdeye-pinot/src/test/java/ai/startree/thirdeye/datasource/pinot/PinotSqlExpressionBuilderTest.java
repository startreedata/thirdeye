/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import static ai.startree.thirdeye.datasource.pinot.PinotSqlExpressionBuilder.removeSimpleDateFormatPrefix;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import org.testng.annotations.Test;

public class PinotSqlExpressionBuilderTest implements SqlExpressionBuilder {

 @Test
  public void TestRemoveSimpleDateFormatPrefixWithNoPrefix() {
   final String timeColumnFormat= "yyyyMMdd";
   final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
   final String expected = "yyyyMMdd";

   assertThat(output).isEqualTo(expected);
 }

 @Test
 public void TestRemoveSimpleDateFormatPrefixWithSimpleDateFormatPrefix() {
  final String timeColumnFormat= "SIMPLE_DATE_FORMAT:yyyyMMdd";
  final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
  final String expected = "yyyyMMdd";

  assertThat(output).isEqualTo(expected);
 }

 @Test
 public void TestRemoveSimpleDateFormatPrefixWithFullPrefix() {
  final String timeColumnFormat= "1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd";
  final String output = removeSimpleDateFormatPrefix(timeColumnFormat);
  final String expected = "yyyyMMdd";

  assertThat(output).isEqualTo(expected);
 }
}
