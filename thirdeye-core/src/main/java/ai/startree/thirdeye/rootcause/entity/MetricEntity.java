/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.entity;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.util.ParsedUrn;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MetricEntity represents an individual metric. It holds meta-data referencing ThirdEye's internal
 * database. The URN namespace is defined as 'thirdeye:metric:{id}'.
 */
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

  public MetricEntity withFilters(Multimap<String, String> filters) {
    return new MetricEntity(TYPE.formatURN(this.id, EntityUtils.encodeDimensions(filters)),
        this.getScore(), this.getRelated(), this.id, filters);
  }

  public MetricEntity withoutFilters() {
    return new MetricEntity(TYPE.formatURN(this.id), this.getScore(), this.getRelated(), this.id,
        filters);
  }

  public static MetricEntity fromMetric(double score, Collection<? extends Entity> related, long id,
      Multimap<String, String> filters) {
    return new MetricEntity(TYPE.formatURN(id, EntityUtils.encodeDimensions(filters)), score,
        new ArrayList<>(related), id, TreeMultimap.create(filters));
  }

  public static MetricEntity fromMetric(double score, Collection<? extends Entity> related,
      long id) {
    return fromMetric(score, related, id, TreeMultimap.create());
  }

  public static MetricEntity fromMetric(double score, long id, Multimap<String, String> filters) {
    return fromMetric(score, new ArrayList<>(), id, filters);
  }

  public static MetricEntity fromMetric(double score, long id) {
    return fromMetric(score, new ArrayList<>(), id, TreeMultimap.create());
  }

  public static MetricEntity fromMetric(Map<String, Collection<String>> filterMaps, long id) {
    Multimap<String, String> filters = ArrayListMultimap.create();
    if (filterMaps != null) {
      for (Map.Entry<String, Collection<String>> entry : filterMaps.entrySet()) {
        filters.putAll(entry.getKey(), entry.getValue());
      }
    }

    return fromMetric(1.0, id, filters);
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
