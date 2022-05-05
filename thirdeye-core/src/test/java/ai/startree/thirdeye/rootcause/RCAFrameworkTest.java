/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.rootcause.impl.LinearAggregationPipeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.testng.Assert;
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

  @Test
  public void testLinearAggregationPipeline() {
    LinearAggregationPipeline agg = new LinearAggregationPipeline("",
        Collections.emptySet(), -1);

    Entity e1 = new Entity("e:one", 1.0, new ArrayList<Entity>());
    Entity e2 = new Entity("e:two", 2.1, new ArrayList<Entity>());
    Entity e3 = new Entity("e:three", 3.2, new ArrayList<Entity>());
    Entity e4 = new Entity("e:four", 4.0, new ArrayList<Entity>());

    Set<Entity> scores1 = new HashSet<>();
    scores1.add(e1);
    scores1.add(e2);
    scores1.add(e3);

    Set<Entity> scores2 = new HashSet<>();
    scores2.add(e2);
    scores2.add(e3);
    scores2.add(e4);

    Map<String, Set<Entity>> inputs = new HashMap<>();
    inputs.put("p1", scores1);
    inputs.put("p2", scores2);

    PipelineContext context = new PipelineContext(inputs);

    List<Entity> entities = new ArrayList<>(agg.run(context).getEntities());
    Collections.sort(entities, Entity.HIGHEST_SCORE_FIRST);

    Assert.assertEquals(entities.size(), 4);
    Assert.assertEquals(entities.get(0).getUrn(), e3.getUrn());
    Assert.assertEquals(entities.get(0).getScore(), 6.4);
    Assert.assertEquals(entities.get(1).getUrn(), e2.getUrn());
    Assert.assertEquals(entities.get(1).getScore(), 4.2);
    Assert.assertEquals(entities.get(2).getUrn(), e4.getUrn());
    Assert.assertEquals(entities.get(2).getScore(), 4.0);
    Assert.assertEquals(entities.get(3).getUrn(), e1.getUrn());
    Assert.assertEquals(entities.get(3).getScore(), 1.0);
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
