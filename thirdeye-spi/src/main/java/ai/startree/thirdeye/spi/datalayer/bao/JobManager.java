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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.Constants.JobStatus;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import java.util.List;

public interface JobManager extends AbstractManager<JobDTO> {

  List<JobDTO> findByStatus(JobStatus status);

  List<JobDTO> findByStatusWithinDays(JobStatus status, int days);

  void updateJobStatusAndEndTime(List<JobDTO> jobsToUpdate, JobStatus nweStatus, long newEndTime);

  int deleteRecordsOlderThanDaysWithStatus(int days, JobStatus status);

  List<JobDTO> findNRecentJobs(int n);

  String getJobNameByJobId(long id);

  List<JobDTO> findRecentScheduledJobByTypeAndConfigId(TaskType taskType,
      long configId, long minScheduledTime);
}
