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
package ai.startree.thirdeye.rootcause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Executors;
import org.testng.annotations.Test;

public class RCAFrameworkTest {

  private static final String INPUT = RCAFramework.INPUT;
  private static final String OUTPUT = RCAFramework.OUTPUT;

  static DummyPipeline makePipeline(String name, String... inputs) {
    final DummyPipeline dummyPipeline = new DummyPipeline();
    dummyPipeline.init(new PipelineInitContext()
        .setOutputName(name)
        .setInputNames(new HashSet<>(Arrays.asList(inputs)))
    );
    return dummyPipeline;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDAGInputPipeline() {
    Collection<Pipeline> pipelines = new ArrayList<>();
    pipelines.add(makePipeline(INPUT));
    pipelines.add(makePipeline(OUTPUT, INPUT));
    new RCAFramework(pipelines, Executors.newSingleThreadExecutor());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDAGNoOutput() {
    Collection<Pipeline> pipelines = new ArrayList<>();
    pipelines.add(makePipeline("a", INPUT));
    new RCAFramework(pipelines, Executors.newSingleThreadExecutor());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDAGNoPath() {
    Collection<Pipeline> pipelines = new ArrayList<>();
    pipelines.add(makePipeline("a", INPUT));
    pipelines.add(makePipeline(OUTPUT, "a", "b"));
    new RCAFramework(pipelines, Executors.newSingleThreadExecutor());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDAGDuplicateName() {
    Collection<Pipeline> pipelines = new ArrayList<>();
    pipelines.add(makePipeline("a"));
    pipelines.add(makePipeline("a", INPUT));
    pipelines.add(makePipeline(OUTPUT, INPUT, "a"));
    new RCAFramework(pipelines, Executors.newSingleThreadExecutor());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDAGCyclicDependency() {
    Collection<Pipeline> pipelines = new ArrayList<>();
    pipelines.add(makePipeline("a", INPUT, "b"));
    pipelines.add(makePipeline("b", INPUT, "a"));
    pipelines.add(makePipeline(OUTPUT, "a", "b"));
    new RCAFramework(pipelines, Executors.newSingleThreadExecutor());
  }

  @Test
  public void testValidDAG() {
    Collection<Pipeline> pipelines = new ArrayList<>();
    pipelines.add(makePipeline("none"));
    pipelines.add(makePipeline("a", INPUT));
    pipelines.add(makePipeline("b", INPUT, "a"));
    pipelines.add(makePipeline(OUTPUT, INPUT, "a", "b", "none"));
    new RCAFramework(pipelines, Executors.newSingleThreadExecutor());
  }

  static class DummyPipeline extends Pipeline {

    @Override
    public PipelineResult run(PipelineContext context) {
      return new PipelineResult(context, Collections.emptySet());
    }
  }
}
