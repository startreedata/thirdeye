// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Line } from "@visx/shape";
import React, { FunctionComponent } from "react";
import {
    DataPoint,
    NormalizedSeries,
    SeriesType,
    ThresholdDataPoint,
} from "../time-series-chart.interfaces";
import { TooltipMarkersProps } from "./tooltip.interfaces";
import {
    getDataPointsInSeriesForXValue,
    TOOLTIP_LINE_COLOR,
} from "./tooltip.utils";

export const TooltipMarkers: FunctionComponent<TooltipMarkersProps> = ({
    xScale,
    yScale,
    chartHeight,
    series,
    xValue,
    colorScale,
}) => {
    const dataPointForXValue: [DataPoint, NormalizedSeries][] =
        getDataPointsInSeriesForXValue(series, xValue);

    return (
        <g>
            {/* Vertical Line */}
            <Line
                from={{ x: xScale(xValue), y: 0 }}
                pointerEvents="none"
                stroke={TOOLTIP_LINE_COLOR}
                strokeDasharray="5,2"
                strokeWidth={2}
                to={{
                    x: xScale(xValue),
                    y: chartHeight,
                }}
            />

            {dataPointForXValue.map(([dataPoint, seriesData]) => {
                const color =
                    seriesData.color === undefined
                        ? colorScale(seriesData.name as string)
                        : seriesData.color;

                return (
                    <>
                        <circle
                            cx={xScale(xValue)}
                            cy={(yScale(dataPoint.y) as number) + 1}
                            fill="black"
                            fillOpacity={0.1}
                            pointerEvents="none"
                            r={4}
                            stroke="black"
                            strokeOpacity={0.1}
                            strokeWidth={2}
                        />
                        <circle
                            cx={xScale(xValue)}
                            cy={yScale(dataPoint.y)}
                            fill={color}
                            pointerEvents="none"
                            r={4}
                            stroke="white"
                            strokeWidth={2}
                        />
                        {seriesData.type === SeriesType.AREA_CLOSED && (
                            <>
                                <circle
                                    cx={xScale(xValue)}
                                    cy={
                                        (yScale(
                                            (dataPoint as ThresholdDataPoint).y1
                                        ) as number) + 1
                                    }
                                    fill="black"
                                    fillOpacity={0.1}
                                    pointerEvents="none"
                                    r={4}
                                    stroke="black"
                                    strokeOpacity={0.1}
                                    strokeWidth={2}
                                />
                                <circle
                                    cx={xScale(xValue)}
                                    cy={yScale(
                                        (dataPoint as ThresholdDataPoint).y1
                                    )}
                                    fill={color}
                                    pointerEvents="none"
                                    r={4}
                                    stroke="white"
                                    strokeWidth={2}
                                />
                            </>
                        )}
                    </>
                );
            })}
        </g>
    );
};
