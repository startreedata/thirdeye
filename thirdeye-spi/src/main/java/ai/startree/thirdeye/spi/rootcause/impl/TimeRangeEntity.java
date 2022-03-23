/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.rootcause.impl;

import ai.startree.thirdeye.spi.rootcause.Entity;
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import ai.startree.thirdeye.spi.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.rootcause.util.ParsedUrn;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TimeRangeEntity represents a time-range as investigated by the user for purposes of
 * root cause search. The URN namespace is defined as 'thirdeye:timerange:{type}:{start}:{end}'.
 */
public class TimeRangeEntity extends Entity {

  public static final EntityType TYPE = new EntityType("thirdeye:timerange:");

  public static final String TYPE_ANOMALY = "anomaly";
  public static final String TYPE_ANALYSIS = "analysis";
  public static final String TYPE_BASELINE = "baseline";

  private final String type;
  private final long start;
  private final long end;

  protected TimeRangeEntity(String urn, double score, List<? extends Entity> related, String type,
      long start, long end) {
    super(urn, score, related);
    this.type = type;
    this.start = start;
    this.end = end;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public String getType() {
    return type;
  }

  @Override
  public TimeRangeEntity withScore(double score) {
    return new TimeRangeEntity(this.getUrn(), score, this.getRelated(), this.type, this.start,
        this.end);
  }

  @Override
  public TimeRangeEntity withRelated(List<? extends Entity> related) {
    return new TimeRangeEntity(this.getUrn(), this.getScore(), related, this.type, this.start,
        this.end);
  }

  public static TimeRangeEntity fromURN(String urn, double score) {
    ParsedUrn parsedUrn = EntityUtils.parseUrnString(urn, TYPE);
    parsedUrn.assertPrefixOnly();

    String type = parsedUrn.getPrefixes().get(2);
    long start = Long.valueOf(parsedUrn.getPrefixes().get(3));
    long end = Long.valueOf(parsedUrn.getPrefixes().get(4));
    return fromRange(score, type, start, end);
  }

  public static TimeRangeEntity fromRange(double score, String type, long start, long end) {
    String urn = TYPE.formatURN(type, start, end);
    return new TimeRangeEntity(urn, score, Collections.emptyList(), type, start, end);
  }

  /**
   * Returns the TimeRangeEntity contained in the search context of an execution context.
   * Expects exactly one TimeRange entity and returns {@code null} if none or multiple
   * time range entities are found. If the search context contains an instance of
   * TimeRangeEntity it returns the instance. Otherwise, constructs a new instance of
   * TimeRangeEntity from an encoding URN.
   *
   * @param context execution context
   * @return TimeRangeEntity
   */
  public static TimeRangeEntity getContextTimeRange(PipelineContext context, String type) {
    Set<TimeRangeEntity> timeRanges = context.filter(TimeRangeEntity.class);
    Set<TimeRangeEntity> matching = new HashSet<>();
    for (TimeRangeEntity e : timeRanges) {
      if (e.getType().equals(type)) {
        matching.add(e);
      }
    }
    if (matching.size() != 1) {
      throw new IllegalArgumentException(
          String.format("Must contain exactly one of type '%s'", type));
    }
    return matching.iterator().next();
  }

  public static TimeRangeEntity getTimeRangeAnomaly(PipelineContext context) {
    return getContextTimeRange(context, TYPE_ANOMALY);
  }

  public static TimeRangeEntity getTimeRangeBaseline(PipelineContext context) {
    return getContextTimeRange(context, TYPE_BASELINE);
  }

  public static TimeRangeEntity getTimeRangeAnalysis(PipelineContext context) {
    return getContextTimeRange(context, TYPE_ANALYSIS);
  }
}
