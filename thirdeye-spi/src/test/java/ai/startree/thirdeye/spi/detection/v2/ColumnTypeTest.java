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
package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
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
