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
package ai.startree.thirdeye.rootcause.entity;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.util.ParsedUrn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DatasetEntity represents a group of metrics tightly associated with each other. It typically
 * serves
 * as a convenience to describe entity mappings common to whole groups of entities.
 * 'thirdeye:dataset:{name}'.
 */
public class DatasetEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:dataset:");

  private final String name;

  protected DatasetEntity(String urn, double score, List<? extends Entity> related, String name) {
    super(urn, score, related);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public DatasetEntity withScore(double score) {
    return new DatasetEntity(this.getUrn(), score, this.getRelated(), this.name);
  }

  @Override
  public DatasetEntity withRelated(List<? extends Entity> related) {
    return new DatasetEntity(this.getUrn(), this.getScore(), related, this.name);
  }

  public static DatasetEntity fromName(double score, String name) {
    String urn = TYPE.formatURN(name);
    return new DatasetEntity(urn, score, new ArrayList<Entity>(), name);
  }

  public static DatasetEntity fromName(double score, Collection<? extends Entity> related,
      String name) {
    String urn = TYPE.formatURN(name);
    return new DatasetEntity(urn, score, new ArrayList<>(related), name);
  }

  public static DatasetEntity fromURN(String urn, double score) {
    ParsedUrn parsedUrn = EntityUtils.parseUrnString(urn, TYPE);
    parsedUrn.assertPrefixOnly();

    String dataset = parsedUrn.getPrefixes().get(2);
    return new DatasetEntity(urn, score, Collections.emptyList(), dataset);
  }
}
