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

package ai.startree.thirdeye.plugins.rca.contributors.cube.data;

import org.testng.annotations.Test;

public class DimNameValueCostEntryTest {

  @Test
  public void testCreation() {
    // test that constructor is working
    new DimNameValueCostEntry("", "", 0, 0, 0d, 0d, 0, 0, 0, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDimensionNameCreation() {
    new DimNameValueCostEntry(null, "", 0, 0, 0d, 0d, 0, 0, 0, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDimensionValueCreation() {
    new DimNameValueCostEntry("", null, 0, 0, 0d, 0d, 0, 0, 0, 0);
  }
}
