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
package ai.startree.thirdeye.datalayer.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Injector;
import java.util.List;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnumerationItemMaintainerTest {

  public static final long ALERT_ID = 1234L;
  private AnomalyManager anomalyManager;
  private SubscriptionGroupManager subscriptionGroupManager;
  private EnumerationItemMaintainer enumerationItemMaintainer;
  private EnumerationItemManager enumerationItemManager;

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    anomalyManager = injector.getInstance(AnomalyManager.class);
    subscriptionGroupManager = injector.getInstance(SubscriptionGroupManager.class);
    enumerationItemMaintainer = injector.getInstance(EnumerationItemMaintainer.class);
    enumerationItemManager = injector.getInstance(EnumerationItemManager.class);
  }

  @AfterMethod
  void afterClass() {
    enumerationItemManager.findAll().forEach(enumerationItemManager::delete);
    anomalyManager.findAll().forEach(anomalyManager::delete);
    subscriptionGroupManager.findAll().forEach(subscriptionGroupManager::delete);
  }

  @Test
  public void testSyncWithNoAuth() {
    final List<String> idKeys = List.of("key");
    final EnumerationItemDTO inputItem = (EnumerationItemDTO) new EnumerationItemDTO().setName("ei1")
        .setParams(Map.of("key", 1))
        .setAuth(new AuthorizationConfigurationDTO().setNamespace("NAMESPACE_OVERRIDEN_BY_SYNC"));
    List<EnumerationItemDTO> items = List.of(inputItem);
    // Testing with no auth
    final String namespace = null;
    List<EnumerationItemDTO> synced = enumerationItemMaintainer.sync(items, idKeys, ALERT_ID, namespace);
    assertThat(synced.size()).isEqualTo(1);
    assertThat(synced.get(0).getId()).isNotNull();
    assertThat(synced.get(0).getParams()).isEqualTo(items.get(0).getParams());
    assertThat(synced.get(0).namespace()).isNull();
  }

  @Test
  public void testSyncWithAuth() {
    final List<String> idKeys = List.of("key");
    final EnumerationItemDTO inputItem = (EnumerationItemDTO) new EnumerationItemDTO().setName("ei1")
        .setParams(Map.of("key", 1))
        .setAuth(new AuthorizationConfigurationDTO().setNamespace("NAMESPACE_OVERRIDEN_BY_SYNC"));
    List<EnumerationItemDTO> items = List.of(inputItem);
    // Testing with no auth
    final String namespace = "THE_NAMESPACE";
    List<EnumerationItemDTO> synced = enumerationItemMaintainer.sync(items, idKeys, ALERT_ID, namespace);
    assertThat(synced.size()).isEqualTo(1);
    assertThat(synced.get(0).getId()).isNotNull();
    assertThat(synced.get(0).getParams()).isEqualTo(items.get(0).getParams());
    assertThat(synced.get(0).namespace()).isEqualTo("THE_NAMESPACE");
  }
}
