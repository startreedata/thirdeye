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

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.DetectionStatusManager;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestDetectionStatusManager {

  private static final String collection1 = "my dataset1";
  private final DateTime now = new DateTime(DateTimeZone.UTC);
  private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHH");
  private Long detectionStatusId1;
  private Long detectionStatusId2;
  private DetectionStatusManager detectionStatusDAO;

  @BeforeClass
  void beforeClass() {
    detectionStatusDAO = new TestDatabase()
        .createInjector()
        .getInstance(DetectionStatusManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {

  }

  @Test
  public void testCreate() {

    String dateString = dateTimeFormatter.print(now.getMillis());
    long dateMillis = dateTimeFormatter.parseMillis(dateString);
    detectionStatusId1 = detectionStatusDAO
        .save(DatalayerTestUtils.getTestDetectionStatus(collection1,
            dateMillis,
            dateString,
            false,
            1));
    detectionStatusDAO
        .save(DatalayerTestUtils.getTestDetectionStatus(collection1,
            dateMillis,
            dateString,
            true,
            2));

    dateMillis = new DateTime(dateMillis, DateTimeZone.UTC).minusHours(1).getMillis();
    dateString = dateTimeFormatter.print(dateMillis);
    detectionStatusId2 = detectionStatusDAO.
        save(DatalayerTestUtils.getTestDetectionStatus(collection1,
            dateMillis,
            dateString,
            true,
            1));
    detectionStatusDAO
        .save(DatalayerTestUtils.getTestDetectionStatus(collection1,
            dateMillis,
            dateString,
            true,
            2));

    dateMillis = new DateTime(dateMillis, DateTimeZone.UTC).minusHours(1).getMillis();
    dateString = dateTimeFormatter.print(dateMillis);
    detectionStatusDAO
        .save(DatalayerTestUtils.getTestDetectionStatus(collection1,
            dateMillis,
            dateString,
            true,
            2));

    Assert.assertNotNull(detectionStatusId1);
    Assert.assertNotNull(detectionStatusId2);

    List<DetectionStatusDTO> detectionStatusDTOs = detectionStatusDAO.findAll();
    Assert.assertEquals(detectionStatusDTOs.size(), 5);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFind() {
    DetectionStatusDTO detectionStatusDTO = detectionStatusDAO.findLatestEntryForFunctionId(1);
    String dateString = dateTimeFormatter.print(now.getMillis());
    Assert.assertEquals(detectionStatusDTO.getFunctionId(), 1);
    Assert.assertEquals(detectionStatusDTO.getDateToCheckInSDF(), dateString);
    Assert.assertEquals(detectionStatusDTO.isDetectionRun(), false);

    long dateMillis = dateTimeFormatter.parseMillis(dateString);
    dateMillis = new DateTime(dateMillis, DateTimeZone.UTC).minusHours(1).getMillis();

    List<DetectionStatusDTO> detectionStatusDTOs = detectionStatusDAO.
        findAllInTimeRangeForFunctionAndDetectionRun(dateMillis, now.getMillis(), 2, true);
    Assert.assertEquals(detectionStatusDTOs.size(), 2);
    detectionStatusDTOs = detectionStatusDAO.
        findAllInTimeRangeForFunctionAndDetectionRun(dateMillis, now.getMillis(), 2, false);
    Assert.assertEquals(detectionStatusDTOs.size(), 0);
  }
}
