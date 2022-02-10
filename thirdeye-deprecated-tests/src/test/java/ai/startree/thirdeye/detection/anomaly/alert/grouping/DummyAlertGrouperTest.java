/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DummyAlertGrouperTest {

  @Test(dataProvider = "prepareAnomalyGroups", dataProviderClass = DimensionalAlertGrouperTest.class)
  public void testGroup(List<MergedAnomalyResultDTO> anomalies,
      Set<MergedAnomalyResultDTO> expectedGroup1,
      Set<MergedAnomalyResultDTO> expectedGroup2, Set<MergedAnomalyResultDTO> expectedRollUpGroup) {
    DummyAlertGrouper alertGrouper = new DummyAlertGrouper();

    Map<DimensionMap, GroupedAnomalyResultsDTO> groupedAnomalies = alertGrouper.group(anomalies);
    Assert.assertEquals(groupedAnomalies.size(), 1);

    Set<MergedAnomalyResultDTO> expectedAnomalySet = new HashSet<>();
    expectedAnomalySet.addAll(anomalies);

    List<MergedAnomalyResultDTO> actualAnomalies = groupedAnomalies.get(new DimensionMap())
        .getAnomalyResults();
    Assert.assertEquals(actualAnomalies.size(), anomalies.size());

    Set<MergedAnomalyResultDTO> actualAnomalySet = new HashSet<>();
    actualAnomalySet.addAll(actualAnomalies);
    Assert.assertEquals(actualAnomalySet, expectedAnomalySet);
  }
}
