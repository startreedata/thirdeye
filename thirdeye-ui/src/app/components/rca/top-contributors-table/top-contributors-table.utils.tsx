/*
 * Copyright 2023 StarTree Inc
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
import { Box, Chip, Grid } from "@material-ui/core";
import { Group } from "@visx/group";
import { Bar, Circle, Line, LinePath } from "@visx/shape";
import { flatten } from "lodash";
import React from "react";
import { TFunction } from "react-i18next";
import { formatDateAndTimeV1, formatDateV1 } from "../../../platform/utils";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { extractDetectionEvaluation } from "../../../utils/alerts/alerts.util";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import { baselineOffsetToMilliseconds } from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { determineGranularity } from "../../../utils/visualization/visualization.util";
import {
    DataPoint,
    Series,
    SeriesType,
    TimeSeriesChartProps,
} from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { generateAnomalyPointsForStartEnd } from "../anomaly-time-series-card/anomaly-time-series-card.utils";
import { getColorForDimensionCombo } from "../investigation-preview/investigation-preview.utils";
import { ExtraDataForAnomalyDimensionAnalysisData } from "./top-contributors-table.interfaces";

export const SERVER_VALUE_FOR_OTHERS = "(ALL_OTHERS)";
export const SERVER_VALUE_FOR_NO_FILTER = "(NO_FILTER)";
export const SERVER_VALUE_ALL_VALUES = "(ALL)";
const BASELINE_LINE_COLOR =
    Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND;

export const generateName = (
    rowData: AnomalyDimensionAnalysisMetricRow,
    metric: string,
    dataset: string,
    dimensionColumns: string[],
    translation: TFunction,
    shouldColor?: boolean
): JSX.Element => {
    const chips: JSX.Element[] = [];

    if (rowData.names.length === 0) {
        return (
            <Grid item>
                <span>
                    {translation(
                        "message.metric-from-dataset-moved-along-all-dimensions-no-specific-root",
                        { metric: metric, dataset: dataset }
                    )}
                </span>
            </Grid>
        );
    }

    const color = getColorForDimensionCombo(
        generateFilterOptions(
            rowData.names,
            dimensionColumns,
            rowData.otherDimensionValues
        )
    );

    rowData.names.forEach((dimensionValue: string, idx: number) => {
        let displayValue = dimensionValue;

        // Values that are `(All)` indicate there are no filters for that column
        if (displayValue === SERVER_VALUE_ALL_VALUES) {
            return;
        }

        if (displayValue === "") {
            displayValue = EMPTY_STRING_DISPLAY;
        }

        if (shouldColor) {
            return chips.push(
                <Grid item>
                    <Chip
                        label={`${dimensionColumns[idx]}=${displayValue}`}
                        size="small"
                        style={{
                            color: color,
                            borderColor: color,
                        }}
                    />
                </Grid>
            );
        }

        return chips.push(
            <Grid item>
                <Chip
                    label={`${dimensionColumns[idx]}=${displayValue}`}
                    size="small"
                />
            </Grid>
        );
    });

    return (
        <Grid container spacing={1}>
            {chips}
        </Grid>
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
    comparisonOffset: string,
    translation: (labelName: string) => string = (s) => s,
    anomaly: Anomaly,
    timezone?: string,
    hideTime?: boolean
): TimeSeriesChartProps => {
    const dateFormatter = hideTime ? formatDateV1 : formatDateAndTimeV1;
    const filteredTimeSeriesData = extractDetectionEvaluation(filtered)[0].data;
    const nonFilteredTimeSeriesData =
        extractDetectionEvaluation(nonFiltered)[0].data;

    const series: Series[] = [
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
            name: "Filtered on dimension combination",
            data: filteredTimeSeriesData.current.map((value, idx) => {
                return {
                    y: value,
                    x: filteredTimeSeriesData.timestamp[idx],
                };
            }),
        },
    ];

    const granularityBestGuess = determineGranularity(
        nonFilteredTimeSeriesData.timestamp
    );
    const highlightPointsForAnomaly = generateAnomalyPointsForStartEnd(
        filteredTimeSeriesData.timestamp,
        filteredTimeSeriesData.current,
        anomaly.startTime,
        anomaly.endTime,
        anomaly.avgCurrentVal,
        granularityBestGuess
    );
    highlightPointsForAnomaly.forEach((d) => {
        d.extraData = {
            anomalyStart: anomaly.startTime,
            anomalyEnd: anomaly.endTime,
            baselineStart:
                anomaly.startTime -
                baselineOffsetToMilliseconds(comparisonOffset),
            baselineEnd:
                anomaly.endTime -
                baselineOffsetToMilliseconds(comparisonOffset),
        };
    });
    const highlightPointsForBaseline = generateAnomalyPointsForStartEnd(
        filteredTimeSeriesData.timestamp,
        filteredTimeSeriesData.current,
        anomaly.startTime - baselineOffsetToMilliseconds(comparisonOffset),
        anomaly.endTime - baselineOffsetToMilliseconds(comparisonOffset),
        anomaly.avgCurrentVal,
        granularityBestGuess
    );
    highlightPointsForBaseline.forEach((d) => {
        d.color = BASELINE_LINE_COLOR;
        d.extraData = {
            anomalyStart: anomaly.startTime,
            anomalyEnd: anomaly.endTime,
            baselineStart:
                anomaly.startTime -
                baselineOffsetToMilliseconds(comparisonOffset),
            baselineEnd:
                anomaly.endTime -
                baselineOffsetToMilliseconds(comparisonOffset),
        };
    });

    series.push({
        name: "Baseline Period and Anomaly Period",
        /**
         * We need the points in data, so it shows up in the legend
         * and in the filtered chart
         */
        data: flatten([highlightPointsForAnomaly, highlightPointsForBaseline]),
        strokeDasharray: Dimension.DASHARRAY_VISUALIZATION_ANOMALY,
        type: SeriesType.CUSTOM,
        color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
        legendIcon: (svgBound: number, color: string) => {
            return (
                <line
                    stroke={color}
                    strokeDasharray={Dimension.DASHARRAY_VISUALIZATION_ANOMALY}
                    strokeWidth={10}
                    x1={`${svgBound / 2}`}
                    x2={`${svgBound / 2}`}
                    y1="0"
                    y2={`${svgBound}`}
                />
            );
        },
        tooltip: {
            tooltipFormatter: (d: DataPoint) => {
                const dataPoint =
                    d as DataPoint<ExtraDataForAnomalyDimensionAnalysisData>;

                if (dataPoint.extraData) {
                    return (
                        <>
                            <tr>
                                <td align="center" colSpan={2}>
                                    <Box paddingBottom={1} paddingTop={2}>
                                        <strong>
                                            Baseline and Anomaly Start End
                                        </strong>
                                    </Box>
                                </td>
                            </tr>
                            <tr>
                                <td>Baseline Start</td>
                                <td align="right">
                                    {dateFormatter(
                                        dataPoint.extraData.baselineStart,
                                        timezone
                                    )}
                                </td>
                            </tr>
                            <tr>
                                <td>Baseline End</td>
                                <td align="right">
                                    {dateFormatter(
                                        dataPoint.extraData.baselineEnd,
                                        timezone
                                    )}
                                </td>
                            </tr>
                            <tr>
                                <td>Anomaly Start</td>
                                <td align="right">
                                    {dateFormatter(
                                        dataPoint.extraData.anomalyStart,
                                        timezone
                                    )}
                                </td>
                            </tr>
                            <tr>
                                <td>Anomaly End</td>
                                <td align="right">
                                    {dateFormatter(
                                        dataPoint.extraData.anomalyEnd,
                                        timezone
                                    )}
                                </td>
                            </tr>
                        </>
                    );
                }

                return <></>;
            },
        },
        customRenderer: (xScale, yScale) => {
            const [minYValue, maxYValue] = yScale.domain();

            const highlights: [DataPoint[], string, string][] = [
                [
                    highlightPointsForAnomaly,
                    Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                    "anomaly",
                ],
                [highlightPointsForBaseline, BASELINE_LINE_COLOR, "baseline"],
            ];

            return highlights.map(
                ([anomalySeriesData, attachingLineColor, id]) => {
                    return (
                        <Group key={id}>
                            {anomalySeriesData.length > 1 &&
                                anomalySeriesData.map((d, idx: number) => {
                                    if (idx + 1 >= anomalySeriesData.length) {
                                        return;
                                    }

                                    return (
                                        <Bar
                                            fill={
                                                d.color ||
                                                Palette.COLOR_VISUALIZATION_STROKE_ANOMALY
                                            }
                                            fillOpacity={0.1}
                                            height={
                                                (yScale(minYValue) || 0) -
                                                (yScale(maxYValue) || 0)
                                            }
                                            key={`${anomaly.id}-bar-${idx}`}
                                            width={
                                                (xScale(
                                                    d.x + granularityBestGuess
                                                ) || 0) - (xScale(d.x) || 0)
                                            }
                                            x={xScale(d.x)}
                                            y={yScale(maxYValue)}
                                        />
                                    );
                                })}
                            <LinePath<DataPoint>
                                data={anomalySeriesData}
                                stroke={attachingLineColor}
                                strokeWidth={2}
                                x={(d) => xScale(d.x) || 0}
                                y={(d) => yScale(d.y) || 0}
                            />
                            {anomalySeriesData.map((d) => (
                                <>
                                    <Line
                                        from={{
                                            x: xScale(d.x),
                                            y: yScale(minYValue),
                                        }}
                                        stroke={
                                            d.color ||
                                            Palette.COLOR_VISUALIZATION_STROKE_ANOMALY
                                        }
                                        strokeDasharray={`${Dimension.DASHARRAY_VISUALIZATION_ANOMALY}`}
                                        strokeWidth={
                                            Dimension.WIDTH_VISUALIZATION_STROKE_ANOMALY_LINE
                                        }
                                        to={{
                                            x: xScale(d.x),
                                            y: yScale(maxYValue),
                                        }}
                                    />
                                    <Circle
                                        cx={xScale(d.x)}
                                        cy={yScale(d.y)}
                                        fill={
                                            d.color ||
                                            Palette.COLOR_VISUALIZATION_STROKE_ANOMALY
                                        }
                                        r={
                                            Dimension.RADIUS_VISUALIZATION_ANOMALY_MARKER
                                        }
                                    />
                                </>
                            ))}
                        </Group>
                    );
                }
            );
        },
    });

    const chartOptions: TimeSeriesChartProps = {
        brush: true,
        height: 400,
        zoom: true,
        series,
        xAxis: {
            hideTime: hideTime,
        },
    };

    if (timezone && chartOptions.xAxis) {
        chartOptions.xAxis.timezone = timezone;
    }

    return chartOptions;
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
        } else if (
            dimensionValue !== SERVER_VALUE_ALL_VALUES &&
            dimensionValue !== SERVER_VALUE_FOR_NO_FILTER
        ) {
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
