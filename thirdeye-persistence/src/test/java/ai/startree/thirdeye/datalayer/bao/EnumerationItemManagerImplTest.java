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

package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.util.SpiUtils.alertRef;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Injector;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

public class EnumerationItemManagerImplTest {

  public static final long ALERT_ID = 1234L;
  private AnomalyManager anomalyManager;
  private SubscriptionGroupManager subscriptionGroupManager;
  private EnumerationItemManagerImpl enumerationItemManager;

  private static EnumerationItemDTO ei(final String name) {
    return ei(name, Map.of());
  }

  private static EnumerationItemDTO ei(final String name, final Map<String, Object> m) {
    return new EnumerationItemDTO().setName(name).setParams(m);
  }

  private static EnumerationItemDTO sourceEi() {
    return ei("ei1", Map.of("a", 1))
        .setAlert(alertRef(ALERT_ID));
  }

  private static AnomalyDTO anomaly(final long startTime, final long endTime) {
    return new AnomalyDTO()
        .setStartTime(startTime)
        .setEndTime(endTime);
  }

  private static AnomalyDTO anomaly() {
    return anomaly(1000L, 2000L);
  }

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    anomalyManager = injector.getInstance(AnomalyManager.class);
    subscriptionGroupManager = injector.getInstance(SubscriptionGroupManager.class);
    enumerationItemManager = injector.getInstance(EnumerationItemManagerImpl.class);
  }

  @AfterMethod
  void afterClass() {
    enumerationItemManager.findAll().forEach(enumerationItemManager::delete);
    anomalyManager.findAll().forEach(anomalyManager::delete);
    subscriptionGroupManager.findAll().forEach(subscriptionGroupManager::delete);
  }
}
