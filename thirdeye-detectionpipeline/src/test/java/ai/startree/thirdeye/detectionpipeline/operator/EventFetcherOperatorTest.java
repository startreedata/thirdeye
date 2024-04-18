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
package ai.startree.thirdeye.detectionpipeline.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.ApplicationContext;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineConfiguration;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EventFetcherOperatorTest {

  public static final String TYPE_DEPLOYMENT = "DEPLOYMENT";
  public static final String TYPE_HOLIDAY = "HOLIDAY";
  public static final long CHRISTMAS_START = 10000L;
  public static final long CHRISTMAS_END = 2L;
  public static final long FR_ONLY_START = 1000L;
  public static final long FR_ONLY_END = 2000L;
  public static final long DEPLOY_START = 1L;
  public static final long DEPLOY_END = 2L;
  public static final String DIMENSION_COUNTRY = "country";
  public static final String COUNTRY_VALUE_US = "US";
  public static final String COUNTRY_VALUE_FR = "FR";
  public static final String NAME_CHRISTMAS = "CHRISTMAS";
  public static final String NAME_FR_ONLY = "FR_ONLY";
  public static final String NAME_DEPLOY_PROD = "DEPLOY_PROD";
  public static final EventDTO CHRISTMAS_EVENT = new EventDTO().setName(NAME_CHRISTMAS)
      .setEventType(TYPE_HOLIDAY)
      .setStartTime(CHRISTMAS_START)
      .setEndTime(CHRISTMAS_END)
      .setTargetDimensionMap(Map.of(DIMENSION_COUNTRY,
          List.of(COUNTRY_VALUE_US, COUNTRY_VALUE_FR)));
  public static final EventDTO FR_ONLY_EVENT = new EventDTO().setName(NAME_FR_ONLY)
      .setEventType(TYPE_HOLIDAY)
      .setStartTime(FR_ONLY_START)
      .setEndTime(FR_ONLY_END)
      .setTargetDimensionMap(Map.of(DIMENSION_COUNTRY, List.of(COUNTRY_VALUE_FR)));
  public static final EventDTO DEPLOY_EVENT = new EventDTO().setName(NAME_DEPLOY_PROD)
      .setEventType(TYPE_DEPLOYMENT)
      .setStartTime(DEPLOY_START)
      .setEndTime(DEPLOY_END)
      .setTargetDimensionMap(Map.of(DIMENSION_COUNTRY,
          List.of(COUNTRY_VALUE_US, COUNTRY_VALUE_FR)));

  private PlanNodeContext planNodeContext;

  @BeforeMethod
  public void setUp() {
    final EventManager eventDao = mock(EventManager.class);
    when(eventDao.findEventsBetweenTimeRange(anyLong(),
        anyLong(),
        nullable(List.class),
        nullable(String.class))).thenReturn(List.of(
        CHRISTMAS_EVENT,
        FR_ONLY_EVENT,
        DEPLOY_EVENT));
    planNodeContext = new PlanNodeContext().setDetectionPipelineContext(
        new DetectionPipelineContext().setApplicationContext(
            new ApplicationContext(
                mock(DataSourceCache.class),
                mock(DetectionRegistry.class),
                mock(PostProcessorRegistry.class),
                eventDao,
                mock(DataSourceManager.class), 
                mock(DatasetConfigManager.class),
                mock(ExecutorService.class),
                new DetectionPipelineConfiguration(),
                mock(EnumerationItemMaintainer.class))
        ));
  }

  @Test
  public void testEventFetcherOperator() throws Exception {
    final PlanNodeBean planNodeBean = new PlanNodeBean().setName("root")
        .setType("EventFetcher")
        // check that all parameters are parsed correctly - but don't test behavior, event manager is mocked
        .setParams(TemplatableMap.fromValueMap(ImmutableMap.of("component.startTimeLookback",
            "P2D",
            "component.endTimeLookback",
            "P1D",
            "component.lookaround",
            "P3D",
            "component.eventTypes",
            List.of("HOLIDAY"),
            "component.sqlFilter",
            "'US' member of dimensionMap['country']")))
        .setOutputs(List.of(new OutputBean().setOutputKey("events").setOutputName("events")));

    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(new Interval(0L, 1L, DateTimeZone.UTC)) // ignored with FROM_DATA
        .setPlanNode(planNodeBean)
        .setPlanNodeContext(planNodeContext);

    final EventFetcherOperator eventFetcherOperator = new EventFetcherOperator();
    eventFetcherOperator.init(context);
    eventFetcherOperator.execute();

    assertThat(eventFetcherOperator.getOutputs().size()).isEqualTo(1);
    final DataTable eventDataTable = (DataTable) eventFetcherOperator.getOutputs()
        .get("events");

    final DataFrame expectedDataFrame = new DataFrame()
        .addSeries(Constants.COL_EVENT_NAME,
            NAME_CHRISTMAS, NAME_FR_ONLY, NAME_DEPLOY_PROD)
        .addSeries(Constants.COL_EVENT_START, CHRISTMAS_START, FR_ONLY_START, DEPLOY_START)
        .addSeries(Constants.COL_EVENT_END, CHRISTMAS_END, FR_ONLY_END, DEPLOY_END);

    assertThat(eventDataTable.getDataFrame()).isEqualTo(expectedDataFrame);
  }
}
