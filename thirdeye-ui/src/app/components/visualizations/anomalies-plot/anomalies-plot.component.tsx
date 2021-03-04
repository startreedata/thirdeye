import { Bar } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../utils/material-ui/dimension.util";
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
                        fill={Palette.COLOR_VISUALIZATION_STROKE_ANOMALY}
                        fillOpacity={0.2}
                        height={props.yScale && props.yScale.range()[0]}
                        key={index}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_ANOMALY}
                        strokeOpacity={0.2}
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                        }
                        width={
                            (props.xScale && props.xScale(anomaly.endTime)) -
                            (props.xScale && props.xScale(anomaly.startTime))
                        }
                        x={props.xScale && props.xScale(anomaly.startTime)}
                        y={props.yScale && props.yScale.range()[1]}
                    />
                ))}
        </>
    );
};
