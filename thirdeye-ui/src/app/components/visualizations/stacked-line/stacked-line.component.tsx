import { Circle, Line } from "@visx/shape";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { ThresholdDataPoint } from "../time-series-chart/time-series-chart.interfaces";
import { StackedLineProps } from "./stacked-line.interfaces";

const DEFAULT_GAP_BETWEEN_LINES = 10;

export const StackedLine: FunctionComponent<StackedLineProps> = ({
    points,
    x,
    y,
    x1,
    y1,
    gapBetweenLines = DEFAULT_GAP_BETWEEN_LINES,
    stroke,
    strokeWidth,
}: StackedLineProps) => {
    return (
        <>
            {points &&
                points.map((event, index) => {
                    const from = {
                        x: x(event),
                        y: y({
                            ...event,
                            y: (index + 1) * gapBetweenLines,
                        }),
                    };
                    const to = {
                        x: x1(event as ThresholdDataPoint),
                        y: y1({
                            ...(event as ThresholdDataPoint),
                            y1: (index + 1) * gapBetweenLines,
                        }),
                    };

                    return (
                        <React.Fragment key={`stacked-line-${index}`}>
                            <Circle
                                cx={x(event as ThresholdDataPoint)}
                                cy={from.y}
                                fill={stroke}
                                r={strokeWidth}
                            />
                            <Circle
                                cx={x1(event as ThresholdDataPoint)}
                                cy={to.y}
                                fill={stroke}
                                r={strokeWidth}
                            />
                            <Line
                                from={from}
                                key={index}
                                stroke={stroke}
                                strokeWidth={
                                    Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE
                                }
                                to={to}
                            />
                        </React.Fragment>
                    );
                })}
        </>
    );
};
