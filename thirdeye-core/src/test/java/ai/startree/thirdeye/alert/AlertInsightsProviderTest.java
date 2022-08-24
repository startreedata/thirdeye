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
package ai.startree.thirdeye.alert;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Test;

public class AlertInsightsProviderTest {

  private final static long JANUARY_1_2022_2AM = 1641002400000L;
  private final static long JANUARY_1_2022_0AM = 1640995200000L;
  private final static long DECEMBER_31_2022_11PM = 1640991600000L;
  private final static long JULY_2_2021_0AM = 1625184000000L;
  private final static long JULY_1_2021_4AM = 1625112000000L;
  private final static long JANUARY_1_2021_0AM = 1609459200000L;
  private final static long DECEMBER_31_2020_11PM = 1609455600000L;
  private final static long JANUARY_1_2019_OAM = 1546300800000L;
  private final static String DAILY_GRANULARITY = Period.days(1).toString();
  public static final String PARIS_TIMEZONE = "Europe/Paris";

  @Test
  public void testDefaultIntervalWithDailyGranularity() {
    final AlertMetadataDTO alertMetadataDTO = new AlertMetadataDTO().setGranularity(
        DAILY_GRANULARITY);
    final Interval res = AlertInsightsProvider.getDefaultInterval(JANUARY_1_2019_OAM,
        JANUARY_1_2022_2AM,
        alertMetadataDTO);

    final Interval expected = new Interval(JANUARY_1_2021_0AM,
        JANUARY_1_2022_0AM,
        DateTimeZone.UTC);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultIntervalWithDailyGranularityWithParisTimezone() {
    final AlertMetadataDTO alertMetadataDTO = new AlertMetadataDTO().setGranularity(
        DAILY_GRANULARITY).setTimezone(PARIS_TIMEZONE);
    final Interval res = AlertInsightsProvider.getDefaultInterval(JANUARY_1_2019_OAM,
        JANUARY_1_2022_2AM,
        alertMetadataDTO);

    final Interval expected = new Interval(DECEMBER_31_2020_11PM,
        DECEMBER_31_2022_11PM,
        DateTimeZone.forID(PARIS_TIMEZONE));
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultIntervalWithDailyGranularityWithStartOfDatasetBeforeDefaultStart() {
    final AlertMetadataDTO alertMetadataDTO = new AlertMetadataDTO().setGranularity(
        DAILY_GRANULARITY);
    final Interval res = AlertInsightsProvider.getDefaultInterval(JULY_1_2021_4AM,
        JANUARY_1_2022_2AM,
        alertMetadataDTO);

    final Interval expected = new Interval(JULY_2_2021_0AM,
        JANUARY_1_2022_0AM,
        DateTimeZone.UTC);
    assertThat(res).isEqualTo(expected);
  }
}
