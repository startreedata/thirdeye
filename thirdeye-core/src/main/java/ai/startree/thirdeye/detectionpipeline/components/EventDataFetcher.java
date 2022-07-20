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
package ai.startree.thirdeye.detectionpipeline.components;

import static ai.startree.thirdeye.spi.Constants.COL_EVENT_END;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_NAME;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_START;
import static ai.startree.thirdeye.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.detectionpipeline.spec.EventFetcherSpec;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;
import org.joda.time.Period;

public class EventDataFetcher implements DataFetcher<EventFetcherSpec> {

  private static final Period DEFAULT_START_TIME_LOOKBACK = Period.ZERO;
  private static final Period DEFAULT_END_TIME_LOOKBACK = Period.ZERO;
  private static final Period DEFAULT_LOOKAROUND = Period.days(1);

  private Period startTimeLookback;
  private Period endTimeLookback;
  private Period lookaround;
  private @NonNull List<String> eventTypes;
  private @Nullable String freeTextSqlFilter;

  private EventManager eventDao;

  @Override
  public void init(final EventFetcherSpec spec) {
    startTimeLookback = isoPeriod(spec.getStartTimeLookback(), DEFAULT_START_TIME_LOOKBACK);
    endTimeLookback = isoPeriod(spec.getEndTimeLookback(), DEFAULT_END_TIME_LOOKBACK);
    lookaround = isoPeriod(spec.getLookaround(), DEFAULT_LOOKAROUND);
    eventTypes = Objects.requireNonNull(spec.getEventTypes());
    freeTextSqlFilter = spec.getSqlFilter();

    eventDao = spec.getEventManager();
  }

  /**
   * The underlying DataFrame has the following columns:
   * {@value Constants#COL_EVENT_NAME}: string series: name of the event
   * {@value Constants#COL_EVENT_START}: long series: timestamp of the start of the event
   * {@value Constants#COL_EVENT_END}: long series: timestamp of the end of the event
   */
  @Override
  public DataTable getDataTable(final Interval detectionInterval) throws Exception {
    final long minTime = detectionInterval.getStart()
        .minus(startTimeLookback)
        .minus(lookaround)
        .getMillis();
    final long maxTime = detectionInterval.getEnd()
        .minus(endTimeLookback)
        .plus(lookaround)
        .getMillis();

    final List<EventDTO> events = eventDao.findEventsBetweenTimeRange(minTime,
        maxTime,
        eventTypes,
        freeTextSqlFilter
    );

    final DataFrame eventDf = toDataFrame(events);
    return SimpleDataTable.fromDataFrame(eventDf);
  }

  private DataFrame toDataFrame(final List<EventDTO> events) {
    String[] eventNames = new String[events.size()];
    long[] eventStarts = new long[events.size()];
    long[] eventEnds = new long[events.size()];

    for (int i=0; i< events.size();i++) {
      var e = events.get(i);
      eventNames[i] = e.getName();
      eventStarts[i] = e.getStartTime();
      eventEnds[i] = e.getEndTime();
    }

    return new DataFrame()
        .addSeries(COL_EVENT_NAME, eventNames)
        .addSeries(COL_EVENT_START, eventStarts)
        .addSeries(COL_EVENT_END, eventEnds);
  }
}
