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
import org.aspectj.lang.annotation.Pointcut;

/**
 * Note about nanoTime: 
 * Mocking System.nanoTime is most likely not a good idea because it is used in complex codepaths 
 * related to futures and threading. Mocking it correctly and anticipating all side effects is tricky.
 * Please mock higher level methods.
 * For instance, try to mock the method that is using nanoTime instead of nanoTime itself.
 **/
@Aspect
public class SystemTimeMockAspect {

  @Pointcut("call(public long java.lang.System.currentTimeMillis())")
  void currentTimeMillis() {
  }

  @Around("currentTimeMillis()")
  public Object aroundSystemcurrentTimeMillis(ProceedingJoinPoint pjp) throws Throwable {
    if (TimeProvider.instance().isTimedMocked()) {
      return TimeProvider.instance().currentTimeMillis();
    }
    return pjp.proceed();
  }
}
