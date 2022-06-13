/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.rootcause.entity.DimensionEntity;
import ai.startree.thirdeye.rootcause.impl.EntityMappingPipeline;
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EntityMappingPipelineTest {

  private static final String DIMENSION_TO_DIMENSION = "DIMENSION_TO_DIMENSION";

  private static final PipelineContext CONTEXT_DEFAULT = makeContext(
      makeEntities(1.0, "country:us", "country:at", "other:value"));

  // TODO direction tests

  @Test
  public void testNoMappingDefault() {
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(
        Collections.emptyList());
    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), false, false,
        false, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertTrue(entities.isEmpty());
  }

  @Test
  public void testNoMappingRewriter() {
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(
        Collections.emptyList());
    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), true, false,
        false, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 3);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:country:at:provided");
    Assert.assertEquals(entities.get(0).getScore(), 1.0);
    Assert.assertEquals(entities.get(1).getUrn(), "thirdeye:dimension:country:us:provided");
    Assert.assertEquals(entities.get(1).getScore(), 1.0);
    Assert.assertEquals(entities.get(2).getUrn(), "thirdeye:dimension:other:value:provided");
    Assert.assertEquals(entities.get(2).getScore(), 1.0);
  }

  @Test
  public void testMappingDefault() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:", "thirdeye:dimension:countryCode:", 0.5,
        DIMENSION_TO_DIMENSION));
    mappings.add(makeMapping("thirdeye:dimension:country:us:provided",
        "thirdeye:dimension:countryCode:us:provided", 0.8, DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), false, false,
        false, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 1);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:countryCode:us:provided");
    Assert.assertEquals(entities.get(0).getScore(), 0.8);
  }

  @Test
  public void testMappingDefaultPrefix() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:", "thirdeye:dimension:countryCode:", 0.5,
        DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), false, true,
        false, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 2);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:countryCode:at:provided");
    Assert.assertEquals(entities.get(0).getScore(), 0.5);
    Assert.assertEquals(entities.get(1).getUrn(), "thirdeye:dimension:countryCode:us:provided");
    Assert.assertEquals(entities.get(1).getScore(), 0.5);
  }

  @Test
  public void testMappingRewriter() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:us:provided",
        "thirdeye:dimension:countryCode:us:provided", 0.8, DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), true, false,
        false, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 3);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:country:at:provided");
    Assert.assertEquals(entities.get(0).getScore(), 1.0);
    Assert.assertEquals(entities.get(1).getUrn(), "thirdeye:dimension:other:value:provided");
    Assert.assertEquals(entities.get(1).getScore(), 1.0);
    Assert.assertEquals(entities.get(2).getUrn(), "thirdeye:dimension:countryCode:us:provided");
    Assert.assertEquals(entities.get(2).getScore(), 0.8);
  }

  @Test
  public void testMappingRewriterPrefix() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:", "thirdeye:dimension:countryCode:", 0.5,
        DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), true, true,
        false, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 3);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:other:value:provided");
    Assert.assertEquals(entities.get(0).getScore(), 1.0);
    Assert.assertEquals(entities.get(1).getUrn(), "thirdeye:dimension:countryCode:at:provided");
    Assert.assertEquals(entities.get(1).getScore(), 0.5);
    Assert.assertEquals(entities.get(2).getUrn(), "thirdeye:dimension:countryCode:us:provided");
    Assert.assertEquals(entities.get(2).getScore(), 0.5);
  }

  @Test
  public void testMappingIterations() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:us:provided",
        "thirdeye:dimension:country:cn:provided", 0.5, DIMENSION_TO_DIMENSION));
    mappings.add(makeMapping("thirdeye:dimension:country:cn:provided",
        "thirdeye:dimension:country:at:provided", 0.5, DIMENSION_TO_DIMENSION));
    mappings.add(makeMapping("thirdeye:dimension:country:at:provided",
        "thirdeye:dimension:country:us:provided", 0.5, DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), Collections.emptySet(), false, false,
        true, EntityMappingPipeline.PROP_DIRECTION_REGULAR, 3);

    List<Entity> entities = runPipeline(p, makeContext(makeEntities(1.0, "country:us")));

    Assert.assertEquals(entities.size(), 3);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:country:cn:provided");
    Assert.assertEquals(entities.get(0).getScore(), 0.5);
    Assert.assertEquals(entities.get(1).getUrn(), "thirdeye:dimension:country:at:provided");
    Assert.assertEquals(entities.get(1).getScore(), 0.25);
    Assert.assertEquals(entities.get(2).getUrn(), "thirdeye:dimension:country:us:provided");
    Assert.assertEquals(entities.get(2).getScore(), 0.125);
  }

  @Test
  public void testMappingInputFilter() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:at:provided",
        "thirdeye:dimension:countryCode:at:provided", 0.5, DIMENSION_TO_DIMENSION));
    mappings.add(makeMapping("thirdeye:dimension:country:us:provided",
        "thirdeye:dimension:countryCode:us:provided", 0.8, DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    Set<String> inputFilters = Collections.singleton("thirdeye:dimension:country:us:");

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, inputFilters, Collections.emptySet(), false, false, false,
        EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 1);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:countryCode:us:provided");
    Assert.assertEquals(entities.get(0).getScore(), 0.8);
  }

  @Test
  public void testMappingOutputFilter() {
    Collection<EntityToEntityMappingDTO> mappings = new ArrayList<>();
    mappings.add(makeMapping("thirdeye:dimension:country:at:provided",
        "thirdeye:dimension:countryCode:at:provided", 0.5, DIMENSION_TO_DIMENSION));
    mappings.add(makeMapping("thirdeye:dimension:country:us:provided",
        "thirdeye:dimension:countryCode:us:provided", 0.8, DIMENSION_TO_DIMENSION));
    EntityToEntityMappingManager entityDAO = new MockEntityToEntityMappingManager(mappings);

    Set<String> outputFilters = Collections.singleton("thirdeye:dimension:countryCode:at:");

    EntityMappingPipeline p = new EntityMappingPipeline("output", Collections.emptySet(),
        entityDAO, Collections.emptySet(), outputFilters, false, false, false,
        EntityMappingPipeline.PROP_DIRECTION_REGULAR, 1);

    List<Entity> entities = runPipeline(p, CONTEXT_DEFAULT);

    Assert.assertEquals(entities.size(), 1);
    Assert.assertEquals(entities.get(0).getUrn(), "thirdeye:dimension:countryCode:at:provided");
    Assert.assertEquals(entities.get(0).getScore(), 0.5);
  }

  private static List<Entity> runPipeline(Pipeline pipeline, PipelineContext context) {
    List<Entity> entities = new ArrayList<>(pipeline.run(context).getEntities());
    Collections.sort(entities, new Comparator<Entity>() {
      @Override
      public int compare(Entity o1, Entity o2) {
        return o1.getUrn().compareTo(o2.getUrn());
      }
    });
    Collections.sort(entities, Entity.HIGHEST_SCORE_FIRST);
    return entities;
  }

  private static Entity[] makeEntities(double score, String... dimensions) {
    Entity[] entities = new Entity[dimensions.length];
    int i = 0;
    for (String dim : dimensions) {
      String[] parts = dim.split(":");
      entities[i++] = DimensionEntity
          .fromDimension(score, parts[0], parts[1], DimensionEntity.TYPE_PROVIDED);
    }
    return entities;
  }

  private static PipelineContext makeContext(Entity... entities) {
    Map<String, Set<Entity>> inputs = new HashMap<>();
    inputs.put("default", new HashSet<>(Arrays.asList(entities)));
    return new PipelineContext(inputs);
  }

  private static EntityToEntityMappingDTO makeMapping(String from, String to, double score,
      String type) {
    EntityToEntityMappingDTO dto = new EntityToEntityMappingDTO();
    dto.setFromURN(from);
    dto.setToURN(to);
    dto.setScore(score);
    dto.setMappingType(type);
    return dto;
  }
}
