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

import static ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator.getCorrectedInterval;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class AlertDetectionIntervalCalculatorTest {

  private static final DateTimeFormatter DATE_PARSER = DateTimeFormat.forPattern(
      "yyyy-MM-dd HH:mm:ss.SSS z");

  private static final long ALERT_ID = 0;
  private static final String DATASET_NAME = "my_dataset";

  @Test
  public void testGetCorrectedIntervalNoMetadata() {
    // test that timeframe is unchanged when there is no metadata
    DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO();
    Interval output = getCorrectedInterval(ALERT_ID,
        inputTaskStart.getMillis(),
        inputTaskEnd.getMillis(),
        inputAlertTemplate);
    Interval expected = new Interval(inputTaskStart, inputTaskEnd);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalNoDelayNorGranularity() {
    // test that timeframe is unchanged when there is no completenessDelay nor granularity
    DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(DATASET_NAME);
    AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO().setMetadata(new AlertMetadataDTO().setDataset(
        datasetConfigDTO));
    Interval output = getCorrectedInterval(ALERT_ID,
        inputTaskStart.getMillis(),
        inputTaskEnd.getMillis(),
        inputAlertTemplate);
    Interval expected = new Interval(inputTaskStart, inputTaskEnd);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithDelay() {
    // test that endtime is changed when there is a delay
    DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(DATASET_NAME);
    datasetConfigDTO.setCompletenessDelay("P1D"); // delay of 1 day
    AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO));
    Interval output = getCorrectedInterval(ALERT_ID,
        inputTaskStart.getMillis(),
        inputTaskEnd.getMillis(),
        inputAlertTemplate);

    Interval expected = new Interval(
        inputTaskStart,  // unchanged
        DATE_PARSER.parseDateTime("2021-11-23 13:02:12.333 UTC"));  // minus 1 day

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithGranularity() {
    // test that timeframe is changed when there is a granularity
    DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(DATASET_NAME);
    AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO()
            .setDataset(datasetConfigDTO)
            .setGranularity("PT1H")  // granularity of 1H
        );
    Interval output = getCorrectedInterval(ALERT_ID,
        inputTaskStart.getMillis(),
        inputTaskEnd.getMillis(),
        inputAlertTemplate);

    Interval expected = new Interval(
        DATE_PARSER.parseDateTime("2021-11-22 11:00:00.000 UTC"),   // floored to hour
        DATE_PARSER.parseDateTime("2021-11-24 13:00:00.000 UTC"));  // floored to hour

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithDelayAndGranularity() {
    // test that timeframe is changed when there is both delay and granularity
    DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(DATASET_NAME);
    datasetConfigDTO.setCompletenessDelay("PT2H"); // delay of 2 hours
    AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO()
            .setDataset(datasetConfigDTO)
            .setGranularity("PT5M")  // granularity of 5 minutes
        );
    Interval output = getCorrectedInterval(ALERT_ID,
        inputTaskStart.getMillis(),
        inputTaskEnd.getMillis(),
        inputAlertTemplate);

    Interval expected = new Interval(
        DATE_PARSER.parseDateTime("2021-11-22 11:20:00.000 UTC"),   // floored to 5 minutes
        DATE_PARSER.parseDateTime("2021-11-24 11:00:0.000 UTC"));  // minus 2 hours + floored to 5 minutes

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithStartTimeGreaterThanEndTimeMinusDelay() {
    // test that both start and endTime are changed when end-delay < start
    // this can happen if delay is augmented
    DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(DATASET_NAME);
    datasetConfigDTO.setCompletenessDelay("P3D");   // delay of 3 day end - delay < start
    AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO));
    Interval output = getCorrectedInterval(ALERT_ID,
        inputTaskStart.getMillis(),
        inputTaskEnd.getMillis(),
        inputAlertTemplate);

    Interval expected = new Interval(
        DATE_PARSER.parseDateTime("2021-11-19 11:22:33.444 UTC"),   // minus 3 days
        DATE_PARSER.parseDateTime("2021-11-21 13:02:12.333 UTC"));  // minus 3 days

    assertThat(output).isEqualTo(expected);

  }
}
