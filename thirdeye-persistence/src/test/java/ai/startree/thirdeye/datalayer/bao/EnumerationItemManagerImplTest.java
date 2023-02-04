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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnumerationItemManagerImplTest {

  private EnumerationItemManager enumerationItemManager;

  private static List<AlertDTO> toAlertList(final Long... alertIds) {
    return Arrays.stream(alertIds).map(alertId -> {
      final AlertDTO alert = new AlertDTO();
      alert.setId(alertId);
      return alert;
    }).collect(Collectors.toList());
  }

  private static EnumerationItemDTO ei(final String ei1) {
    return new EnumerationItemDTO().setName(ei1);
  }

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    enumerationItemManager = injector.getInstance(EnumerationItemManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    enumerationItemManager.findAll().forEach(enumerationItemManager::delete);
  }

  @Test
  public void testFindExistingOrCreate() {
    // Create list of enumeration Item DTOs
    final EnumerationItemDTO ei1 = ei("ei1").setParams(Map.of("k1", "v1"));
    final EnumerationItemDTO ei2 = ei("ei2").setParams(Map.of("k2", "v2"))
        .setAlerts(toAlertList(123L, 456L));
    final EnumerationItemDTO ei3 = ei("ei3").setParams(Map.of("k3", "v3"));
    final List<EnumerationItemDTO> list = List.of(ei1, ei2, ei3);
    list.forEach(enumerationItemManager::save);

    final EnumerationItemDTO dtoNoMatch = enumerationItemManager.findExistingOrCreate(ei("ei2")
        .setParams(Map.of("k1", "v1")) // params are different
        .setAlerts(toAlertList(123L)));

    // assert that dtoNoMatch id is not in the list of enumeration item ids
    assertThat(list.stream()
        .map(EnumerationItemDTO::getId)
        .collect(Collectors.toList()))
        .doesNotContain(dtoNoMatch.getId());

    final EnumerationItemDTO dto1 = enumerationItemManager.findExistingOrCreate(ei("ei1")
        .setParams(Map.of("k1", "v1"))
        .setAlerts(toAlertList(123L)));

    assertThat(dto1.getName()).isEqualTo(ei1.getName());

    final EnumerationItemDTO dto2 = enumerationItemManager.findExistingOrCreate(ei("ei2")
        .setParams(Map.of("k2", "v2"))
        .setAlerts(toAlertList(123L)));

    assertThat(dto2.getName()).isEqualTo(ei2.getName());
    assertThat(dto2.getAlerts()).isEqualTo(ei2.getAlerts());

    final EnumerationItemDTO dto3 = enumerationItemManager.findExistingOrCreate(ei("ei3")
        .setParams(Map.of("k3", "v3"))
        .setAlerts(toAlertList(123L)));

    assertThat(dto3.getName()).isEqualTo(ei3.getName());
    assertThat(dto3.getAlerts()).containsExactlyInAnyOrderElementsOf(toAlertList(123L));

    final EnumerationItemDTO dto4 = enumerationItemManager.findExistingOrCreate(ei("ei2")
        .setParams(Map.of("k2", "v2"))
        .setAlerts(toAlertList(789L)));

    assertThat(dto4.getName()).isEqualTo(ei2.getName());
    assertThat(dto4.getAlerts()).containsExactlyInAnyOrderElementsOf(toAlertList(
        123L, 456L, 789L));
  }
}
