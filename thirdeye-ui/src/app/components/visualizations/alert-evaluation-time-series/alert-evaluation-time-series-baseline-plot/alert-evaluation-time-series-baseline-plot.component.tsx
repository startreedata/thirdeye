import { curveNatural, LinePath } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui-util/dimension-util";
import { Palette } from "../../../../utils/material-ui-util/palette-util";
import { AlertEvaluationTimeSeriesPoint } from "../alert-evaluation-time-series.interfaces";
import { AlertEvaluationTimeSeriesBaselinePlotProps } from "./alert-evaluation-time-series-baseline-plot.interfaces";

export const AlertEvaluationTimeSeriesBaselinePlot: FunctionComponent<AlertEvaluationTimeSeriesBaselinePlotProps> = (
    props: AlertEvaluationTimeSeriesBaselinePlotProps
) => {
    return (
        // Baseline plot
        <LinePath
            curve={curveNatural}
            data={props.alertEvaluationTimeSeriesPoints}
            defined={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): boolean => {
                return isFinite(alertEvaluationTimeSeriesPoint.expected);
            }}
            stroke={Palette.COLOR_VISUALIZATION_STROKE_BASELINE}
            strokeDasharray={Dimension.WIDTH_VISUALIZATION_STROKE_DASHARRAY}
            strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT}
            x={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.xScale(alertEvaluationTimeSeriesPoint.timestamp);
            }}
            y={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.yScale(alertEvaluationTimeSeriesPoint.expected);
            }}
        />
    );
};
