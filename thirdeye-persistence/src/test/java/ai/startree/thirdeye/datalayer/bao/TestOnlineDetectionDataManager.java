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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import ai.startree.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestOnlineDetectionDataManager {

  private OnlineDetectionDataManager dataDAO;

  @BeforeMethod
  void beforeMethod() {
    dataDAO = new TestDatabase().createInjector().getInstance(OnlineDetectionDataManager.class);
  }

  @Test
  public void testFindByDatasetAndMetric() {
    String datasetName1 = "dataset1";
    String metricName1 = "metric1";
    OnlineDetectionDataDTO onlineDetectionDataDTO1 = new OnlineDetectionDataDTO();
    onlineDetectionDataDTO1.setDataset(datasetName1);
    onlineDetectionDataDTO1.setMetric(metricName1);

    String datasetName2 = "dataset2";
    String metricName2 = "metric2";
    OnlineDetectionDataDTO onlineDetectionDataDTO2 = new OnlineDetectionDataDTO();
    onlineDetectionDataDTO2.setDataset(datasetName2);
    onlineDetectionDataDTO2.setMetric(metricName2);

    Long id1 = dataDAO.save(onlineDetectionDataDTO1);
    Long id2 = dataDAO.save(onlineDetectionDataDTO2);

    List<OnlineDetectionDataDTO> res1
        = dataDAO.findByDatasetAndMetric(datasetName1, metricName1);

    Assert.assertEquals(res1.size(), 1);
    Assert.assertEquals(res1.get(0).getId(), id1);

    List<OnlineDetectionDataDTO> res2
        = dataDAO.findByDatasetAndMetric(datasetName2, metricName2);

    Assert.assertEquals(res2.size(), 1);
    Assert.assertEquals(res2.get(0).getId(), id2);

    List<OnlineDetectionDataDTO> res3
        = dataDAO.findByDatasetAndMetric(datasetName2, metricName1);

    Assert.assertEquals(res3.size(), 0);
  }
}
