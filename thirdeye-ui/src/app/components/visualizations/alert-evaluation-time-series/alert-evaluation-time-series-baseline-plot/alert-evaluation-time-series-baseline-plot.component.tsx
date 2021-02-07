import { LinePath } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AlertEvaluationTimeSeriesBaselinePlotProps } from "./alert-evaluation-time-series-baseline-plot.interfaces";

export const AlertEvaluationTimeSeriesBaselinePlot: FunctionComponent<AlertEvaluationTimeSeriesBaselinePlotProps> = (
    props: AlertEvaluationTimeSeriesBaselinePlotProps
) => {
    return (
        <LinePath
            data={props.alertEvaluationTimeSeriesPoints}
            defined={(alertEvaluationTimeSeriesPoint) =>
                isFinite(alertEvaluationTimeSeriesPoint.expected)
            }
            stroke={Palette.COLOR_VISUALIZATION_STROKE_BASELINE}
            strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE}
            x={(alertEvaluationTimeSeriesPoint) =>
                props.xScale(alertEvaluationTimeSeriesPoint.timestamp)
            }
            y={(alertEvaluationTimeSeriesPoint) =>
                props.yScale(alertEvaluationTimeSeriesPoint.expected)
            }
        />
    );
};
