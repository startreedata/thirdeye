/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
