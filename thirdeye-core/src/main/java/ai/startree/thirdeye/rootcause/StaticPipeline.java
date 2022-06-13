/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.rootcause.util.EntityUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StaticPipeline emits a fixed set of entities as a result, regardless of the input. It is
 * used to encapsulate constants (such as user input) during framework execution.
 */
public class StaticPipeline extends Pipeline {

  private static final String PROP_ENTITIES = "entities";
  private static final String PROP_SCORES = "scores";

  private Set<Entity> entities;

  public StaticPipeline(Set<Entity> entities) {
    this.entities = entities;
  }

  @SuppressWarnings("unused")
  public StaticPipeline() {}

  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();

    if (entities == null) {
      if (!properties.containsKey(PROP_ENTITIES)) {
        throw new IllegalArgumentException(
            String.format("Property '%s' required, but not found", PROP_ENTITIES));
      }

      this.entities = new HashSet<>();

      if (properties.get(PROP_ENTITIES) instanceof Map) {
        // with scores
        Map<String, Double> entities = (Map<String, Double>) properties.get(PROP_ENTITIES);
        for (Map.Entry<String, Double> entry : entities.entrySet()) {
          this.entities.add(EntityUtils.parseURN(entry.getKey(), entry.getValue()));
        }
      } else if (properties.get(PROP_ENTITIES) instanceof List) {
        // without scores
        List<String> urns = (List<String>) properties.get(PROP_ENTITIES);
        for (String urn : urns) {
          this.entities.add(EntityUtils.parseURN(urn, 1.0));
        }
      }
    }
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, this.entities);
  }
}
