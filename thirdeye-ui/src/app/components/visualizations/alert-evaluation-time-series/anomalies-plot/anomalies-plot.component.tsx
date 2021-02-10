import { Bar } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AnomaliesPlotProps } from "./anomalies-plot.interfaces";

export const AnomaliesPlot: FunctionComponent<AnomaliesPlotProps> = (
    props: AnomaliesPlotProps
) => {
    return (
        <>
            {props.alertEvaluationAnomalyPoints &&
                props.alertEvaluationAnomalyPoints.map(
                    (alertEvaluationAnomalyPoint, index) => (
                        <Bar
                            fill={Palette.COLOR_VISUALIZATION_STROKE_ANOMALY}
                            height={props.yScale.range()[0]}
                            key={index}
                            opacity={0.2}
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
                            y={props.yScale.range()[1]}
                        />
                    )
                )}
        </>
    );
};
