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
package ai.startree.thirdeye.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AlertDetectionIntervalCalculatorTest {

  private static final DateTimeFormatter DATE_PARSER = DateTimeFormat.forPattern(
      "yyyy-MM-dd HH:mm:ss.SSS z");

  private static final long ALERT_ID = 0;
  private static final String DATASET_NAME = "my_dataset";

  private AlertDetectionIntervalCalculator intervalCalculator;

  @BeforeMethod
  public void setUp() throws IOException, ClassNotFoundException {
    final AlertTemplateRenderer alertTemplateRenderer = mock(AlertTemplateRenderer.class);
    when(alertTemplateRenderer.renderAlert(any(AlertDTO.class), any())).then(
        i -> ((AlertDTO) i.getArguments()[0]).getTemplate());
    intervalCalculator = new AlertDetectionIntervalCalculator(alertTemplateRenderer);
  }

  @Test
  public void testGetCorrectedIntervalNoMetadata() throws IOException, ClassNotFoundException {
    // test that timeframe is unchanged when there is no metadata
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO();
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());
    final Interval expected = new Interval(inputTaskStart, inputTaskEnd);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalNoDelayNorGranularity()
      throws IOException, ClassNotFoundException {
    // test that timeframe is unchanged when there is no completenessDelay nor granularity
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setDataset(DATASET_NAME);
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO().setMetadata(
        new AlertMetadataDTO().setDataset(
            datasetConfigDTO));
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());
    Interval expected = new Interval(inputTaskStart, inputTaskEnd);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithDelay() throws IOException, ClassNotFoundException {
    // test that endtime is changed when there is a delay
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(DATASET_NAME)
        .setCompletenessDelay("P1D"); // delay of 1 day
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO));
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());

    final Interval expected = new Interval(
        inputTaskStart,  // unchanged
        DATE_PARSER.parseDateTime("2021-11-23 13:02:12.333 UTC"));  // minus 1 day

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithGranularity() throws IOException, ClassNotFoundException {
    // test that timeframe is changed when there is a granularity
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setDataset(DATASET_NAME);
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO()
            .setDataset(datasetConfigDTO)
            .setGranularity("PT1H")  // granularity of 1H
        );
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());

    final Interval expected = new Interval(
        DATE_PARSER.parseDateTime("2021-11-22 11:00:00.000 UTC"),   // floored to hour
        DATE_PARSER.parseDateTime("2021-11-24 13:00:00.000 UTC"));  // floored to hour

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithDelayAndGranularity()
      throws IOException, ClassNotFoundException {
    // test that timeframe is changed when there is both delay and granularity
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(DATASET_NAME)
        .setCompletenessDelay("PT2H"); // delay of 2 hours
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO()
            .setDataset(datasetConfigDTO)
            .setGranularity("PT5M")  // granularity of 5 minutes
        );
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());

    final Interval expected = new Interval(
        DATE_PARSER.parseDateTime("2021-11-22 11:20:00.000 UTC"),   // floored to 5 minutes
        DATE_PARSER.parseDateTime(
            "2021-11-24 11:00:0.000 UTC"));  // minus 2 hours + floored to 5 minutes

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithStartTimeGreaterThanEndTimeMinusDelay()
      throws IOException, ClassNotFoundException {
    // test that both start and endTime are changed when end-delay < start
    // this can happen if delay is augmented
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(DATASET_NAME)
        .setCompletenessDelay("P3D");   // delay of 3 day end - delay < start
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO));
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());

    final Interval expected = new Interval(
        DATE_PARSER.parseDateTime("2021-11-19 11:22:33.444 UTC"),   // minus 3 days
        DATE_PARSER.parseDateTime("2021-11-21 13:02:12.333 UTC"));  // minus 3 days

    assertThat(output).isEqualTo(expected);
  }
}
