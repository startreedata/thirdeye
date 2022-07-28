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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.spi.Constants.JobStatus;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class uses weaving to control the java system time.
 *
 * Note: if run within IntelliJ, run with the following JVM option:
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar
 * IntelliJ does not use the pom surefire config: https://youtrack.jetbrains.com/issue/IDEA-52286
 *
 * In command line: ./mvnw -pl 'thirdeye-persistence' -Dtest=TestAnomalyJobManager test
 *
 */
public class TestAnomalyJobManager {

  private static final TimeProvider CLOCK = TimeProvider.instance();
  // use a time big enough because Timestamp(small int) parses to hours instead of millis
  public static final long JANUARY_1_2022 = 1640998861000L;

  private Long anomalyJobId1;
  private Long anomalyJobId2;
  private Long anomalyJobId3;

  private JobManager jobDAO;

  @BeforeClass
  void beforeClass() {
    // ensure time is controlled via the TimeProvider CLOCK - ie weaving is working correctly
    // advance time at each step
    assertThat(CLOCK.isTimeMockWorking()).isTrue();
    CLOCK.useMockTime(JANUARY_1_2022);  // JANUARY 1 2022
    jobDAO = new TestDatabase().createInjector().getInstance(JobManager.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    CLOCK.useSystemTime();
  }

  @Test
  public void testCreate() {
    CLOCK.tick(1);
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
    CLOCK.tick(1);
    JobStatus status = JobStatus.COMPLETED;
    long jobEndTime = System.currentTimeMillis();
    CLOCK.tick(1);
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
  public void testDeleteRecordsOlderThanDaysWithStatus() {
    JobStatus status = JobStatus.COMPLETED;
    CLOCK.tick(1);
    int numRecordsDeleted = jobDAO.deleteRecordsOlderThanDaysWithStatus(0, status);
    Assert.assertEquals(numRecordsDeleted, 2);
    List<JobDTO> anomalyJobs = jobDAO.findByStatus(status);
    Assert.assertEquals(anomalyJobs.size(), 0);
  }

  @Test(dependsOnMethods = {"testDeleteRecordsOlderThanDaysWithStatus"})
  public void testFindByStatusWithinDays() {
    anomalyJobId1 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId1);
    anomalyJobId2 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId2);
    anomalyJobId3 = jobDAO.save(DatalayerTestUtils.getTestJobSpec());
    Assert.assertNotNull(anomalyJobId3);

    CLOCK.tick(1);
    List<JobDTO> jobsWithZeroDays = jobDAO.findByStatusWithinDays(JobStatus.SCHEDULED, 0);
    Assert.assertEquals(jobsWithZeroDays.size(), 0);

    List<JobDTO> jobsWithOneDays = jobDAO.findByStatusWithinDays(JobStatus.SCHEDULED, 1);
    Assert.assertTrue(jobsWithOneDays.size() > 0);
  }
}
