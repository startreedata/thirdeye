/*
 * Copyright 2023 StarTree Inc
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
