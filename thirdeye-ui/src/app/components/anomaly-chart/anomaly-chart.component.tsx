import LinePath from "@visx/shape/lib/shapes/LinePath";
import React, { FunctionComponent } from "react";
import { getDate, getValue } from "../../utils/chart/chart-util";
import {
    AnomalyChartProps,
    TimeSeriesAnomaly,
} from "./anomaly-chart.interface";

const STROKE = "red";
const STROKE_WIDTH = 2;
const STROKE_RADIOUS = 5;

export const AnomalyChart: FunctionComponent<AnomalyChartProps> = ({
    anomalies,
    xScale,
    yScale,
    dotRadius,
    stroke,
    strokeWidth,
}: AnomalyChartProps) => {
    return (
        <>
            {anomalies.map((anomaly) => {
                const lineData: TimeSeriesAnomaly[] = [
                    {
                        timestamp: new Date(anomaly.startTime),
                        current: anomaly.avgCurrentVal,
                    },
                    {
                        timestamp: new Date(anomaly.endTime),
                        current: anomaly.avgCurrentVal,
                    },
                ];

                return (
                    <>
                        {/* Plot dots when anomlies occurs */}
                        {lineData.map((dot, idx) => (
                            <circle
                                cx={xScale(getDate(dot))}
                                cy={yScale(getValue(dot))}
                                fill={stroke ?? STROKE}
                                key={idx}
                                r={dotRadius ?? STROKE_RADIOUS}
                            />
                        ))}
                        {/* Plot line between dots */}
                        <LinePath<TimeSeriesAnomaly>
                            data={lineData}
                            key={anomaly.id}
                            stroke={stroke ?? STROKE}
                            strokeWidth={strokeWidth ?? STROKE_WIDTH}
                            x={(d): number => xScale(getDate(d)) ?? 0}
                            y={(d): number => yScale(getValue(d)) ?? 0}
                        />
                    </>
                );
            })}
        </>
    );
};
