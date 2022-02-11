/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.rootcause.impl;

import ai.startree.thirdeye.spi.rootcause.Entity;
import ai.startree.thirdeye.spi.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.rootcause.util.ParsedUrn;
import java.util.Collections;
import java.util.List;

public class HyperlinkEntity extends Entity {

  public static final EntityType TYPE = new EntityType("http:");

  private HyperlinkEntity(String urn, double score, List<? extends Entity> related) {
    super(urn, score, related);
  }

  public String getUrl() {
    return this.getUrn();
  }

  @Override
  public HyperlinkEntity withScore(double score) {
    return new HyperlinkEntity(this.getUrn(), score, this.getRelated());
  }

  @Override
  public HyperlinkEntity withRelated(List<? extends Entity> related) {
    return new HyperlinkEntity(this.getUrn(), this.getScore(), related);
  }

  public static HyperlinkEntity fromURL(String url, double score) {
    ParsedUrn parsedUrn = EntityUtils.parseUrnString(url, TYPE);
    parsedUrn.assertPrefixOnly();
    return new HyperlinkEntity(url, score, Collections.emptyList());
  }
}
