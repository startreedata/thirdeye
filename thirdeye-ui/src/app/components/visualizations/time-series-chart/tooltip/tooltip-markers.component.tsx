import { Line } from "@visx/shape";
import React, { FunctionComponent } from "react";
import { DataPoint, Series } from "../time-series-chart.interfaces";
import { TooltipMarkersProps } from "./tooltip.interfaces";
import { getDataPointsInSeriesForXValue } from "./tooltip.utils";

const LINE_COLOR = "#AAAAAA";

export const TooltipMarkers: FunctionComponent<TooltipMarkersProps> = ({
    xScale,
    yScale,
    chartHeight,
    series,
    xValue,
    colorScale,
}) => {
    const dataPointForXValue: [DataPoint, Series][] =
        getDataPointsInSeriesForXValue(series, xValue);

    return (
        <g>
            {/* Vertical Line */}
            <Line
                from={{ x: xScale(xValue), y: 0 }}
                pointerEvents="none"
                stroke={LINE_COLOR}
                strokeDasharray="5,2"
                strokeWidth={2}
                to={{
                    x: xScale(xValue),
                    y: chartHeight,
                }}
            />

            {dataPointForXValue.map(([dataPoint, seriesData]) => {
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
                            fill={colorScale(seriesData.name as string)}
                            pointerEvents="none"
                            r={4}
                            stroke="white"
                            strokeWidth={2}
                        />
                    </>
                );
            })}
        </g>
    );
};
