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
package org.sample;

import static org.sample.Srn2Benchmark.correctImplem;
import static org.sample.Srn2Benchmark.pinotImpl;

public class ProfilingExample {

  public static void main(String[] args) {
    // warmup - it's hard to do correctly in a single run
    warmup();
    // run we will look at
    runWeLookAt();
  }

  private static void runWeLookAt() {
    for (int i = 0; i<300000 ; i++) {
      //var res = correctImplem(TargetType.EXAMPLE1, "1", "database1");
      var res = pinotImpl(TargetType.EXAMPLE1, "1", "database1");
      System.out.println(res);
    }
  }

  private static void warmup() {
    for (int i = 0; i<10000 ; i++) {
      var res1 = correctImplem(TargetType.EXAMPLE1, "1", "database1");
      var res2 = pinotImpl(TargetType.EXAMPLE1, "1", "database1");
      System.out.println(res2);
      System.out.println(res1);
    }
  }
}
