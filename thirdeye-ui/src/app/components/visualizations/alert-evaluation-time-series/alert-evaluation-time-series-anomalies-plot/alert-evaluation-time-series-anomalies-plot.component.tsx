import { Bar } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AlertEvaluationTimeSeriesAnomaliesPlotProps } from "./alert-evaluation-time-series-anomalies-plot.interfaces";

export const AlertEvaluationTimeSeriesAnomaliesPlot: FunctionComponent<AlertEvaluationTimeSeriesAnomaliesPlotProps> = (
    props: AlertEvaluationTimeSeriesAnomaliesPlotProps
) => {
    return (
        <>
            {props.alertEvaluationAnomalyPoints &&
                props.alertEvaluationAnomalyPoints.map(
                    (alertEvaluationAnomalyPoint, index) => (
                        <Bar
                            fill={Palette.COLOR_VISUALIZATION_STROKE_ANOMALY}
                            fillOpacity={0.2}
                            height={
                                props.yScale(props.yScale.domain()[0]) -
                                props.yScale(props.yScale.domain()[1])
                            }
                            key={index}
                            stroke={Palette.COLOR_VISUALIZATION_STROKE_ANOMALY}
                            strokeOpacity={0.2}
                            strokeWidth={
                                Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                            }
                            width={
                                props.xScale(
                                    alertEvaluationAnomalyPoint.endTime
                                ) -
                                props.xScale(
                                    alertEvaluationAnomalyPoint.startTime
                                )
                            }
                            x={props.xScale(
                                alertEvaluationAnomalyPoint.startTime
                            )}
                            y={props.yScale(props.yScale.domain()[1])}
                        />
                    )
                )}
        </>
    );
};
