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
import { Box, Typography } from "@material-ui/core";
import { Orientation } from "@visx/axis";
import { Group } from "@visx/group";
import { Bar, Circle, Line, LinePath } from "@visx/shape";
import { flatten, isEqual } from "lodash";
import React from "react";
import { NavigateFunction } from "react-router";
import {
    formatDateAndTimeV1,
    formatLargeNumberV1,
} from "../../../platform/utils";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    DetectionData,
    DetectionEvaluation,
} from "../../../rest/dto/detection.interfaces";
import { extractDetectionEvaluation } from "../../../utils/alerts/alerts.util";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { getAnomaliesAnomalyPath } from "../../../utils/routes/routes.util";
import { determineGranularity } from "../../../utils/visualization/visualization.util";
import {
    DataPoint,
    NormalizedSeries,
    Series,
    SeriesType,
    ThresholdDataPoint,
    TimeSeriesChartProps,
    ZoomDomain,
} from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";

export const ZOOM_START_KEY = "zoomStart";
export const ZOOM_END_KEY = "zoomEnd";
const CURSOR_POINTER_STYLE = { cursor: "pointer" };

export const SMALL_CHART_SIZE = 500;
export const CHART_SIZE_OPTIONS = [
    ["S", SMALL_CHART_SIZE],
    ["M", 800],
    ["L", 1100],
];

export const generateSeriesDataForDetectionEvaluation = (
    detectionEvaluation: DetectionEvaluation,
    filteredAlertEvaluations: [AlertEvaluation, AnomalyFilterOption[]][],
    translation: (id: string) => string,
    hideUpperLowerBound?: boolean,
    hideActivity?: boolean,
    hidePredicted?: boolean
): Series[] => {
    const filteredTimeSeriesData: Series[] = [];

    filteredAlertEvaluations.forEach((alertEvalAndFilters) => {
        const [filteredAlertEvaluation, filterOptions] = alertEvalAndFilters;
        let filteredAlertEvaluationTimeSeriesData: DetectionData | undefined =
            undefined;

        if (detectionEvaluation.enumerationItem !== undefined) {
            const matchingDetectionEvaluationForEnumerationItem =
                extractDetectionEvaluation(filteredAlertEvaluation).find(
                    (candidate) => {
                        return isEqual(
                            candidate.enumerationItem,
                            detectionEvaluation.enumerationItem
                        );
                    }
                );

            if (matchingDetectionEvaluationForEnumerationItem) {
                filteredAlertEvaluationTimeSeriesData =
                    matchingDetectionEvaluationForEnumerationItem.data;
            }
        } else {
            filteredAlertEvaluationTimeSeriesData = extractDetectionEvaluation(
                filteredAlertEvaluation
            )[0].data;
        }

        if (filteredAlertEvaluationTimeSeriesData) {
            const timestamps = filteredAlertEvaluationTimeSeriesData.timestamp;

            filteredTimeSeriesData.push({
                name: filterOptions
                    .map((item) => concatKeyValueWithEqual(item, false))
                    .join(" & "),
                type: SeriesType.LINE,
                data: filteredAlertEvaluationTimeSeriesData.current
                    .map((value, idx) => {
                        return {
                            y: value,
                            x: timestamps[idx],
                        };
                    })
                    .filter((d) => Number.isFinite(d.y)),
                tooltip: {
                    valueFormatter: formatLargeNumberV1,
                },
                strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE,
            });
        }
    });

    const timeSeriesData = detectionEvaluation.data;

    const chartSeries: Series[] = [];

    if (!hideUpperLowerBound) {
        chartSeries.push({
            enabled: false,
            name: translation("label.upper-and-lower-bound"),
            type: SeriesType.AREA_CLOSED,
            fillOpacity: 0.1,
            color: Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
            data: timeSeriesData.lowerBound.map((value, idx) => {
                return {
                    y: value,
                    y1: timeSeriesData.upperBound[idx],
                    x: timeSeriesData.timestamp[idx],
                };
            }),
            stroke: Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
            strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT,
            tooltip: {
                pointFormatter: (d: DataPoint): string => {
                    const dThreshold = d as ThresholdDataPoint;

                    return `${formatLargeNumberV1(
                        dThreshold.y
                    )} - ${formatLargeNumberV1(dThreshold.y1)}`;
                },
            },
            legendIndex: 10,
        });
    }

    if (!hideActivity) {
        chartSeries.push({
            name: translation("label.activity"),
            type: SeriesType.LINE,
            color: Palette.COLOR_VISUALIZATION_STROKE_CURRENT,
            stroke: Palette.COLOR_VISUALIZATION_STROKE_CURRENT,
            strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_CURRENT,
            data: timeSeriesData.current
                .map((value, idx) => {
                    return {
                        y: value,
                        x: timeSeriesData.timestamp[idx],
                    };
                })
                .filter((d) => Number.isFinite(d.y)),
            tooltip: {
                pointFormatter: (d: DataPoint): string =>
                    formatLargeNumberV1(d.y),
            },
            legendIcon: (svgBound, color) => {
                return (
                    <line
                        stroke={color}
                        strokeWidth={10}
                        x1="0"
                        x2={`${svgBound}`}
                        y1={`${svgBound / 2}`}
                        y2={`${svgBound / 2}`}
                    />
                );
            },
        });
    }

    if (!hidePredicted) {
        chartSeries.push({
            name: translation("label.predicted"),
            type: SeriesType.LINE,
            color: Palette.COLOR_VISUALIZATION_STROKE_BASELINE,
            strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE,
            data: timeSeriesData.expected
                .map((value, idx) => {
                    return {
                        y: value,
                        x: timeSeriesData.timestamp[idx],
                    };
                })
                .filter((d) => Number.isFinite(d.y)),
            tooltip: {
                pointFormatter: (d: DataPoint): string =>
                    formatLargeNumberV1(d.y),
            },
            strokeDasharray: `${Dimension.DASHARRAY_VISUALIZATION_BASELINE}`,
        });
    }

    return [...chartSeries, ...filteredTimeSeriesData];
};

