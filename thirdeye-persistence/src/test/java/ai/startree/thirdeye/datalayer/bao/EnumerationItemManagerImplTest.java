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

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class EnumerationItemManagerImplTest {

  private EnumerationItemManager enumerationItemManager;

  @SuppressWarnings("unused")
  private static List<AlertDTO> toAlertList(final Long... alertIds) {
    return Arrays.stream(alertIds).map(alertId -> {
      final AlertDTO alert = new AlertDTO();
      alert.setId(alertId);
      return alert;
    }).collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
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
}
