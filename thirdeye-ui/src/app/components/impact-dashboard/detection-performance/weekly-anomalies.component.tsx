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
import TitleCard from "../../title-card/title-card.component";
import { BarGraph } from "../../visualizations/bar-graph/bar-graph.component";
import { TitleRenderer } from "./title-renderer.component";

export const WeekAnomaliesGraph = ({
    title,
    notificationText,
    anomalies,
    selectedAnalysisPeriod,
    previousPeriodAnomalies,
}: AnomaliesGraphProps): JSX.Element => {
    const currentWeeklyTimeWindow = getTimeWindows(
        anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod].startTime,
        anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod].endTime,
        24 * 7
    );
    const previousWeeklyTimeWindow = getTimeWindows(
        anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod]
            .startTime,
        anaylysisPeriodPreviousWindowTimeMapping[selectedAnalysisPeriod]
            .endTime,
        24 * 7
    );
    const groupedAnomaliesDataByTimeCurrentWeeklyWindow =
        groupDataByTimeWindows(anomalies || [], currentWeeklyTimeWindow);

    const groupedAnomaliesDataByTimePrevioustWeeklyWindow =
        groupDataByTimeWindows(
            previousPeriodAnomalies || [],
            previousWeeklyTimeWindow
        );

    const currentPeriodWeeklyDataPoints =
        groupedAnomaliesDataByTimeCurrentWeeklyWindow.map((data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        });

    const previoustPeriodWeeklyDataPoints =
        groupedAnomaliesDataByTimePrevioustWeeklyWindow.map((data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        });
    const barGraphData = currentPeriodWeeklyDataPoints.map((data, idx) => {
        const d = epochToDate(
            groupedAnomaliesDataByTimeCurrentWeeklyWindow[idx].windowStart
        );
        const date = new Date();
        date.setDate(idx + 1);

        return {
            currentPeriod: data.y,
            previousPeriod: previoustPeriodWeeklyDataPoints[idx].y,
            date: d,
        };
    });

    return (
        <>
            <TitleCard
                content={
                    <BarGraph
                        data={barGraphData}
                        height={520}
                        keys={["currentPeriod", "previousPeriod"]}
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
