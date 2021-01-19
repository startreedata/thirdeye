import { useTheme } from "@material-ui/core";
import { Circle } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { AlertEvaluationTimeSeriesAnomaliesPlotProps } from "./alert-evaluation-time-series-anomalies-plot.interfaces";

export const AlertEvaluationTimeSeriesAnomaliesPlot: FunctionComponent<AlertEvaluationTimeSeriesAnomaliesPlotProps> = (
    props: AlertEvaluationTimeSeriesAnomaliesPlotProps
) => {
    const theme = useTheme();

    return (
        // Anomalies plot
        <>
            {props.alertEvaluationAnomalyPoints &&
                props.alertEvaluationAnomalyPoints.map(
                    (alertEvaluationAnomalyPoint, index) => (
                        <Circle
                            cx={props.xScale(
                                alertEvaluationAnomalyPoint.startTime
                            )}
                            cy={props.yScale(
                                alertEvaluationAnomalyPoint.current
                            )}
                            fill={theme.palette.error.main}
                            key={index}
                            r={props.radius || 5}
                        />
                    )
                )}
        </>
    );
};
