import { AreaClosed } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AlertEvaluationTimeSeriesUpperAndLowerBoundPlotProps } from "./alert-evaluation-time-series-upper-and-lower-bound-plot.interfaces";

export const AlertEvaluationTimeSeriesUpperAndLowerBoundPlot: FunctionComponent<AlertEvaluationTimeSeriesUpperAndLowerBoundPlotProps> = (
    props: AlertEvaluationTimeSeriesUpperAndLowerBoundPlotProps
) => {
    return (
        <AreaClosed
            data={props.alertEvaluationTimeSeriesPoints}
            defined={(alertEvaluationTimeSeriesPoint) =>
                // Area to be plot between upper and lower bound whenever both available or
                // between current and upper/lower bound (whichever available)
                (isFinite(alertEvaluationTimeSeriesPoint.upperBound) &&
                    isFinite(alertEvaluationTimeSeriesPoint.lowerBound)) ||
                (isFinite(alertEvaluationTimeSeriesPoint.current) &&
                    isFinite(alertEvaluationTimeSeriesPoint.upperBound)) ||
                (isFinite(alertEvaluationTimeSeriesPoint.current) &&
                    isFinite(alertEvaluationTimeSeriesPoint.lowerBound))
            }
            fill={Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND}
            fillOpacity={0.3}
            stroke={Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND}
            strokeOpacity={0.3}
            strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT}
            x={(alertEvaluationTimeSeriesPoint) =>
                props.xScale(alertEvaluationTimeSeriesPoint.timestamp)
            }
            y0={(alertEvaluationTimeSeriesPoint) =>
                // Lower bound or current
                props.yScale(
                    isFinite(alertEvaluationTimeSeriesPoint.lowerBound)
                        ? alertEvaluationTimeSeriesPoint.lowerBound
                        : alertEvaluationTimeSeriesPoint.current
                )
            }
            y1={(alertEvaluationTimeSeriesPoint) =>
                // Upper bound or current
                props.yScale(
                    isFinite(alertEvaluationTimeSeriesPoint.upperBound)
                        ? alertEvaluationTimeSeriesPoint.upperBound
                        : alertEvaluationTimeSeriesPoint.current
                )
            }
            yScale={props.yScale}
        />
    );
};
