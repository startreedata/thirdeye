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
package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.entity.EntityType;
import ai.startree.thirdeye.rootcause.entity.EventEntity;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.ArrayList;
import java.util.List;

public class ThirdEyeEventEntity extends EventEntity {

  private final EventDTO dto;

  private ThirdEyeEventEntity(String urn, double score, List<? extends Entity> related, long id,
      EventDTO dto, String eventType) {
    super(urn, score, related, eventType, id);
    this.dto = dto;
  }

  public EventDTO getDto() {
    return dto;
  }

  @Override
  public ThirdEyeEventEntity withScore(double score) {
    return new ThirdEyeEventEntity(this.getUrn(), score, this.getRelated(), this.getId(), this.dto,
        getEventType());
  }

  @Override
  public ThirdEyeEventEntity withRelated(List<? extends Entity> related) {
    return new ThirdEyeEventEntity(this.getUrn(), this.getScore(), related, this.getId(), this.dto,
        getEventType());
  }

  public static ThirdEyeEventEntity fromDTO(double score, EventDTO dto, String eventType) {
    EntityType type = new EntityType(EventEntity.TYPE.getPrefix() + eventType + ":");
    String urn = type.formatURN(dto.getId());
    return new ThirdEyeEventEntity(urn, score, new ArrayList<Entity>(), dto.getId(), dto,
        eventType);
  }

  public static ThirdEyeEventEntity fromDTO(double score, List<? extends Entity> related,
      EventDTO dto, String eventType) {
    EntityType type = new EntityType(EventEntity.TYPE.getPrefix() + eventType + ":");
    String urn = type.formatURN(dto.getId());
    return new ThirdEyeEventEntity(urn, score, related, dto.getId(), dto, eventType);
  }
}
