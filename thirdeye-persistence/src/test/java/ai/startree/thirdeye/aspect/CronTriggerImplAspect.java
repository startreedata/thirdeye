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
package ai.startree.thirdeye.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class CronTriggerImplAspect {
  
  private static final Logger LOG = LoggerFactory.getLogger(CronTriggerImplAspect.class);

  @Around("execution(* org.quartz.impl.triggers.CronTriggerImpl.updateAfterMisfire(..))")
  public void disableUpdateAfterMisfire(ProceedingJoinPoint pjp) throws Throwable {
    if (TimeProvider.instance().isTimedMocked()) {
      LOG.debug("Time is mocked, skipping misfire update in quartz trigger.");
    }
    pjp.proceed();
  }
}
