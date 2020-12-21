import { curveNatural, LinePath } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../utils/material-ui-util/dimension-util";
import { Palette } from "../../../utils/material-ui-util/palette-util";
import { AlertEvaluationTimeSeriesPoint } from "../alert-evaluation-time-series.interfaces";
import { AlertEvaluationTimeSeriesCurrentPlotProps } from "./alert-evaluation-time-series-current-plot.interfaces";

export const AlertEvaluationTimeSeriesCurrentPlot: FunctionComponent<AlertEvaluationTimeSeriesCurrentPlotProps> = (
    props: AlertEvaluationTimeSeriesCurrentPlotProps
) => {
    return (
        // Current plot
        <LinePath
            curve={curveNatural}
            data={props.alertEvaluationTimeSeriesPoints}
            defined={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): boolean => {
                return isFinite(alertEvaluationTimeSeriesPoint.current);
            }}
            stroke={Palette.COLOR_VISUALIZATION_STROKE_DEFAULT}
            strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT}
            x={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.xScale(alertEvaluationTimeSeriesPoint.timestamp);
            }}
            y={(
                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
            ): number => {
                return props.yScale(alertEvaluationTimeSeriesPoint.current);
            }}
        />
    );
};
