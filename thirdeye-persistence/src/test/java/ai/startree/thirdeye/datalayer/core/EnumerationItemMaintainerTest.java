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

import static ai.startree.thirdeye.spi.util.SpiUtils.alertRef;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
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

  private static EnumerationItemDTO ei(final String name, final Map<String, Object> m) {
    return new EnumerationItemDTO().setName(name).setParams(m);
  }

  private static EnumerationItemDTO eiWithAuth(final String name,
      final Map<String, Object> params,
      final String namespace) {
    final EnumerationItemDTO ei = ei(name, params);
    ei.setAuth(auth(namespace));
    return ei;
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

  private static AuthorizationConfigurationDTO auth(final String namespace) {
    return new AuthorizationConfigurationDTO().setNamespace(namespace);
  }

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
  public void testSyncWithAuth() {
    final List<String> idKeys = List.of("key");
    List<EnumerationItemDTO> items = List.of(
        ei("ei1", Map.of("key", 1))
    );

    List<EnumerationItemDTO> synced;
    String namespace;

    // Testing with no auth
    synced = enumerationItemMaintainer.sync(items, idKeys, ALERT_ID, null);
    assertThat(synced.size()).isEqualTo(1);
    assertThat(synced.get(0).getId()).isNotNull();
    assertThat(synced.get(0).getParams()).isEqualTo(items.get(0).getParams());

    // Testing with auth add
    namespace = "ns1";
    items = List.of(
        eiWithAuth("ei1", Map.of("key", 1), namespace)
    );
    synced = enumerationItemMaintainer.sync(items, idKeys, ALERT_ID, null);
    assertThat(synced.size()).isEqualTo(1);
    assertThat(synced.get(0).getId()).isNotNull();
    assertThat(synced.get(0).getParams()).isEqualTo(items.get(0).getParams());
    assertThat(synced.get(0).getAuth()).isNotNull();
    assertThat(synced.get(0).getAuth().getNamespace()).isEqualTo(namespace);

    // Testing with auth modify
    namespace = "ns2";
    items = List.of(
        eiWithAuth("ei1", Map.of("key", 1), namespace)
    );
    synced = enumerationItemMaintainer.sync(items, idKeys, ALERT_ID, null);
    assertThat(synced.size()).isEqualTo(1);
    assertThat(synced.get(0).getId()).isNotNull();
    assertThat(synced.get(0).getParams()).isEqualTo(items.get(0).getParams());
    assertThat(synced.get(0).getAuth()).isNotNull();
    assertThat(synced.get(0).getAuth().getNamespace()).isEqualTo(namespace);

    // Testing with auth remove
    items = List.of(
        ei("ei1", Map.of("key", 1))
    );
    synced = enumerationItemMaintainer.sync(items, idKeys, ALERT_ID, null);
    assertThat(synced.size()).isEqualTo(1);
    assertThat(synced.get(0).getId()).isNotNull();
    assertThat(synced.get(0).getParams()).isEqualTo(items.get(0).getParams());
    assertThat(synced.get(0).getAuth()).isNull();
  }
}
