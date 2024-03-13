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

import static ai.startree.thirdeye.scheduler.SubscriptionCronScheduler.jobKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SubscriptionCronSchedulerTest {

  public static final String CRON_EXPRESSION = "* * * * * ?";
  public static final long ID = 1L;
  @Mock
  private Scheduler scheduler;
  @Mock
  private SubscriptionGroupManager subscriptionGroupManager;

  private SubscriptionCronScheduler subscriptionCronScheduler;

  // Utility method to create a SubscriptionGroupDTO
  private static SubscriptionGroupDTO createSubscriptionGroup(final Long id,
      final boolean isActive,
      final String cronExpression) {
    final SubscriptionGroupDTO sg = new SubscriptionGroupDTO()
        .setActive(isActive)
        .setCronExpression(cronExpression);
    sg.setId(id);
    return sg;
  }

  @BeforeClass
  void setUp() throws SchedulerException {
    MockitoAnnotations.openMocks(this);
    subscriptionCronScheduler = new SubscriptionCronScheduler(subscriptionGroupManager,
        scheduler,
        new ThirdEyeSchedulerConfiguration());
    when(scheduler.getJobKeys(GroupMatcher.anyGroup())).thenReturn(new HashSet<>());
  }

  @Test
  void processSubscriptionGroupActiveAndNotScheduled() throws SchedulerException {
    reset(scheduler);
    final SubscriptionGroupDTO activeGroup = createSubscriptionGroup(ID, true, CRON_EXPRESSION);
    final Set<JobKey> scheduledJobs = new HashSet<>();

    subscriptionCronScheduler.processSubscriptionGroup(activeGroup, scheduledJobs);

    verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  void processSubscriptionGroupActiveAndScheduled() throws SchedulerException {
    reset(scheduler);
    final SubscriptionGroupDTO activeGroup = createSubscriptionGroup(ID, true, CRON_EXPRESSION);
    final Set<JobKey> scheduledJobs = Set.of(jobKey(activeGroup.getId()));
    subscriptionCronScheduler.processSubscriptionGroup(activeGroup, scheduledJobs);

    verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
  }

  @Test
  void processSubscriptionGroupInactiveAndScheduled() throws SchedulerException {
    reset(scheduler);
    final SubscriptionGroupDTO inactiveGroup = createSubscriptionGroup(ID, false, CRON_EXPRESSION);
    final Set<JobKey> scheduledJobs = Collections.singleton(jobKey(inactiveGroup.getId()));

    subscriptionCronScheduler.processSubscriptionGroup(inactiveGroup, scheduledJobs);

    final Long id = inactiveGroup.getId();
    verify(scheduler).deleteJob(jobKey(id));
  }

  @Test
  void deleteIfNotInDatabase() throws SchedulerException {
    reset(scheduler);
    when(subscriptionGroupManager.findById(ID)).thenReturn(null);

    subscriptionCronScheduler.deleteIfNotInDatabase(jobKey(ID));

    verify(scheduler).deleteJob(jobKey(ID));
  }
}