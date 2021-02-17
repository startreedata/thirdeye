import { Bar } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Palette } from "../../../utils/material-ui/palette.util";
import { AnomaliesPlotProps } from "./anomalies-plot.interfaces";

export const AnomaliesPlot: FunctionComponent<AnomaliesPlotProps> = (
    props: AnomaliesPlotProps
) => {
    return (
        <>
            {props.anomalies &&
                props.anomalies.map((anomaly, index) => (
                    <Bar
                        fill={Palette.COLOR_VISUALIZATION_FILL_ANOMALY}
                        height={props.yScale.range()[0]}
                        key={index}
                        opacity={0.2}
                        width={
                            props.xScale(anomaly.endTime) -
                            props.xScale(anomaly.startTime)
                        }
                        x={props.xScale(anomaly.startTime)}
                        y={props.yScale.range()[1]}
                    />
                ))}
        </>
    );
};
