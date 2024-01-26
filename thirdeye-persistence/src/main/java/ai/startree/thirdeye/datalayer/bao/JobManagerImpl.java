/*
 * Copyright 2024 StarTree Inc
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

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.Constants.JobStatus;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.sql.Timestamp;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Singleton
public class JobManagerImpl extends AbstractManagerImpl<JobDTO> implements JobManager {

  @Inject
  public JobManagerImpl(GenericPojoDao genericPojoDao) {
    super(JobDTO.class, genericPojoDao);
  }

  @Override
  @Transactional
  public List<JobDTO> findByStatus(JobStatus status) {
    return super.findByPredicate(Predicate.EQ("status", status.toString()));
  }

  @Override
  public List<JobDTO> findByStatusWithinDays(JobStatus status, int days) {
    DateTime activeDate = new DateTime(System.currentTimeMillis(),
        DateTimeZone.UTC).minusDays(days);
    Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    Predicate statusPredicate = Predicate.EQ("status", status.toString());
    Predicate timestampPredicate = Predicate.GE("createTime", activeTimestamp);
    return findByPredicate(Predicate.AND(statusPredicate, timestampPredicate));
  }

  @Override
  @Transactional
  public void updateJobStatusAndEndTime(List<JobDTO> jobsToUpdate, JobStatus newStatus,
      long newEndTime) {
    Preconditions.checkNotNull(newStatus);
    if (CollectionUtils.isNotEmpty(jobsToUpdate)) {
      for (JobDTO jobDTO : jobsToUpdate) {
        jobDTO.setStatus(newStatus);
        jobDTO.setScheduleEndTime(newEndTime);
      }
      update(jobsToUpdate);
    }
  }

  @Override
  @Transactional
  public int deleteRecordsOlderThanDaysWithStatus(int days, JobStatus status) {
    DateTime expireDate = new DateTime(System.currentTimeMillis(),
        DateTimeZone.UTC).minusDays(days);
    Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());
    Predicate statusPredicate = Predicate.EQ("status", status.toString());
    Predicate timestampPredicate = Predicate.LT("updateTime", expireTimestamp);
    return deleteByPredicate(Predicate.AND(statusPredicate, timestampPredicate));
  }
}
