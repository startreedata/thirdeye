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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

@Singleton
public class GuiceJobFactory implements JobFactory {

  private final Injector injector;

  // see https://github.com/google/guice/wiki/InjectingTheInjector - can be an anti-pattern but should be ok in this case
  @Inject
  public GuiceJobFactory(final Injector injector) {
    this.injector = injector;
  }

  @Override
  public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler)
      throws SchedulerException {
    return injector.getInstance(bundle.getJobDetail().getJobClass());
  }
}
