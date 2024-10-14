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
import React, { ReactElement } from "react";
import { Orientation } from "@visx/axis";
// Interfaces
import { AnomaliesGraphProps } from "./detection-performance.interfaces";

// Utils
import { epochToDate, getTimeWindows, groupDataByTimeWindows } from "./util";
import {
    anaylysisPeriodStartTimeMapping,
    anaylysisPeriodPreviousWindowTimeMapping,
} from "../../../platform/utils";

// Components
import TitleCard from "../../title-card/title-card.component";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { TitleRenderer } from "./title-renderer.component";
import { useDetectionPerformanceStyles } from "./detection-performance.styles";
import { startCase } from "lodash";

export const LineGraph = ({
    title,
    notificationText,
    selectedAnalysisPeriod,
    anomalies,
    previousPeriodAnomalies,
}: AnomaliesGraphProps): JSX.Element => {
    const componentStyles = useDetectionPerformanceStyles();
    // Get all the 24hr time windows for the selected range
    const currentTimeWindow = getTimeWindows(
        anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod].startTime,
        anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod].endTime
    );
    const previousTimeWindow = getTimeWindows(
        anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod]
            .startTime,
        anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod].endTime
    );
    // group the anomalies based on their startime with the approprie time window
    const groupedAnomaliesDataByTimeCurrentWindow = groupDataByTimeWindows(
        anomalies || [],
        currentTimeWindow
    );
    const groupedAnomaliesDataByTimePreviousWindow = groupDataByTimeWindows(
        previousPeriodAnomalies || [],
        previousTimeWindow
    );

    /* create data point for every startTime of the window period with yAxis as number of
    anomalies in those periods */
    const currentPeriodDataPoints = groupedAnomaliesDataByTimeCurrentWindow.map(
        (data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        }
    );
    const previousPeriodDataPoints =
        groupedAnomaliesDataByTimePreviousWindow.map((data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        });

    /* At every date, we have to show anomalies for that date + anomalies recorded till
    that date in the period */
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
    const previousPeriodCumulativeSumDataPoints = previousPeriodDataPoints.map(
        (d, idx) => {
            return {
                x: currentPeriodDataPoints[idx].x,
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

    const tootltipRender = (
        tooltipData: { x: number; y: number },
        period: string
    ): ReactElement => {
        let date = tooltipData.x;

        let weeksToSubtract = 4;

        if (period === "previousPeriod") {
            switch (selectedAnalysisPeriod) {
                case "13w":
                    weeksToSubtract = 13;

                    break;
                case "26w":
                    weeksToSubtract = 26;
            }
            date = date - weeksToSubtract * 7 * 24 * 60 * 60 * 1000;
        }

        return (
            <div className={componentStyles.tooltip}>
                <div className={period}>
                    {`${startCase(period)} (${epochToDate(date)}): ${
                        tooltipData.y
                    }`}
                </div>
            </div>
        );
    };

    const seriesData = [
        {
            name: `Current ${selectedAnalysisPeriod} period`,
            data: currentPeriodCumulativeSumDataPoints,
            tooltip: {
                tooltipFormatter: (d: { x: number; y: number }) =>
                    tootltipRender(d, "currentPeriod"),
            },
        },
        {
            name: `Previous ${selectedAnalysisPeriod} period(${previousPeriodReadableDate.startTime}-${previousPeriodReadableDate.endTime})`,
            data: previousPeriodCumulativeSumDataPoints,
            tooltip: {
                tooltipFormatter: (d: { x: number; y: number }) =>
                    tootltipRender(d, "previousPeriod"),
            },
        },
    ];
    const colorPallete = ["#006CA7", "#F37B0E"];

    return (
        <>
            <TitleCard
                content={
                    <TimeSeriesChart
                        colorPalette={colorPallete}
                        height={500}
                        series={seriesData}
                        yAxis={{ position: Orientation.right }}
                    />
                }
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
