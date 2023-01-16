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
package ai.startree.thirdeye.rootcause.events;

import java.util.concurrent.TimeUnit;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Different functions that score the similarity of two intervals.
 * The first interval is an anomaly, the other interval is a potentially related event.
 * lookaround is the period that was used to look before and after the start of the anomaly.
 * Scoring functions should return high scores when intervals are similar.
 * The definition of similarity for intervals is business specific, it is not necessarily a measure
 * of overlap.
 */
public enum IntervalSimilarityScoring {
  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. The closer
   * an entity's start time is to the start time of the interval, the higher the score. This
   * scorer allows for a lookback and lookahead period around the startTime of the anomalyInterval.
   */
  TRIANGULAR {
    public double score(final Interval anomalyInterval, final Interval otherInterval,
        final Period lookaround) {
      double diff = Math.abs(otherInterval.getStartMillis() - anomalyInterval.getStartMillis())
          / (double) lookaround.toStandardDuration().getMillis();
      // clip time out of lookaround range to 1
      diff = Math.min(diff, 1.);

      return 1 - diff;
    }
  },

  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. Similar to
   * {@code TRIANGULAR} in function, squaring the result score.
   *
   * @see #TRIANGULAR
   */
  QUADRATIC {
    public double score(final Interval anomalyInterval, final Interval eventInterval,
        final Period lookback) {
      return Math.pow(TRIANGULAR.score(anomalyInterval, eventInterval, lookback), 2);
    }
  },

  /**
   * Determines a score between {@code [0.0, 1.0]} based on the entity's start time. The score
   * is proportional to the absolute distance from the anomaly region start and truncated to
   * {@code 0.0} after the midpoint of this region.
   *
   * Note:  this function was kept because it was the default scoring function in the legacy TE. New
   * triangular and quadratic scoring look better and are easier to understand.
   */
  HYPERBOLA {
    public double score(final Interval anomalyInterval, final Interval eventInterval,
        final Period lookback) {
      if (eventInterval.getStartMillis()
          >= (anomalyInterval.getStartMillis() + anomalyInterval.getEndMillis()) / 2) {
        return 0;
      }
      return 1.0d / (
          COEFFICIENT * Math.abs(eventInterval.getStartMillis() - anomalyInterval.getStartMillis())
              + 1.0);
    }
  };

  private static final double COEFFICIENT = 1.0d / TimeUnit.HOURS.toMillis(1);

  public abstract double score(final Interval anomalyInterval, final Interval eventInterval,
      final Period lookaround);
}
