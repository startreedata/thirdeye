package org.apache.pinot.thirdeye.datalayer.bao;

import java.util.List;
import org.apache.pinot.thirdeye.datalayer.TestDatabase;
import org.apache.pinot.thirdeye.spi.datalayer.bao.OnlineDetectionDataManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;
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
