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
package ai.startree.thirdeye.detection.anomaly.views;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.assertEquals;

import ai.startree.thirdeye.util.TimeBucket;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCondensedAnomalyTimelinesView {

  /**
   * Create 5-min granularity test data
   */
  private AnomalyTimelinesView getTestData(int num) {
    DateTime date = new DateTime(2018, 1, 1, 0, 0, 0);
    AnomalyTimelinesView view = new AnomalyTimelinesView();
    for (int i = 0; i < num; i++) {
      view.addTimeBuckets(
          new TimeBucket(date.getMillis(), date.plusMinutes(5).getMillis(), date.getMillis(),
              date.plusMinutes(5).getMillis()));
      view.addCurrentValues((double) i);
      view.addBaselineValues(i + 0.333333333333);  // mimic the situation with many decimal digits
      date = date.plusMinutes(5);
    }
    view.addSummary("test", "test");
    return view;
  }

  @Test
  public void testFromAnomalyTimelinesView() {
    int testNum = 100;
    long minBucketMillis = CondensedAnomalyTimelinesView.DEFAULT_MIN_BUCKET_UNIT;
    CondensedAnomalyTimelinesView condensedView = CondensedAnomalyTimelinesView
        .fromAnomalyTimelinesView(getTestData(testNum));
    assertEquals(condensedView.bucketMillis.longValue(),
        Period.minutes(5).toStandardDuration().getMillis() / minBucketMillis);
    assertEquals(condensedView.getTimeStamps().size(), testNum);

    DateTime date = new DateTime(2018, 1, 1, 0, 0, 0);
    for (int i = 0; i < testNum; i++) {
      assertEquals(condensedView.getTimeStamps().get(i).longValue(),
          (date.getMillis() - condensedView.timestampOffset) / minBucketMillis);
      assertThat(condensedView.getCurrentValues().get(i)).isEqualTo(i + 0d);
      assertThat(condensedView.getBaselineValues().get(i)).isEqualTo(i + 0.333333333333);
      date = date.plusMinutes(5);
    }
  }

  @Test
  public void testFromJsonString() throws Exception {
    int testNum = 100;
    CondensedAnomalyTimelinesView condensedView = CondensedAnomalyTimelinesView
        .fromAnomalyTimelinesView(getTestData(testNum));

    AnomalyTimelinesView anomalyTimelinesView = CondensedAnomalyTimelinesView
        .fromJsonString(condensedView.toJsonString()).toAnomalyTimelinesView();

    DateTime date = new DateTime(2018, 1, 1, 0, 0, 0);
    for (int i = 0; i < testNum; i++) {
      TimeBucket timeBucket = anomalyTimelinesView.getTimeBuckets().get(i);
      assertEquals(timeBucket.getCurrentStart(), date.getMillis());
      assertEquals(timeBucket.getBaselineEnd(), date.plusMinutes(5).getMillis());
      assertThat(condensedView.getCurrentValues().get(i)).isEqualTo(i + 0d);
      assertThat(condensedView.getBaselineValues().get(i)).isEqualTo(i + 0.333333333333);
      date = date.plusMinutes(5);
    }
  }

  /**
   * Compression Test case 1: anomaly view could satisfy requirement after rounding up the decimals.
   */
  @Test
  public void testCompressWithRoundUp() throws Exception {
    int testNum = 500;
    CondensedAnomalyTimelinesView condensedView = CondensedAnomalyTimelinesView
        .fromAnomalyTimelinesView(getTestData(testNum));
    Assert.assertTrue(
        condensedView.toJsonString().length() > CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH);
    CondensedAnomalyTimelinesView compressedView = condensedView.compress();
    Assert.assertTrue(
        compressedView.toJsonString().length() < CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH);
    assertEquals(compressedView.timeStamps.size(), testNum);

    DateTime date = new DateTime(2018, 1, 1, 0, 0, 0);
    long minBucketMillis = CondensedAnomalyTimelinesView.DEFAULT_MIN_BUCKET_UNIT;
    for (int i = 0; i < compressedView.getTimeStamps().size(); i++) {
      assertEquals(compressedView.getTimeStamps().get(i).longValue(),
          (date.getMillis() - condensedView.timestampOffset) / minBucketMillis);
      assertThat(compressedView.getCurrentValues().get(i)).isEqualTo(i + 0.0d);
      assertThat(compressedView.getBaselineValues().get(i)).isEqualTo(i + 0.33);
      date = date.plusMinutes(5);
    }
  }

  /**
   * Compression Test case 2:  The anomaly view is still too large after rounding up, and needed to
   * be further compressed
   */
  @Test
  public void testCompress() throws Exception {
    int testNum = 600;
    long minBucketMillis = CondensedAnomalyTimelinesView.DEFAULT_MIN_BUCKET_UNIT;
    CondensedAnomalyTimelinesView condensedView = CondensedAnomalyTimelinesView
        .fromAnomalyTimelinesView(getTestData(testNum));
    Assert.assertTrue(
        condensedView.toJsonString().length() > CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH);
    CondensedAnomalyTimelinesView compressedView = condensedView.compress();
    Assert.assertTrue(
        compressedView.toJsonString().length() < CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH);
    assertEquals(300, compressedView.timeStamps.size());
    assertEquals(compressedView.bucketMillis.longValue(), 10);
    DateTime date = new DateTime(2018, 1, 1, 0, 0, 0);
    for (int i = 0; i < compressedView.getTimeStamps().size(); i++) {
      assertEquals(compressedView.getTimeStamps().get(i).longValue(),
          (date.getMillis() - condensedView.timestampOffset) / minBucketMillis);
      assertThat(compressedView.getCurrentValues().get(i)).isEqualTo(i * 2 + 0d);
      assertThat(compressedView.getBaselineValues().get(i)).isEqualTo(i * 2 + 0.33);
      date = date.plusMinutes(10);
    }

    AnomalyTimelinesView decompressedView = compressedView.toAnomalyTimelinesView();
    date = new DateTime(2018, 1, 1, 0, 0, 0);
    for (int i = 0; i < decompressedView.getBaselineValues().size(); i++) {
      TimeBucket timeBucket = decompressedView.getTimeBuckets().get(i);
      assertEquals(timeBucket.getCurrentStart(), date.getMillis());
      assertEquals(timeBucket.getCurrentEnd(), date.plusMinutes(10).getMillis());
      assertThat(decompressedView.getCurrentValues().get(i)).isEqualTo(i * 2 + 0d);
      assertThat(decompressedView.getBaselineValues().get(i)).isEqualTo(i * 2 + 0.33);
      date = date.plusMinutes(10);
    }
  }

  /**
   * Compression Test case 3:  compressed 2 times
   */
  @Test
  public void testCompressTwice() throws Exception {
    int testNum = 1200;
    long minBucketMillis = CondensedAnomalyTimelinesView.DEFAULT_MIN_BUCKET_UNIT;
    CondensedAnomalyTimelinesView condensedView = CondensedAnomalyTimelinesView
        .fromAnomalyTimelinesView(getTestData(testNum));
    Assert.assertTrue(
        condensedView.toJsonString().length() > CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH);
    CondensedAnomalyTimelinesView compressedView = condensedView.compress();
    Assert.assertTrue(
        compressedView.toJsonString().length() < CondensedAnomalyTimelinesView.DEFAULT_MAX_LENGTH);
    assertEquals(300, compressedView.timeStamps.size());
    assertEquals(compressedView.bucketMillis.longValue(), 20);
    DateTime date = new DateTime(2018, 1, 1, 0, 0, 0);
    for (int i = 0; i < compressedView.getTimeStamps().size(); i++) {
      assertEquals(compressedView.getTimeStamps().get(i).longValue(),
          (date.getMillis() - condensedView.timestampOffset) / minBucketMillis);
      assertThat(compressedView.getCurrentValues().get(i)).isEqualTo(i * 4 + 0d);
      assertThat(compressedView.getBaselineValues().get(i)).isEqualTo(i * 4 + 0.33);
      date = date.plusMinutes(20);
    }

    AnomalyTimelinesView decompressedView = compressedView.toAnomalyTimelinesView();
    date = new DateTime(2018, 1, 1, 0, 0, 0);
    for (int i = 0; i < decompressedView.getBaselineValues().size(); i++) {
      TimeBucket timeBucket = decompressedView.getTimeBuckets().get(i);
      assertEquals(timeBucket.getCurrentStart(), date.getMillis());
      assertEquals(timeBucket.getCurrentEnd(), date.plusMinutes(20).getMillis());
      assertThat(decompressedView.getCurrentValues().get(i)).isEqualTo(i * 4 + 0d);
      assertThat(decompressedView.getBaselineValues().get(i)).isEqualTo(i * 4 + 0.33);
      date = date.plusMinutes(20);
    }
  }
}
