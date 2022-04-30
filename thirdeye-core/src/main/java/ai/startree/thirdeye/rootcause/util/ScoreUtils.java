/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility for scoring entities
 */
public class ScoreUtils {

  private ScoreUtils() {
    // left blank
  }

  /**
   * Determines a score between {@code [0.0, 1.0]} based on an entity's time range, described
   * by start and end timestamps.
   */
  public interface TimeRangeStrategy {

    /**
     * Returns an entity's score based on start and end timestamps.
     *
     * @param start start time in millis
     * @param end end time in millis
     * @return score between {@code [0.0, 1.0]}
     */
    double score(long start, long end);
  }

  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. The closer
   * an entity's start time is to the start time of the interval, the higher the score. If the
   * entity's start time lies outside the window, the score is zero.
   */
  public static final class LinearStartTimeStrategy implements TimeRangeStrategy {

    private final long start;
    private final long duration;

    public LinearStartTimeStrategy(long start, long end) {
      if (start > end) {
        throw new IllegalArgumentException("Requires start <= end");
      }
      this.start = start;
      this.duration = end - start;
    }

    @Override
    public double score(long start, long end) {
      if (start < this.start) {
        return 0;
      }
      long offset = start - this.start;
      return Math.min(Math.max(1.0d - offset / (double) this.duration, 0), 1.0);
    }
  }

  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. The closer
   * an entity's start time is to the start time of the interval, the higher the score. This
   * scorer allows for a lookback period before the start of the actual interval and provides
   * an increasing score between {@code lookback} an {@code start}, and a decreasing score
   * between {@code start} and {@code end}.
   */
  public static final class TriangularStartTimeStrategy implements TimeRangeStrategy {

    private final long lookback;
    private final long start;
    private final long end;

    public TriangularStartTimeStrategy(long lookback, long start, long end) {
      if (lookback > start) {
        throw new IllegalArgumentException("Requires lookback <= start");
      }
      if (start > end) {
        throw new IllegalArgumentException("Requires start <= end");
      }

      this.lookback = lookback;
      this.start = start;
      this.end = end;
    }

    @Override
    public double score(long start, long end) {
      if (start < this.lookback) {
        return 0;
      }
      if (start >= this.end) {
        return 0;
      }

      if (start < this.start) {
        // in lookback
        long duration = this.start - this.lookback;
        long offset = start - this.lookback;
        return offset / (double) duration;
      }

      // in time range
      long duration = this.end - this.start;
      long offset = start - this.start;
      return 1.0 - offset / (double) duration;
    }
  }

  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. Similar to
   * {@code TriangularStartTimeStrategy} in function, squaring the result score.
   *
   * @see TriangularStartTimeStrategy
   */
  public static final class QuadraticTriangularStartTimeStrategy implements TimeRangeStrategy {

    private final TriangularStartTimeStrategy delegate;

    public QuadraticTriangularStartTimeStrategy(long lookback, long start, long end) {
      this.delegate = new TriangularStartTimeStrategy(lookback, start, end);
    }

    @Override
    public double score(long start, long end) {
      return Math.pow(this.delegate.score(start, end), 2);
    }
  }

  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. The score
   * is proportional to the absolute distance from the anomaly region start and truncated to
   * {@code 0.0} after the midpoint of this region.
   */
  public static final class HyperbolaStrategy implements TimeRangeStrategy {

    private static final double COEFFICIENT = 1.0d / TimeUnit.HOURS.toMillis(1);

    private final long start;
    private final long end;

    public HyperbolaStrategy(long start, long end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public double score(long start, long end) {
      if (start >= (this.start + this.end) / 2) {
        return 0;
      }
      return 1.0d / (COEFFICIENT * Math.abs(start - this.start) + 1.0);
    }
  }
}
