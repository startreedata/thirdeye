package org.apache.pinot.thirdeye.spi.detection.v2;

import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ColumnTypeTest {

  @Test
  public void testArrayTypeCheck() {
    Assert.assertFalse(new ColumnType(ColumnDataType.INT).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.LONG).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.FLOAT).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.DOUBLE).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.STRING).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.BOOLEAN).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.DATE).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.BYTES).isArray());
    Assert.assertFalse(new ColumnType(ColumnDataType.OBJECT).isArray());
    Assert.assertTrue(new ColumnType(ColumnDataType.INT_ARRAY).isArray());
    Assert.assertTrue(new ColumnType(ColumnDataType.LONG_ARRAY).isArray());
    Assert.assertTrue(new ColumnType(ColumnDataType.FLOAT_ARRAY).isArray());
    Assert.assertTrue(new ColumnType(ColumnDataType.DOUBLE_ARRAY).isArray());
    Assert.assertTrue(new ColumnType(ColumnDataType.STRING_ARRAY).isArray());
  }
}
