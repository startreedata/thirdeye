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
package ai.startree.thirdeye.scheduler.job;

import static ai.startree.thirdeye.spi.Constants.CTX_INJECTOR;
import static java.util.Objects.requireNonNull;

import com.google.inject.Injector;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThirdEyeAbstractJob implements Job {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeAbstractJob.class);

  protected final <T> T getInstance(final JobExecutionContext context, Class<T> clazz) {
    final Injector injector = (Injector) getObjectFromContext(context, CTX_INJECTOR);
    return injector.getInstance(clazz);
  }

  private Object getObjectFromContext(final JobExecutionContext context, final String key) {
    try {
      return requireNonNull(context.getScheduler().getContext().get(key));
    } catch (SchedulerException e) {
      final String message = String.format("Scheduler error. No key: %s", key);
      log.error(message, e);
      throw new RuntimeException(message, e);
    }
  }
}
