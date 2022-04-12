/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.entity;

import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.rootcause.Entity;
import ai.startree.thirdeye.spi.rootcause.util.ParsedUrn;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnomalyEventEntity extends EventEntity {

  public static final String EVENT_TYPE_ANOMALY = "anomaly";

  public static final EntityType TYPE = new EntityType(
      EventEntity.TYPE.getPrefix() + EVENT_TYPE_ANOMALY + ":");

  protected AnomalyEventEntity(String urn, double score, List<? extends Entity> related, long id) {
    super(urn, score, related, EVENT_TYPE_ANOMALY, id);
  }

  @Override
  public AnomalyEventEntity withScore(double score) {
    return new AnomalyEventEntity(this.getUrn(), score, this.getRelated(), this.getId());
  }

  @Override
  public AnomalyEventEntity withRelated(List<? extends Entity> related) {
    return new AnomalyEventEntity(this.getUrn(), this.getScore(), related, this.getId());
  }

  public static AnomalyEventEntity fromDTO(double score, MergedAnomalyResultDTO dto) {
    String urn = TYPE.formatURN(dto.getId());
    return new AnomalyEventEntity(urn, score, new ArrayList<Entity>(), dto.getId());
  }

  public static AnomalyEventEntity fromURN(String urn, double score) {
    ParsedUrn parsedUrn = EntityUtils.parseUrnString(urn, TYPE);
    parsedUrn.assertPrefixOnly();

    long id = Long.parseLong(parsedUrn.getPrefixes().get(3));
    return new AnomalyEventEntity(urn, score, Collections.emptyList(), id);
  }
}
