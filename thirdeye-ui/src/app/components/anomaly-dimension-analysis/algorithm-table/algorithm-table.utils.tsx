import { Chip } from "@material-ui/core";
import React from "react";
import {
    AnomalyBreakdownAPIOffsetsToWeeks,
    AnomalyBreakdownAPIOffsetValues,
    OFFSET_TO_HUMAN_READABLE,
} from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { WEEK_IN_MILLISECONDS } from "../../../utils/time/time.util";
import { TimeSeriesChartProps } from "../../visualizations/time-series-chart/time-series-chart.interfaces";

export const SERVER_VALUE_FOR_OTHERS = "(ALL_OTHERS)";
export const SERVER_VALUE_ALL_VALUES = "(ALL)";

export const generateName = (
    rowData: AnomalyDimensionAnalysisMetricRow,
    metric: string,
    dataset: string,
    dimensionColumns: string[]
): JSX.Element => {
    const chips: JSX.Element[] = [];

    rowData.names.forEach((dimensionValue: string, idx: number) => {
        let displayValue = dimensionValue;

        // Values that are `(All)` indicate there are no filters for that column
        if (displayValue === SERVER_VALUE_ALL_VALUES) {
            return;
        }

        if (displayValue === "") {
            displayValue = EMPTY_STRING_DISPLAY;
        }

        return chips.push(
            <Chip
                label={`${dimensionColumns[idx]}=${displayValue}`}
                size="small"
            />
        );
    });

    return (
        <span>
            {metric} from {dataset} filtered by ({chips})
        </span>
    );
};

export const generateOtherDimensionTooltipString = (
    dimensionValuesForOther: string[]
): string => {
    return `${SERVER_VALUE_FOR_OTHERS} includes: ${dimensionValuesForOther.join(
        ", "
    )}`;
};

export const offsetMilliseconds = (
    timestamp: number,
    offset: AnomalyBreakdownAPIOffsetValues
): number => {
    return (
        timestamp -
        WEEK_IN_MILLISECONDS * AnomalyBreakdownAPIOffsetsToWeeks[offset]
    );
};

export const generateComparisonChartOptions = (
    nonFiltered: AlertEvaluation,
    filtered: AlertEvaluation,
    anomaly: Anomaly,
    comparisonOffset: AnomalyBreakdownAPIOffsetValues
): TimeSeriesChartProps => {
    const series = [
        {
            name: "Non Filtered",
            data: nonFiltered.detectionEvaluations.output_AnomalyDetectorResult_0.data.current.map(
                (value, idx) => {
                    return {
                        y: value,
                        x: nonFiltered.detectionEvaluations
                            .output_AnomalyDetectorResult_0.data.timestamp[idx],
                    };
                }
            ),
            enabled: false,
        },
        {
            name: "Filtered",
            data: filtered.detectionEvaluations.output_AnomalyDetectorResult_0.data.current.map(
                (value, idx) => {
                    return {
                        y: value,
                        x: filtered.detectionEvaluations
                            .output_AnomalyDetectorResult_0.data.timestamp[idx],
                    };
                }
            ),
        },
    ];

    return {
        height: 150,
        series,
        xAxis: {
            plotBands: [
                {
                    start: anomaly.startTime,
                    end: anomaly.endTime,
                    name: "Anomaly Period",
                    color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                    opacity: 0.2,
                },
                {
                    start: offsetMilliseconds(
                        anomaly.startTime,
                        comparisonOffset
                    ),
                    end: offsetMilliseconds(anomaly.endTime, comparisonOffset),
                    name: OFFSET_TO_HUMAN_READABLE[comparisonOffset],
                    color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                    opacity: 0.2,
                },
            ],
        },
    };
};
