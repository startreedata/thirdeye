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
import { useTranslation } from "react-i18next";
// Interfaces
import {
    AnomaliesGraphProps,
    TooltipData,
} from "./detection-performance.interfaces";

// Utils
import { epochToDate, getTimeWindows, groupDataByTimeWindows } from "./util";
import {
    anaylysisPeriodStartTimeMapping,
    anaylysisPeriodPreviousWindowTimeMapping,
} from "../../../platform/utils";

// Components
import TitleCard from "../../title-card/title-card.component";
import { BarGraph } from "../../visualizations/bar-graph/bar-graph.component";
import { TitleRenderer } from "./title-renderer.component";
import { ReactElement } from "react-markdown/lib/react-markdown";

// Styles
import { useDetectionPerformanceStyles } from "./detection-performance.styles";

export const WeeklyGraph = ({
    title,
    notificationText,
    anomalies,
    selectedAnalysisPeriod,
    previousPeriodAnomalies,
}: AnomaliesGraphProps): JSX.Element => {
    const { t } = useTranslation();
    const componentStyles = useDetectionPerformanceStyles();

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
        const currentWindowStartDate = epochToDate(
            groupedAnomaliesDataByTimeCurrentWeeklyWindow[idx].windowStart
        );
        const currentWindowEndDate = epochToDate(
            groupedAnomaliesDataByTimeCurrentWeeklyWindow[idx].windowEnd
        );
        const previousWindowStartDate = epochToDate(
            groupedAnomaliesDataByTimePrevioustWeeklyWindow[idx].windowStart
        );
        const previousWindowEndDate = epochToDate(
            groupedAnomaliesDataByTimePrevioustWeeklyWindow[idx].windowEnd
        );

        return {
            currentPeriod: data.y,
            previousPeriod: previoustPeriodWeeklyDataPoints[idx].y,
            date: currentWindowStartDate,
            tooltipData: {
                currentPeriod: data.y,
                previousPeriod: previoustPeriodWeeklyDataPoints[idx].y,
                currentPeriodDate: `${currentWindowStartDate}-${currentWindowEndDate}`,
                previousPeriodDate: `${previousWindowStartDate}-${previousWindowEndDate}`,
            },
        };
    });

    const tootltipRender = (tooltipData: unknown): ReactElement => {
        const data = tooltipData as TooltipData;

        return (
            <div className={componentStyles.tooltip}>
                <div className="currentPeriod">
                    {`${t("pages.impact-dashboard.common.current-period")}(${
                        data.currentPeriodDate
                    }): ${data.currentPeriod}`}
                </div>
                <div className="previousPeriod">
                    {`${t("pages.impact-dashboard.common.previous-period")}(${
                        data.previousPeriodDate
                    }): ${data.previousPeriod}`}
                </div>
            </div>
        );
    };

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

    const keysColorMapping = {
        currentPeriod: "#F37B0E",
        previousPeriod: "#006CA7",
    };

    const graphLegend = [
        {
            text: `Current ${selectedAnalysisPeriod} period`,
            value: keysColorMapping.currentPeriod,
        },
        {
            text: `Previous ${selectedAnalysisPeriod} period(${previousPeriodReadableDate.startTime}-${previousPeriodReadableDate.endTime})`,
            value: keysColorMapping.previousPeriod,
        },
    ];

    return (
        <>
            <TitleCard
                content={
                    <BarGraph
                        data={barGraphData}
                        graphLegend={graphLegend}
                        height={480}
                        keysColorMapping={keysColorMapping}
                        tooltipRenderer={tootltipRender}
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
