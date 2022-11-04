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
