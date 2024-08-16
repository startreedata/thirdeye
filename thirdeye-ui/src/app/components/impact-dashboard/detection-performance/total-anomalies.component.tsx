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
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React from "react";
import { AnomaliesGraphProps } from "./detection-performance.interfaces";
import { epochToDate, getTimeWindows, groupDataByTimeWindows } from "./util";
import {
    anaylysisPeriodStartTimeMapping,
    anaylysisPeriodPreviousWindowTimeMapping,
} from "../../../platform/utils";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import TitleCard from "../../title-card/title-card.component";
import { TitleRenderer } from "./title-renderer.component";

export const TotalAnomaliesGraph = ({
    title,
    notificationText,
    selectedAnalysisPeriod,
    anomalies,
    previousPeriodAnomalies,
}: AnomaliesGraphProps): JSX.Element => {
    const currentTimeWindow = getTimeWindows(
        anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod].startTime,
        anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod].endTime
    );
    const previousTimeWindow = getTimeWindows(
        anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod]
            .startTime,
        anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod].endTime
    );
    const groupedAnomaliesDataByTimeCurrentWindow = groupDataByTimeWindows(
        anomalies || [],
        currentTimeWindow
    );

    const currentPeriodDataPoints = groupedAnomaliesDataByTimeCurrentWindow.map(
        (data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        }
    );

    const currentPeriodCumulativeSumDataPoints = currentPeriodDataPoints.map(
        (d, idx) => {
            return {
                x: d.x,
                y:
                    d.y +
                    (idx > 0
                        ? currentPeriodDataPoints.reduce((acc, curr, index) => {
                              if (index < idx) {
                                  return acc + curr.y;
                              } else {
                                  return acc;
                              }
                          }, currentPeriodDataPoints[0].y)
                        : 0),
            };
        }
    );

    const groupedAnomaliesDataByTimePreviousWindow = groupDataByTimeWindows(
        previousPeriodAnomalies || [],
        previousTimeWindow
    );
    const previousPeriodDataPoints =
        groupedAnomaliesDataByTimePreviousWindow.map((data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        });

    const previousPeriodCumulativeSumDataPoints = previousPeriodDataPoints.map(
        (d, idx) => {
            return {
                x: d.x,
                y:
                    d.y +
                    (idx > 0
                        ? previousPeriodDataPoints.reduce(
                              (acc, curr, index) => {
                                  if (index < idx) {
                                      return acc + curr.y;
                                  } else {
                                      return acc;
                                  }
                              },
                              previousPeriodDataPoints[0].y
                          )
                        : 0),
            };
        }
    );
    const previousPeriodReadableDate = {
        startTime: epochToDate(
            anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod]
                .startTime
        ),
        endTime: epochToDate(
            anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod]
                .endTime
        ),
    };
    const sd = [
        {
            name: `Current ${selectedAnalysisPeriod} period`,
            data: currentPeriodCumulativeSumDataPoints, // currentSeriesData,
        },
        {
            name: `Previous ${selectedAnalysisPeriod} period(${previousPeriodReadableDate.startTime}-${previousPeriodReadableDate.endTime})`,
            data: previousPeriodCumulativeSumDataPoints, // previousSeriesData,
        },
    ];

    return (
        <>
            <TitleCard
                content={<TimeSeriesChart height={500} series={sd} />}
                title={
                    <TitleRenderer
                        notificationText={notificationText}
                        title={title}
                    />
                }
            />
        </>
    );
};
