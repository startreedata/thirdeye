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
import { useStyles } from "./detection-performance.styles";
import { Typography } from "@material-ui/core";
import AnalysisPeriod from "../anaylysis-period/analysis-period.component";
import { DetectionPerformanceProps } from "./detection-performance.interfaces";
import { epochToDate, getTimeWindows, groupDataByTimeWindows } from "./util";
import {
    anaylysisPeriodStartTimeMapping,
    anaylysisPeriodPreviousWindowTimeMapping,
} from "../../../platform/utils";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import TitleCard from "../../title-card/title-card.component";
import BarGraph from "../../visualizations/bar-graph/bar-graph.component";

const DetectionPerformance = ({
    anomalies,
    previousPeriodAnomalies,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: DetectionPerformanceProps): ReactElement => {
    const componentStyles = useStyles();

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

    const currentSeriesData: { x: number; y: number }[] =
        groupedAnomaliesDataByTimeCurrentWindow.map((data) => {
            return {
                x: data.windowStart,
                y: data.data.length,
            };
        });
    const groupedAnomaliesDataByTimePreviousWindow = groupDataByTimeWindows(
        previousPeriodAnomalies || [],
        previousTimeWindow
    );
    const previousSeriesData: { x: number; y: number }[] =
        groupedAnomaliesDataByTimePreviousWindow.map((data, idx) => {
            return {
                x: groupedAnomaliesDataByTimeCurrentWindow[idx].windowStart,
                y: data.data.length,
            };
        });
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
            data: currentSeriesData,
        },
        {
            name: `Previous ${selectedAnalysisPeriod} period(${previousPeriodReadableDate.startTime}-${previousPeriodReadableDate.endTime})`,
            data: previousSeriesData,
        },
    ];

    const barGraphData = currentSeriesData.map((data, idx) => {
        const d = epochToDate(
            groupedAnomaliesDataByTimeCurrentWindow[idx].windowStart
        );
        const date = new Date();
        date.setDate(idx + 1);

        return {
            currentPeriod: data.y,
            previousPeriod: previousSeriesData[idx].y,
            date: d,
        };
    });

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <Typography>Detection performance</Typography>
                <AnalysisPeriod
                    analysisPeriods={analysisPeriods}
                    selectedPeriod={selectedAnalysisPeriod}
                    onClick={onAnalysisPeriodChange}
                />
            </div>
            <div className={componentStyles.visualizationContainer}>
                <div className={componentStyles.visualization}>
                    <TitleCard
                        content={<TimeSeriesChart height={500} series={sd} />}
                        title="Total # of anomalies detected"
                    />
                </div>
                <div className={componentStyles.visualization}>
                    <TitleCard
                        content={
                            // <TimeSeriesChart
                            //   height={500}
                            //   series={sd}
                            // />
                            <BarGraph
                                data={barGraphData}
                                keys={["currentPeriod", "previousPeriod"]}
                            />
                        }
                        title="Weekly anomalies reported"
                    />
                </div>
            </div>
        </>
    );
};

export default DetectionPerformance;
