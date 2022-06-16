/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.entity;

import ai.startree.thirdeye.rootcause.Entity;
import java.util.List;

/**
 * EventEntity represents an individual event. It holds meta-data referencing ThirdEye's internal
 * database. The URN namespace is defined as 'thirdeye:event:{type}:{id}'.
 */
public class EventEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:event:");

  private final String eventType;
  private final long id;

  protected EventEntity(String urn, double score, List<? extends Entity> related, String eventType,
      long id) {
    super(urn, score, related);
    this.id = id;
    this.eventType = eventType;
  }

  public String getEventType() {
    return eventType;
  }

  public long getId() {
    return id;
  }

  @Override
  public EventEntity withScore(double score) {
    return new EventEntity(this.getUrn(), score, this.getRelated(), this.eventType, this.id);
  }

  @Override
  public EventEntity withRelated(List<? extends Entity> related) {
    return new EventEntity(this.getUrn(), this.getScore(), related, this.eventType, this.id);
  }
}
