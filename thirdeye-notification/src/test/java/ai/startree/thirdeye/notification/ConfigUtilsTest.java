/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigUtilsTest {

  @Test
  public void testGetListNull() {
    Assert.assertTrue(SubscriptionGroupFilterUtils.getList(null).isEmpty());
  }

  @Test
  public void testGetListPartialNull() {
    Assert.assertEquals(SubscriptionGroupFilterUtils.getList(Arrays.asList("a", null)).size(), 2);
  }

  @Test
  public void testGetLongsNull() {
    Assert.assertTrue(SubscriptionGroupFilterUtils.getLongs(null).isEmpty());
  }

  @Test
  public void testGetLongsPartialNull() {
    Assert.assertEquals(SubscriptionGroupFilterUtils.getLongs(Arrays.asList(1L, null, 2L)).size(), 2);
  }

  @Test
  public void testGetListModification() {
    List<String> defaultList = new ArrayList<>();
    List<String> list = SubscriptionGroupFilterUtils.getList(null, defaultList);
    list.add("value");
    Assert.assertNotEquals(list, defaultList);
  }
}
