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

import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class SubscriptionGroupFilterTest {

  @Test
  public void testFilterAnomalies() {
    final SubscriptionGroupFilter instance = new SubscriptionGroupFilter(
        mock(AnomalyManager.class),
        mock(AlertManager.class)
    );
    instance.filterAnomalies(new AnomalyFilter()
        .setAlertId(123L)
        .setIsChild(false)
        .setCreateTimeWindow(new Interval(1230L, 1234L)), 5678L, "");
  }
}