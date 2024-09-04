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
