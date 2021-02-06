import { useTheme } from "@material-ui/core";
import { AreaClosed, curveNatural } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { AlertEvaluationTimeSeriesPoint } from "../alert-evaluation-time-series.interfaces";
import { AlertEvaluationTimeSeriesUpperAndLowerBoundPlotProps } from "./alert-evaluation-time-series-upper-and-lower-bound-plot.interfaces";

export const AlertEvaluationTimeSeriesUpperAndLowerBoundPlot: FunctionComponent<AlertEvaluationTimeSeriesUpperAndLowerBoundPlotProps> = (
    props: AlertEvaluationTimeSeriesUpperAndLowerBoundPlotProps
) => {
    const theme = useTheme();

    return (
        // Upper and lower bound plot
        <AreaClosed
            curve={curveNatural}
            data={props.alertEvaluationTimeSeriesPoints}
            defined={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): boolean => {
                return (
                    isFinite(alertEvaluationTimeSeriesPoint.lowerBound) &&
                    isFinite(alertEvaluationTimeSeriesPoint.upperBound)
                );
            }}
            fill={theme.palette.primary.main}
            stroke={theme.palette.primary.main}
            strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT}
            x={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.xScale(alertEvaluationTimeSeriesPoint.timestamp);
            }}
            y0={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.yScale(alertEvaluationTimeSeriesPoint.lowerBound);
            }}
            y1={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.yScale(alertEvaluationTimeSeriesPoint.upperBound);
            }}
            yScale={props.yScale}
        />
    );
};
