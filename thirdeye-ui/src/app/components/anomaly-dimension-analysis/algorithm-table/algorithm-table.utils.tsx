import { Chip } from "@material-ui/core";
import React from "react";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import { Palette } from "../../../utils/material-ui/palette.util";
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

export const generateComparisonChartOptions = (
    current: AlertEvaluation,
    baseline: AlertEvaluation,
    anomaly: Anomaly
): TimeSeriesChartProps => {
    const series = [
        {
            name: "Current",
            data: current.detectionEvaluations.output_AnomalyDetectorResult_0.data.current.map(
                (value, idx) => {
                    return {
                        y: value,
                        x: current.detectionEvaluations
                            .output_AnomalyDetectorResult_0.data.timestamp[idx],
                    };
                }
            ),
        },
        {
            name: "Baseline",
            data: baseline.detectionEvaluations.output_AnomalyDetectorResult_0.data.current.map(
                (value, idx) => {
                    return {
                        y: value,
                        x: baseline.detectionEvaluations
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
                    name: "Anomaly",
                    color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                    opacity: 0.2,
                },
            ],
        },
    };
};
