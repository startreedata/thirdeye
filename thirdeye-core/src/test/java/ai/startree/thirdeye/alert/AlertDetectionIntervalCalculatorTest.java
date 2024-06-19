/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.util.TimeUtils;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
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
    when(alertTemplateRenderer.renderAlert(any(AlertDTO.class))).then(
        i -> ((AlertDTO) i.getArguments()[0]).getTemplate());
    intervalCalculator = new AlertDetectionIntervalCalculator(alertTemplateRenderer);
  }

  @Test
  public void testGetCorrectedIntervalNoMetadata() throws IOException, ClassNotFoundException {
    // metadata is required
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO();
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);

    assertThatThrownBy(() -> intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis())).isInstanceOf(
        IllegalArgumentException.class);
  }

  @Test
  public void testGetCorrectedIntervalNoGranularity()
      throws IOException, ClassNotFoundException {
    // metadata$granularity is required
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setDataset(DATASET_NAME);
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO().setMetadata(
        new AlertMetadataDTO().setDataset(
            datasetConfigDTO));
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    assertThatThrownBy(() -> intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis())).isInstanceOf(
        IllegalArgumentException.class);
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
  public void testGetCorrectedIntervalWithDelayNotAppliedAndGranularity()
      throws IOException, ClassNotFoundException {
    // test that timeframe is changed when there is both delay and granularity
    // with delay not applied because taskEnd is smaller than data watermark
    final DateTime inputTaskStart = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime inputTaskEnd = DATE_PARSER.parseDateTime("2021-11-24 13:02:12.333 UTC");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(DATASET_NAME)
        // delay of 2 hours - will not be applied
        .setCompletenessDelay("PT2H");
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
            "2021-11-24 13:00:0.000 UTC"));  // floored to 5 minutes + delay not applied

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithDelayAppliedAndGranularity()
      throws IOException, ClassNotFoundException {
    // test that timeframe is changed when there is both delay and granularity
    // delay will be applied because taskEnd is greater than the data watermark
    final DateTime inputTaskEnd = new DateTime(Constants.DEFAULT_CHRONOLOGY);
    final DateTime inputTaskStart = inputTaskEnd.minus(Period.days(3).withHours(2));
    final Period granularity = isoPeriod("PT5M");
    final Period completenessDelay = isoPeriod("PT2H");

    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(DATASET_NAME)
        // delay of 2 hours - will be applied
        .setCompletenessDelay(completenessDelay.toString());
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO()
            .setDataset(datasetConfigDTO)
            .setGranularity(granularity.toString())  // granularity of 5 minutes
        );
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());

    final Interval expected = new Interval(
        TimeUtils.floorByPeriod(inputTaskStart, granularity),   // floored to 5 minutes
        // delay applied + floored to 5 minutes
        TimeUtils.floorByPeriod(inputTaskEnd.minus(completenessDelay), granularity));

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetCorrectedIntervalWithStartTimeGreaterThanEndTimeMinusDelay()
      throws IOException, ClassNotFoundException {
    // test that both start and endTime are changed when end-delay < start
    // this can happen if delay value is increased
    final DateTime inputTaskEnd = new DateTime(Constants.DEFAULT_CHRONOLOGY);
    final DateTime inputTaskStart = inputTaskEnd.minus(Period.days(2).withHours(1));
    final Period granularity = isoPeriod("PT5M");
    final Period completenessDelay = isoPeriod("P3D");

    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(DATASET_NAME)
        .setCompletenessDelay("P3D");   // delay of 3 day end - delay < start
    final AlertTemplateDTO inputAlertTemplate = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO).setGranularity(
            granularity.toString()));
    final AlertDTO inputAlertDto = (AlertDTO) new AlertDTO().setTemplate(inputAlertTemplate)
        .setId(ALERT_ID);
    final Interval output = intervalCalculator.getCorrectedInterval(inputAlertDto,
        inputTaskStart.getMillis(), inputTaskEnd.getMillis());

    final Interval expected = new Interval(
        // minus 3 days + floored to 5 minutes
        TimeUtils.floorByPeriod(inputTaskStart.minus(completenessDelay), granularity),
        // delay applied + floored to 5 minutes
        TimeUtils.floorByPeriod(inputTaskEnd.minus(completenessDelay), granularity));

    assertThat(output).isEqualTo(expected);
  }
}
