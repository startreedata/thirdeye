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
import ai.startree.thirdeye.spi.Constants.JobStatus;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class TestAnomalyJobManager {

  // some tests can be flaky because of the latencies caused by the dockerized MySQL
  private static final int FLAKY_TEST_SLEEP = 100;

  private Long anomalyJobId1;
  private Long anomalyJobId2;
  private Long anomalyJobId3;

  private JobManager jobDAO;

  @BeforeClass
  void beforeClass() {
    jobDAO = new TestDatabase().createInjector().getInstance(JobManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {

  }

  @Test
  public void testCreate() {
    anomalyJobId1 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId1);
    anomalyJobId2 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId2);
    anomalyJobId3 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId3);
    printAll("After insert");
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFindAll() {
    List<JobDTO> anomalyJobs = jobDAO.findAll();
    Assert.assertEquals(anomalyJobs.size(), 3);
  }

  @Test(dependsOnMethods = {"testFindAll"})
  public void testUpdateStatusAndJobEndTime() {
    JobStatus status = JobStatus.COMPLETED;
    long jobEndTime = System.currentTimeMillis();
    List<JobDTO> jobDTOs = jobDAO.findByIds(Arrays.asList(anomalyJobId1, anomalyJobId3));
    jobDAO.updateJobStatusAndEndTime(jobDTOs, status, jobEndTime);
    JobDTO anomalyJob = jobDAO.findById(anomalyJobId1);
    Assert.assertEquals(anomalyJob.getStatus(), status);
    Assert.assertEquals(anomalyJob.getScheduleEndTime(), jobEndTime);
    printAll("After testUpdateStatusAndJobEndTime");
  }

  @Test(dependsOnMethods = {"testUpdateStatusAndJobEndTime"})
  public void testFindByStatus() {
    JobStatus status = JobStatus.COMPLETED;
    List<JobDTO> anomalyJobs = jobDAO.findByStatus(status);
    Assert.assertEquals(anomalyJobs.size(), 2);
    Assert.assertEquals(anomalyJobs.get(0).getStatus(), status);
  }

  private void printAll(String msg) {
    List<JobDTO> allAnomalyJobs = jobDAO.findAll();
    System.out.println("START:ALL JOB after:" + msg);
    for (JobDTO jobDTO : allAnomalyJobs) {
      System.out.println(jobDTO);
    }
    System.out.println("END:ALL JOB after:" + msg);
  }

  @Test(dependsOnMethods = {"testFindByStatus"})
  public void testDeleteRecordsOlderThanDaysWithStatus() throws InterruptedException {
    Thread.sleep(FLAKY_TEST_SLEEP);
    JobStatus status = JobStatus.COMPLETED;
    int numRecordsDeleted = jobDAO.deleteRecordsOlderThanDaysWithStatus(0, status);
    Assert.assertEquals(numRecordsDeleted, 2);
    List<JobDTO> anomalyJobs = jobDAO.findByStatus(status);
    Assert.assertEquals(anomalyJobs.size(), 0);
  }

  @Test(dependsOnMethods = {"testDeleteRecordsOlderThanDaysWithStatus"})
  public void testFindByStatusWithinDays() throws InterruptedException {
    anomalyJobId1 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId1);
    anomalyJobId2 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId2);
    anomalyJobId3 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId3);

    Thread.sleep(300 + FLAKY_TEST_SLEEP); // To ensure every job has been created more than 1 ms ago

    List<JobDTO> jobsWithZeroDays = jobDAO.findByStatusWithinDays(JobStatus.SCHEDULED, 0);
    Assert.assertEquals(jobsWithZeroDays.size(), 0);

    List<JobDTO> jobsWithOneDays = jobDAO.findByStatusWithinDays(JobStatus.SCHEDULED, 1);
    Assert.assertTrue(jobsWithOneDays.size() > 0);
  }
}
