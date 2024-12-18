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
package ai.startree.thirdeye.scheduler;

import static ai.startree.thirdeye.scheduler.JobUtils.currentCron;
import static ai.startree.thirdeye.scheduler.JobUtils.getIdFromJobKey;
import static ai.startree.thirdeye.spi.Constants.CRON_TIMEZONE;
import static ai.startree.thirdeye.spi.util.TimeUtils.maximumTriggersPerMinute;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskCronSchedulerRunnable<E extends AbstractDTO> implements Runnable {
  
  private final Logger log;
  private final Scheduler scheduler;
  private final TaskType taskType;
  private final GroupMatcher<JobKey> groupMatcher;
  private final int cronMaxTriggersPerMinute;
  private final AbstractManager<E> entityDao;
  private final String entityName;
  private final Class<? extends Job> jobClazz;
  private final CronGetter<E> cronGetter;
  private final isActiveGetter<E> isActiveGetter;

  public TaskCronSchedulerRunnable(
      final AbstractManager<E> entityDao,
      final CronGetter<E> cronGetter,
      final isActiveGetter<E> isActiveGetter,
      final Class<E> entityClazz,
      final TaskType taskType,
      final Class<? extends Job> jobClazz,
      final GuiceJobFactory guiceJobFactory,
      final int cronMaxTriggersPerMinute,
      final Class<?> loggerClass) {
    try {
      scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.setJobFactory(guiceJobFactory);
      scheduler.start();
    } catch (final SchedulerException e) {
      throw new RuntimeException("Failed to initialize the %s scheduler".formatted(taskType), e);
    }
    this.entityDao = entityDao;
    this.cronGetter = cronGetter;
    this.isActiveGetter = isActiveGetter;
    this.entityName = entityClazz.getSimpleName();
    this.taskType = taskType;
    this.jobClazz = jobClazz;
    this.cronMaxTriggersPerMinute = cronMaxTriggersPerMinute;
    this.log = LoggerFactory.getLogger(loggerClass);

    this.groupMatcher = GroupMatcher.jobGroupEquals(taskType.toString());
    
  }

  @Override
  public void run() {
    // catch all exceptions to prevent unscheduling - this is run in executorService.scheduleWithFixedDelay
    try {
      updateSchedules();
    } catch (final Exception e) {
      log.error("Error updating {} task creation schedules", taskType, e);
    }
  }

  public void shutdown() throws SchedulerException {
    scheduler.shutdown();
  }

  private void updateSchedules() throws SchedulerException {
    // TODO CYRIL scale - loading all entities is expensive - only the id and the cron are necessary - requires custom SQL (JOOQ)
    // FIXME CYRIL SCALE - No need for a strong isolation level here - the default isolation level lock all entities until the query is finished, blocking progress of tasks and potentially some update/delete operations in the UI.
    //   dirty reads are be fine, this logic runs every minute
    //   also only fetch only active entities directly and remove is active from Schedulable interface 
    final List<E> allEntities = entityDao.findAll();

    // schedule active entities
    allEntities.forEach(this::schedule);

    // cleanup schedules of deleted and deactivated entities
    final Map<Long, E> idToEntity = allEntities.stream()
        .collect(Collectors.toMap(AbstractDTO::getId, e -> e));
    final Set<JobKey> scheduledJobKeys = scheduler.getJobKeys(groupMatcher);
    for (final JobKey jobKey : scheduledJobKeys) {
      try {
        final Long id = getIdFromJobKey(jobKey);
        final E entity = idToEntity.get(id);
        if (entity == null) {
          log.info("{} with id {} does not exist anymore. Stopping the scheduled {} job.",
              entityName, id, taskType);
          stopJob(jobKey);
        } else if (!isActive(entity)) {
          log.info("{} with id {} is deactivated. Stopping the scheduled {} job.", entityName, id, taskType);
          stopJob(jobKey);
        }
      } catch (final Exception e) {
        log.error("Error removing job key {}", jobKey, e);
      }
    }
  }

  private void schedule(final E entity) {
    if (!isActive(entity)) {
      log.debug("{}: {} is inactive. Skipping.", entityName, entity.getId());
      return;
    }

    // schedule job: add or update job
    try {
      final String jobName = taskType + "_" + entity.getId();
      final JobKey jobKey = new JobKey(jobName, taskType.toString());
      if (scheduler.checkExists(jobKey)) {
        log.debug("{} {} is already scheduled", entityName, jobKey.getName());
        final String currentCron = currentCron(scheduler, jobKey);
        if (!cronOf(entity).equals(currentCron)) {
          log.info("Cron expression of {} {} has been changed from {} to {}. "
                  + "Restarting schedule",
              entityName, entity.getId(), currentCron, cronOf(entity));
          stopJob(jobKey);
          startJob(entity, jobKey);
        }
      } else {
        startJob(entity, jobKey);
      }
    } catch (final Exception e) {
      log.error("Error creating/updating job key for {} config {}", taskType, entity.getId(), e);
    }
  }

  public void startJob(final E config, final JobKey jobKey) throws SchedulerException {
    final Trigger trigger = buildTrigger(config);
    final JobDetail job = JobBuilder.newJob(jobClazz)
        .withIdentity(jobKey)
        .build();
    scheduler.scheduleJob(job, trigger);
    log.info("Scheduled {} job {}", taskType, jobKey.getName());
  }

  private void stopJob(final JobKey jobKey) throws SchedulerException {
    if (!scheduler.checkExists(jobKey)) {
      log.error("Could not find job to delete {}, {} in the job scheduler. This should never happen. Please reach out to StarTree support.", jobKey.getName(), jobKey.getGroup());
    }
    scheduler.deleteJob(jobKey);
    log.info("Stopped {} job {}", taskType, jobKey.getName());
  }


  private Trigger buildTrigger(final E config) {
    final String cron = cronOf(config);
    final int maxTriggersPerMinute = maximumTriggersPerMinute(cron);
    checkArgument(maxTriggersPerMinute <= cronMaxTriggersPerMinute,
        "Attempting to schedule a %s job for %s %s that can trigger up to %s times per minute. The limit is %s. Please update the cron %s",
        taskType,
        entityName, 
        config.getId(),
        maxTriggersPerMinute, cronMaxTriggersPerMinute, cron
    );
    final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
        .cronSchedule(cron)
        .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE));
    return TriggerBuilder.newTrigger()
        .withSchedule(cronScheduleBuilder)
        .build();
  }
  
  private boolean isActive(final E entity) {
    return isActiveGetter.isActive(entity);
  }

  private String cronOf(final E entity) {
    return cronGetter.getCron(entity);
  }
  
  public interface CronGetter<E extends AbstractDTO> {
    String getCron(final E entity);
  }

  public interface isActiveGetter<E extends AbstractDTO> {
    boolean isActive(final E entity);
  }
}