export const generateChartOptions = (
    detectionEvaluation: DetectionEvaluation,
    anomaly: Anomaly,
    filteredAlertEvaluation: [AlertEvaluation, AnomalyFilterOption[]][],
    translation: (id: string) => string,
    timezone?: string
): TimeSeriesChartProps => {
    let series: Series[] = [];

    if (detectionEvaluation) {
        const timeseriesData = detectionEvaluation.data;

        series = generateSeriesDataForDetectionEvaluation(
            detectionEvaluation,
            filteredAlertEvaluation,
            translation
        );
        series.push(
            generateSeriesForAnomalies(
                [anomaly],
                translation,
                timeseriesData.timestamp,
                timeseriesData.current,
                undefined,
                timezone
            )
        );
    }

    const chartOptions: TimeSeriesChartProps = {
        series,
        yAxis: {
            position: Orientation.right,
        },
        legend: true,
        brush: true,
        zoom: true,
        tooltip: true,
    };

    if (timezone) {
        chartOptions.xAxis = {
            timezone,
        };
    }

    return chartOptions;
};

export const generateSeriesForAnomalies = (
    anomalies: Anomaly[],
    translation: (id: string) => string,
    timestamps: number[],
    valuesToTrackAgainst: number[],
    navigate?: NavigateFunction,
    timezone?: string
): Series => {
    const granularityBestGuess = determineGranularity(timestamps);

    const pointerStyleOrNot = navigate ? CURSOR_POINTER_STYLE : {};
    const dataPointsByAnomalies: [Anomaly, DataPoint<Anomaly>[]][] =
        anomalies.map((anomaly) => {
            const anomalyRange =
                (anomaly.endTime - anomaly.startTime) / granularityBestGuess;
            const anomalySeriesData: DataPoint<Anomaly>[] = [];

            if (anomalyRange > 1) {
                let idx;
                for (idx = 0; idx < anomalyRange; idx++) {
                    const currentTimestamp =
                        anomaly.startTime + granularityBestGuess * idx;
                    const currentTimestampIdx = timestamps.findIndex(
                        (ts) => ts === currentTimestamp
                    );

                    if (currentTimestampIdx > -1) {
                        anomalySeriesData.push({
                            x: currentTimestamp,
                            y: valuesToTrackAgainst[currentTimestampIdx],
                            extraData: anomaly,
                        });
                    }
                }
            } else {
                anomalySeriesData.push({
                    x: anomaly.startTime,
                    y: anomaly.avgCurrentVal,
                    extraData: anomaly,
                });
            }

            return [anomaly, anomalySeriesData];
        });

    return {
        name: translation("label.anomalies"),
        /**
         * We need the points in data, so it shows up in the legend
         * and in the filtered chart
         */
        data: flatten(
            dataPointsByAnomalies.map(
                ([, anomalySeriesData]) => anomalySeriesData
            )
        ),
        strokeDasharray: Dimension.DASHARRAY_VISUALIZATION_ANOMALY,
        type: SeriesType.CUSTOM,
        color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
        legendIcon: (svgBound, color) => {
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
            tooltipFormatter: (d: DataPoint, series: NormalizedSeries) => {
                const dataPoint = d as DataPoint<Anomaly>;

                if (dataPoint.extraData) {
                    return (
                        <>
                            <tr>
                                <td align="center" colSpan={2}>
                                    <Box paddingBottom={1} paddingTop={2}>
                                        <strong>
                                            {translation("label.anomaly")} #
                                            {dataPoint.extraData.id}
                                        </strong>
                                    </Box>
                                </td>
                            </tr>
                            <tr>
                                <td>{translation("label.start")}</td>
                                <td align="right">
                                    {formatDateAndTimeV1(
                                        dataPoint.extraData.startTime,
                                        timezone
                                    )}
                                </td>
                            </tr>
                            <tr>
                                <td>{translation("label.end")}</td>
                                <td align="right">
                                    {formatDateAndTimeV1(
                                        dataPoint.extraData.endTime,
                                        timezone
                                    )}
                                </td>
                            </tr>
                            {navigate && (
                                <tr>
                                    <td align="center" colSpan={2}>
                                        <Box paddingTop={1}>
                                            <Typography variant="caption">
                                                {translation(
                                                    "message.click-to-view-anomaly"
                                                )}
                                            </Typography>
                                        </Box>
                                    </td>
                                </tr>
                            )}
                        </>
                    );
                }

                return (
                    <tr>
                        <td>{series.name}</td>
                        <td align="right">
                            {formatLargeNumberV1(dataPoint.y)}
                        </td>
                    </tr>
                );
            },
        },
        customRenderer: (xScale, yScale) => {
            const [minYValue, maxYValue] = yScale.domain();

            return dataPointsByAnomalies.map(([anomaly, anomalySeriesData]) => {
                return (
                    <Group
                        key={`${anomaly.id}`}
                        style={pointerStyleOrNot}
                        onClick={() => {
                            navigate &&
                                window.open(
                                    getAnomaliesAnomalyPath(anomaly.id),
                                    "_blank"
                                );
                        }}
                    >
                        {anomalySeriesData.length > 1 &&
                            anomalySeriesData.map((d, idx) => {
                                if (idx + 1 >= anomalySeriesData.length) {
                                    return;
                                }

                                return (
                                    <Bar
                                        fill={
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
                            stroke={Palette.COLOR_VISUALIZATION_STROKE_ANOMALY}
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
            });
        },
    };
};

export const generateChartOptionsForAlert = (
    detectionEvaluation: DetectionEvaluation,
    anomalies: Anomaly[],
    translation: (id: string) => string,
    navigate?: NavigateFunction,
    timezone?: string
): TimeSeriesChartProps => {
    let series: Series[] = [];

    if (detectionEvaluation !== null) {
        series = generateSeriesDataForDetectionEvaluation(
            detectionEvaluation,
            [],
            translation
        );
    }

    if (anomalies) {
        series.push(
            generateSeriesForAnomalies(
                anomalies,
                translation,
                detectionEvaluation.data.timestamp,
                detectionEvaluation.data.current,
                navigate,
                timezone
            )
        );
    }

    const chartOptions: TimeSeriesChartProps = {
        series,
        legend: true,
        brush: true,
        zoom: true,
        tooltip: true,
        yAxis: {
            position: Orientation.right,
        },
    };

    if (timezone) {
        chartOptions.xAxis = {
            timezone,
        };
    }

    return chartOptions;
};

export const generateChartOptionsForMetricsReport = (
    detectionEvaluation: DetectionEvaluation,
    anomalies: Anomaly[],
    translation: (id: string) => string,
    timezone?: string
): TimeSeriesChartProps => {
    let series: Series[] = [];

    if (detectionEvaluation !== null) {
        series = generateSeriesDataForDetectionEvaluation(
            detectionEvaluation,
            [],
            translation,
            true,
            false,
            true
        );
    }

    if (anomalies) {
        series.push(
            generateSeriesForAnomalies(
                anomalies,
                translation,
                detectionEvaluation.data.timestamp,
                detectionEvaluation.data.current
            )
        );
    }

    const chartOptions: TimeSeriesChartProps = {
        series,
        legend: false,
        brush: false,
        zoom: true,
        tooltip: false,
        yAxis: { enabled: false },
    };

    if (timezone) {
        chartOptions.xAxis = {
            timezone,
        };
    }

    return chartOptions;
};

export const determineInitialZoom = (
    searchParams: URLSearchParams
): ZoomDomain | undefined => {
    if (searchParams.has(ZOOM_START_KEY) && searchParams.has(ZOOM_END_KEY)) {
        return {
            x0: Number(searchParams.get(ZOOM_START_KEY)),
            x1: Number(searchParams.get(ZOOM_END_KEY)),
        };
    }

    return undefined;
};
