/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for configuring and executing a root cause search with multiple pipelines.
 * The framework is instantiated with multiple (named) pipelines and a result aggregator. The run()
 * method then executes the configured pipelines for arbitrary inputs without
 * maintaining any additional state within the RCAFramework.
 *
 * RCAFramework supports parallel DAG execution and requires pipelines to form a valid path
 * from {@code INPUT} to {@code OUTPUT}. The execution order of pipelines is guaranteed to be
 * compatible with serial execution in one single thread.
 */

/*
 *                   /-> pipeline.run() --> pipeline.run() \
 *                  /                                       \
 * INPUT --> run() ---> pipeline.run() ---> pipeline.run() --> OUTPUT
 *                  \                    /
 *                   \-> pipeline.run() /
 */
@Deprecated
public class RCAFramework {

  private static final Logger LOG = LoggerFactory.getLogger(RCAFramework.class);

  public static final String INPUT = "INPUT";
  public static final String OUTPUT = "OUTPUT";
  public static final long TIMEOUT = 600000;

  private final Map<String, Pipeline> pipelines;
  private final ExecutorService executor;

  public RCAFramework(Collection<Pipeline> pipelines, ExecutorService executor) {
    this.executor = executor;

    if (!isValidDAG(pipelines)) {
      throw new IllegalArgumentException(
          String.format("Invalid DAG. '%s' not reachable output name '%s'", OUTPUT, INPUT));
    }

    this.pipelines = new HashMap<>();
    for (Pipeline p : pipelines) {
      if (INPUT.equals(p.getOutputName())) {
        throw new IllegalArgumentException(
            String.format("Must not contain a pipeline with output name '%s'", INPUT));
      }
      if (this.pipelines.containsKey(p.getOutputName())) {
        throw new IllegalArgumentException(
            String.format("Already contains pipeline with output name '%s'", p.getOutputName()));
      }
      this.pipelines.put(p.getOutputName(), p);
    }

    if (!this.pipelines.containsKey(OUTPUT)) {
      throw new IllegalArgumentException(
          String.format("Must contain a pipeline with output name '%s'", OUTPUT));
    }
  }

  /**
   * Performs rootcause search for a user-specified set of input entities.
   * Fans out entities to individual pipelines, collects results, and aggregates them.
   *
   * @param input user-specified search entities
   * @return aggregated results
   */
  @Deprecated
  public RCAFrameworkExecutionResult run(Set<Entity> input) throws Exception {
    throw new UnsupportedOperationException("deprecated");
  }

  static boolean isValidDAG(Collection<Pipeline> pipelines) {
    Set<String> visited = new HashSet<>();
    visited.add(INPUT);

    int prevSize = 0;
    while (prevSize < visited.size()) {
      prevSize = visited.size();
      for (Pipeline p : pipelines) {
        if (visited.containsAll(p.getInputNames())) {
          visited.add(p.getOutputName());
        }
      }
    }

    return visited.contains(OUTPUT);
  }
}
