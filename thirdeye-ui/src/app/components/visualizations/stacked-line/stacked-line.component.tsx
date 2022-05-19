import { Circle, Line } from "@visx/shape";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import {
    DataPoint,
    ThresholdDataPoint,
} from "../time-series-chart/time-series-chart.interfaces";
import { StackedLineProps } from "./stacked-line.interfaces";

const DEFAULT_GAP_BETWEEN_LINES = 10;

export const StackedLine: FunctionComponent<StackedLineProps> = ({
    xScale,
    yScale,
    gapBetweenLines = DEFAULT_GAP_BETWEEN_LINES,
    stroke,
    strokeWidth,
    series,
}: StackedLineProps) => {
    const {
        xAccessor,
        x1Accessor,
        yAccessor,
        y1Accessor,
        data: points,
    } = series;

    const getXValue = (d: DataPoint): number => {
        return xScale(xAccessor(d)) ?? 0;
    };

    const getX1Value = (d: DataPoint): number => {
        return xScale(x1Accessor(d as ThresholdDataPoint)) ?? 0;
    };

    const getYValue = (d: DataPoint): number => {
        return yScale(yAccessor(d)) ?? 0;
    };

    const getY1Value = (d: ThresholdDataPoint): number => {
        return yScale(y1Accessor(d as ThresholdDataPoint)) ?? 0;
    };

    return (
        <>
            {points &&
                points.map((event, index) => {
                    const from = {
                        x: getXValue(event),
                        y: getYValue({
                            ...event,
                            y: (index + 1) * gapBetweenLines,
                        }),
                    };
                    const to = {
                        x: getX1Value(event as ThresholdDataPoint),
                        y: getY1Value({
                            ...(event as ThresholdDataPoint),
                            y1: (index + 1) * gapBetweenLines,
                        }),
                    };

                    return (
                        <React.Fragment key={`stacked-line-${index}`}>
                            <Circle
                                cx={getXValue(event as ThresholdDataPoint)}
                                cy={from.y}
                                fill={stroke}
                                r={strokeWidth}
                            />
                            <Circle
                                cx={getX1Value(event as ThresholdDataPoint)}
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
