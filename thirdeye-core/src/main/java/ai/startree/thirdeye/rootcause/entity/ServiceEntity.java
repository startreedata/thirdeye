/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.entity;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.util.ParsedUrn;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ServiceEntity represents a service associated with certain metrics or dimensions. It typically
 * serves as a connecting piece between observed discrepancies between current and baseline metrics
 * and root cause events such as code deployments. The URN namespace is defined as
 * 'thirdeye:service:{name}'.
 */
public class ServiceEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:service:");

  private final String name;

  protected ServiceEntity(String urn, double score, List<? extends Entity> related, String name) {
    super(urn, score, related);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public ServiceEntity withScore(double score) {
    return new ServiceEntity(this.getUrn(), score, this.getRelated(), this.name);
  }

  @Override
  public ServiceEntity withRelated(List<? extends Entity> related) {
    return new ServiceEntity(this.getUrn(), this.getScore(), related, this.name);
  }

  public static ServiceEntity fromName(double score, String name) {
    String urn = TYPE.formatURN(name);
    return new ServiceEntity(urn, score, new ArrayList<Entity>(), name);
  }

  public static ServiceEntity fromURN(String urn, double score) {
    ParsedUrn parsedUrn = EntityUtils.parseUrnString(urn, TYPE);
    parsedUrn.assertPrefixOnly();

    String service = parsedUrn.getPrefixes().get(2);
    return new ServiceEntity(urn, score, Collections.emptyList(), service);
  }
}
