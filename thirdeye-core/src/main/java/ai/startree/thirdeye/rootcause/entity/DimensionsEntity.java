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
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DimensionsEntity represents a dimension cut of the data without being bound
 * to a specific entity, such as a metric. The DimensionsEntity holds (uri encoded)
 * tuples of keys and values, e.g. 'thirdeye:dimensions:key1=value1:key2=value2'.
 *
 * <br/><b>NOTE:</b> it is the successor of DimensionEntity
 *
 * @see DimensionEntity
 */
public class DimensionsEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:dimensions:");

  private final Multimap<String, String> dimensions;

  private DimensionsEntity(String urn, double score, List<? extends Entity> related,
      Multimap<String, String> dimensions) {
    super(urn, score, related);
    this.dimensions = dimensions;
  }

  public Multimap<String, String> getDimensions() {
    return dimensions;
  }

  @Override
  public DimensionsEntity withScore(double score) {
    return fromDimensions(score, this.getRelated(), this.dimensions);
  }

  @Override
  public DimensionsEntity withRelated(List<? extends Entity> related) {
    return fromDimensions(this.getScore(), related, this.dimensions);
  }

  public DimensionsEntity withDimensions(Multimap<String, String> dimensions) {
    return fromDimensions(this.getScore(), this.getRelated(), dimensions);
  }

  public static DimensionsEntity fromDimensions(double score, Multimap<String, String> dimensions) {
    return fromDimensions(score, Collections.emptyList(), dimensions);
  }

  public static DimensionsEntity fromDimensions(double score, Collection<? extends Entity> related,
      Multimap<String, String> dimensions) {
    return new DimensionsEntity(TYPE.formatURN(EntityUtils.encodeDimensions(dimensions)), score,
        new ArrayList<>(related), dimensions);
  }

  public static DimensionsEntity fromURN(String urn, double score) {
    return fromDimensions(score, EntityUtils.parseUrnString(urn, TYPE, 2).toFiltersMap());
  }
}
