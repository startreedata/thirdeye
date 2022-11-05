/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Bar } from "@visx/shape";
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
