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
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Chip } from "@material-ui/core";
import React from "react";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { extractDetectionEvaluation } from "../../../utils/alerts/alerts.util";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import {
    baselineOffsetToMilliseconds,
    comparisonOffsetReadableValue,
} from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import {
    SeriesType,
    TimeSeriesChartProps,
} from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis.interfaces";

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
    nonFiltered: AlertEvaluation,
    filtered: AlertEvaluation,
    startTime: number,
    endTime: number,
    comparisonOffset: string,
    translation: (labelName: string) => string = (s) => s
): TimeSeriesChartProps => {
    const filteredTimeSeriesData = extractDetectionEvaluation(filtered)[0].data;
    const nonFilteredTimeSeriesData =
        extractDetectionEvaluation(nonFiltered)[0].data;

    const series = [
        {
            enabled: false,
            name: translation("label.upper-and-lower-bound"),
            type: SeriesType.AREA_CLOSED,
            fillOpacity: 0.1,
            color: Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
            stroke: Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
            strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT,
            data: filteredTimeSeriesData.lowerBound.map((value, idx) => {
                return {
                    y: value,
                    y1: filteredTimeSeriesData.upperBound[idx],
                    x: filteredTimeSeriesData.timestamp[idx],
                };
            }),
            tooltipValueFormatter: (value: number): string =>
                formatLargeNumberV1(value),
        },
        {
            name: translation("label.non-filtered"),
            data: nonFilteredTimeSeriesData.current.map((value, idx) => {
                return {
                    y: value,
                    x: nonFilteredTimeSeriesData.timestamp[idx],
                };
            }),
            enabled: false,
        },
        {
            name: translation("label.filtered"),
            data: filteredTimeSeriesData.current.map((value, idx) => {
                return {
                    y: value,
                    x: filteredTimeSeriesData.timestamp[idx],
                };
            }),
        },
        {
            name: translation("label.predicted"),
            data: filteredTimeSeriesData.expected.map((value, idx) => {
                return {
                    y: value,
                    x: filteredTimeSeriesData.timestamp[idx],
                };
            }),
        },
    ];

    return {
        brush: true,
        height: 400,
        series,
        xAxis: {
            plotBands: [
                {
                    start: startTime,
                    end: endTime,
                    name: translation("label.anomaly-period"),
                    color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                    opacity: 0.2,
                },
                {
                    start:
                        startTime -
                        baselineOffsetToMilliseconds(comparisonOffset),
                    end:
                        endTime -
                        baselineOffsetToMilliseconds(comparisonOffset),
                    name: comparisonOffsetReadableValue(comparisonOffset),
                    color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                    opacity: 0.2,
                },
            ],
        },
    };
};

export const generateFilterOptions = (
    names: string[],
    dimensionColumns: string[],
    otherDimensionValues: string[]
): AnomalyFilterOption[] => {
    const filters: AnomalyFilterOption[] = [];

    names.forEach((dimensionValue, idx) => {
        if (dimensionValue === SERVER_VALUE_FOR_OTHERS) {
            otherDimensionValues.forEach((otherValue) => {
                filters.push({ key: dimensionColumns[idx], value: otherValue });
            });
        } else if (dimensionValue !== SERVER_VALUE_ALL_VALUES) {
            // (All) means no filter on the column
            filters.push({ key: dimensionColumns[idx], value: dimensionValue });
        }
    });

    return filters;
};

export const generateFilterStrings = (
    names: string[],
    dimensionColumns: string[],
    otherDimensionValues: string[]
): string[] => {
    return generateFilterOptions(names, dimensionColumns, otherDimensionValues)
        .map((item) => concatKeyValueWithEqual(item, false))
        .sort();
};
