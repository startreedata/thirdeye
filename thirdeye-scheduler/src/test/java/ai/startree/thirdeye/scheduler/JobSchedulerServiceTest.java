package ai.startree.thirdeye.scheduler;/*
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.io.IOException;
import org.joda.time.Period;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JobSchedulerServiceTest {

  private JobSchedulerService jobSchedulerService;

  @BeforeMethod
  public void setUp() throws IOException, ClassNotFoundException {
    final AlertTemplateRenderer alertTemplateRenderer = mock(AlertTemplateRenderer.class);
    when(alertTemplateRenderer.renderAlert(any(AlertDTO.class), any())).then(
        i -> ((AlertDTO) i.getArguments()[0]).getTemplate());
    jobSchedulerService = new JobSchedulerService(null, null, null, null, alertTemplateRenderer);
  }

  @DataProvider(name = "computeTaskStartTestCases")
  public Object[][] computeTaskStartTestCases() {
    // one alert for each template
    final Object[] noMutabilityPeriod = {10L, 20L, null, 10L};
    final Object[] zeroMutabilityPeriod = {10L, 20L, Period.ZERO.toString(), 10L};
    final Object[] mutabilityPeriod = {10L, 20L, Period.millis(15).toString(), 5L};
    final Object[] mutabilityPeriodStartBiggerThanTaskStart = {10L, 20L,
        Period.millis(5).toString(), 10L};

    return new Object[][]{noMutabilityPeriod, zeroMutabilityPeriod, mutabilityPeriod,
        mutabilityPeriodStartBiggerThanTaskStart};
  }

  @Test(dataProvider = "computeTaskStartTestCases")
  public void testComputeTaskStart(final long lastTimestamp,
      final long endTime, final String mutabilityPeriod, final long expectedStartTime) {
    final AlertDTO alert = new AlertDTO().setTemplate(new AlertTemplateDTO().setMetadata(
            new AlertMetadataDTO().setDataset(
                new DatasetConfigDTO().setMutabilityPeriod(mutabilityPeriod))))
        .setLastTimestamp(lastTimestamp);
    final var output = jobSchedulerService.computeTaskStart(alert, endTime);
    assertThat(output).isEqualTo(expectedStartTime);
  }
}
