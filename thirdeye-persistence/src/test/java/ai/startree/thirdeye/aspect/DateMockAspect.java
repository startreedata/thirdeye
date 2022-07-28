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
package ai.startree.thirdeye.aspect;

import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class DateMockAspect {

  @Pointcut("call(java.util.Date.new())")
  void currentDate() {
  }

  @Around("currentDate()")
  public Object aroundCurrentDate(ProceedingJoinPoint pjp) throws Throwable {
    if (TimeProvider.instance().isTimedMocked()) {
      return new Date(TimeProvider.instance().currentTimeMillis());
    }
    return pjp.proceed();
  }
}
