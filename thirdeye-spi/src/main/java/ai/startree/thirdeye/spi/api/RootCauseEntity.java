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
