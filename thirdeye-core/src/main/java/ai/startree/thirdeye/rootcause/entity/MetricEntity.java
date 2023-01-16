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
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.util.ParsedUrn;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.List;

/**
 * MetricEntity represents an individual metric. It holds meta-data referencing ThirdEye's internal
 * database. The URN namespace is defined as 'thirdeye:metric:{id}'.
 */
@Deprecated
public class MetricEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:metric:");

  private final long id;
  private final Multimap<String, String> filters;

  protected MetricEntity(String urn, double score, List<? extends Entity> related, long id,
      Multimap<String, String> filters) {
    super(urn, score, related);
    this.id = id;
    this.filters = filters;
  }

  public long getId() {
    return id;
  }

  public Multimap<String, String> getFilters() {
    return this.filters;
  }

  @Override
  public MetricEntity withScore(double score) {
    return new MetricEntity(this.getUrn(), score, this.getRelated(), this.id, this.filters);
  }

  @Override
  public MetricEntity withRelated(List<? extends Entity> related) {
    return new MetricEntity(this.getUrn(), this.getScore(), related, this.id, this.filters);
  }

  public static MetricEntity fromURN(String urn, double score) {
    ParsedUrn parsedUrn = EntityUtils.parseUrnString(urn, TYPE, 3);
    long id = Long.parseLong(parsedUrn.getPrefixes().get(2));
    return new MetricEntity(urn, score, Collections.emptyList(), id, parsedUrn.toFiltersMap());
  }

  public static MetricEntity fromURN(String urn) {
    return fromURN(urn, 1.0);
  }
}
