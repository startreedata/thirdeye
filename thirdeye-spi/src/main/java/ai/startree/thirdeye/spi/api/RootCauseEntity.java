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
package ai.startree.thirdeye.spi.api;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;

public class RootCauseEntity {

  private String urn;
  private double score;
  private String label;
  private String type;
  private String link;
  private List<RootCauseEntity> relatedEntities = new ArrayList<>();
  private Multimap<String, String> attributes = ArrayListMultimap.create();

  public String getUrn() {
    return urn;
  }

  public RootCauseEntity setUrn(final String urn) {
    this.urn = urn;
    return this;
  }

  public double getScore() {
    return score;
  }

  public RootCauseEntity setScore(final double score) {
    this.score = score;
    return this;
  }

  public String getLabel() {
    return label;
  }

  public RootCauseEntity setLabel(final String label) {
    this.label = label;
    return this;
  }

  public String getType() {
    return type;
  }

  public RootCauseEntity setType(final String type) {
    this.type = type;
    return this;
  }

  public String getLink() {
    return link;
  }

  public RootCauseEntity setLink(final String link) {
    this.link = link;
    return this;
  }

  public List<RootCauseEntity> getRelatedEntities() {
    return relatedEntities;
  }

  public RootCauseEntity setRelatedEntities(
      final List<RootCauseEntity> relatedEntities) {
    this.relatedEntities = relatedEntities;
    return this;
  }

  public Multimap<String, String> getAttributes() {
    return attributes;
  }

  public RootCauseEntity setAttributes(
      final Multimap<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public void addRelatedEntity(RootCauseEntity e) {
    this.relatedEntities.add(e);
  }

  public void putAttribute(String key, String value) {
    this.attributes.put(key, value);
  }
}
