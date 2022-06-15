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
package ai.startree.thirdeye.rootcause.callgraph;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.entity.EntityType;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.List;

public class CallGraphEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:callgraph:");

  private final DataFrame edge;

  protected CallGraphEntity(String urn, double score, List<? extends Entity> related,
      DataFrame edge) {
    super(urn, score, related);
    this.edge = edge;
  }

  public DataFrame getEdge() {
    return new DataFrame(this.edge);
  }

  @Override
  public CallGraphEntity withScore(double score) {
    return new CallGraphEntity(this.getUrn(), score, this.getRelated(), this.getEdge());
  }

  @Override
  public CallGraphEntity withRelated(List<? extends Entity> related) {
    return new CallGraphEntity(this.getUrn(), this.getScore(), related, this.getEdge());
  }

  public CallGraphEntity withEdge(DataFrame edge) {
    return new CallGraphEntity(this.getUrn(), this.getScore(), this.getRelated(),
        new DataFrame(edge));
  }

  public static CallGraphEntity fromEdge(double score, DataFrame edge) {
    return fromEdge(score, Collections.emptyList(), edge);
  }

  public static CallGraphEntity fromEdge(double score, List<? extends Entity> related,
      DataFrame edge) {
    if (edge.size() != 1) {
      throw new IllegalArgumentException("Must provide a data frame with exactly one row");
    }

    Multimap<String, String> dimensions = ArrayListMultimap.create();
    for (String seriesName : edge.getSeriesNames()) {
      dimensions.put(seriesName, edge.getString(seriesName, 0));
    }

    return new CallGraphEntity(TYPE.formatURN(EntityUtils.encodeDimensions(dimensions)), score,
        related, edge);
  }
}
