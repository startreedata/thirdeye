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
package ai.startree.thirdeye.datalayer.dao;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AnomalyEntityTest {

  private GenericPojoDao dao;

  @BeforeClass
  void beforeClass() {
    dao = MySqlTestDatabase.sharedInjector().getInstance(GenericPojoDao.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    dao.deleteByPredicate(Predicate.NEQ("baseId", 0), AnomalyDTO.class);
  }

  private AnomalyDTO buildAnomaly() {
    return new AnomalyDTO();
  }

  private AnomalyLabelDTO buildLabel(final boolean ignore) {
    return new AnomalyLabelDTO().setIgnore(ignore);
  }

  @Test
  public void ignoreLabelIndexTest() {
    dao.create(buildAnomaly());
    final List<AnomalyLabelDTO> labels = List.of(buildLabel(false), buildLabel(true));
    dao.create(buildAnomaly().setAnomalyLabels(labels));

    final List<AnomalyDTO> notIgnored = dao.filter(new DaoFilter()
        .setBeanClass(AnomalyDTO.class)
        .setPredicate(Predicate.EQ("ignored", false)));
    final List<AnomalyDTO> ignored = dao.filter(new DaoFilter()
        .setBeanClass(AnomalyDTO.class)
        .setPredicate(Predicate.EQ("ignored", true)));

    assertThat(notIgnored).isNotEmpty();
    notIgnored.forEach(anomaly -> assertThat(isIgnored(anomaly)).isFalse());
    assertThat(ignored).isNotEmpty();
    ignored.forEach(anomaly -> assertThat(isIgnored(anomaly)).isTrue());
  }

  private boolean isIgnored(final AnomalyDTO anomaly) {
    final List<AnomalyLabelDTO> labels = anomaly.getAnomalyLabels();
    if (labels == null) {
      return false;
    }
    for (AnomalyLabelDTO label : labels) {
      if (label.isIgnore()) {
        return true;
      }
    }
    return false;
  }
}
