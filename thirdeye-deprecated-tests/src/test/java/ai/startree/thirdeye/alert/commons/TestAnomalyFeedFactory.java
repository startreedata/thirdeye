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
package ai.startree.thirdeye.alert.commons;

import ai.startree.thirdeye.detection.alert.AnomalyFeedFactory;
import ai.startree.thirdeye.detection.alert.feed.AnomalyFeed;
import ai.startree.thirdeye.detection.alert.feed.UnionAnomalyFeed;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAnomalyFeedFactory {

  @Test(enabled = false)
  public void testCreateAlertFeed() throws Exception {
    AnomalyFeed anomalyFeed = AnomalyFeedFactory.fromClassName("UnionAnomalyFeed");
    Assert.assertNotNull(anomalyFeed);
    Assert.assertTrue(anomalyFeed instanceof UnionAnomalyFeed);
  }
}
