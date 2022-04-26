/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * An entity represents a node in the knowledge graph traversed to identify potential root causes.
 * The URN represents a unique identifier (with a hierarchical namespace) and the score identifies
 * the relative importance to other entities given a specific context.
 * In the user-specified search context, the score represents the subjective importance of the
 * entity as determined by the user. In the execution context and the pipeline results the score
 * represents the relative importance of entities associated with the search context as determined
 * by individual pipelines and the aggregator.
 *
 * <br/><b>NOTE:</b> a subclass of {@code Entity} may be returned by pipelines in order to pass
 * along transient meta-data for individual entities.
 *
 * <br/><b>NOTE:</b> due to the potentially vast number of nodes in the knowledge graph there is no
 * centralized repository of valid URNs.
 */
public class Entity {

  private final String urn;
  private final double score;
  private final List<Entity> related;

  public static final Comparator<Entity> HIGHEST_SCORE_FIRST = new Comparator<Entity>() {
    @Override
    public int compare(Entity o1, Entity o2) {
      return -1 * Double.compare(o1.score, o2.score);
    }
  };

  public Entity(String urn, double score, List<? extends Entity> related) {
    this.urn = urn;
    this.score = score;
    this.related = Collections.unmodifiableList(new ArrayList<>(related));
  }

  public String getUrn() {
    return urn;
  }

  public double getScore() {
    return score;
  }

  public List<Entity> getRelated() {
    return related;
  }

  public Entity withScore(double score) {
    return new Entity(this.urn, score, this.related);
  }

  public Entity withRelated(List<? extends Entity> related) {
    return new Entity(this.urn, this.score, related);
  }

  @Override
  public String toString() {
    return String
        .format("%s(urn=%s, score=%.3f)", getClass().getSimpleName(), this.urn, this.score);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Entity)) {
      return false;
    }
    Entity entity = (Entity) o;
    return Double.compare(entity.score, score) == 0
        && Objects.equals(urn, entity.urn)
        && Objects.equals(related, entity.related);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urn, score, related);
  }
}
