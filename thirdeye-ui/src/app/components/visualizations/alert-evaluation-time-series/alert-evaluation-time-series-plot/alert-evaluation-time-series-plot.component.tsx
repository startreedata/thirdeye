/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AreaClosed, LinePath } from "@visx/shape";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AnomaliesPlot } from "../../anomalies-plot/anomalies-plot.component";
import { AlertEvaluationTimeSeriesPlotProps } from "./alert-evaluation-time-series-plot.interfaces";

export const AlertEvaluationTimeSeriesPlot: FunctionComponent<
    AlertEvaluationTimeSeriesPlotProps
> = (props: AlertEvaluationTimeSeriesPlotProps) => {
    return (
        <>
            {/* Anomalies */}
            {props.anomalies && props.alertEvaluationAnomalies && (
                <AnomaliesPlot
                    anomalies={props.alertEvaluationAnomalies}
                    xScale={props.xScale}
                    yScale={props.yScale}
                />
            )}

            {/* Upper and lower bound */}
            {props.upperAndLowerBound && (
                <AreaClosed
                    data={props.alertEvaluationTimeSeriesPoints}
                    defined={(alertEvaluationTimeSeriesPoint) => {
                        // Area to be plot between upper and lower bound whenever both available or
                        // between current and upper/lower bound (whichever available)
                        if (
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.upperBound
                            ) &&
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.lowerBound
                            )
                        ) {
                            // Upper and lower bound both available
                            return true;
                        }

                        if (
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.current
                            ) &&
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.upperBound
                            )
                        ) {
                            // Current and upper bound available
                            return true;
                        }

                        if (
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.current
                            ) &&
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.lowerBound
                            )
                        ) {
                            // Current and lower bound available
                            return true;
                        }

                        return false;
                    }}
                    fill={
                        Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND
                    }
                    fillOpacity={0.6}
                    stroke={
                        Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND
                    }
                    strokeOpacity={0.6}
                    strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT}
                    x={(alertEvaluationTimeSeriesPoint) =>
                        props.xScale &&
                        props.xScale(alertEvaluationTimeSeriesPoint.timestamp)
                    }
                    y0={(alertEvaluationTimeSeriesPoint) =>
                        // Lower bound or current
                        props.yScale &&
                        props.yScale(
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.lowerBound
                            )
                                ? alertEvaluationTimeSeriesPoint.lowerBound
                                : alertEvaluationTimeSeriesPoint.current
                        )
                    }
                    y1={(alertEvaluationTimeSeriesPoint) =>
                        // Upper bound or current
                        props.yScale &&
                        props.yScale(
                            Number.isFinite(
                                alertEvaluationTimeSeriesPoint.upperBound
                            )
                                ? alertEvaluationTimeSeriesPoint.upperBound
                                : alertEvaluationTimeSeriesPoint.current
                        )
                    }
                    yScale={props.yScale}
                />
            )}

            {/* Baseline */}
            {props.baseline && (
                <LinePath
                    data={props.alertEvaluationTimeSeriesPoints}
                    defined={(alertEvaluationTimeSeriesPoint) =>
                        Number.isFinite(alertEvaluationTimeSeriesPoint.expected)
                    }
                    stroke={Palette.COLOR_VISUALIZATION_STROKE_BASELINE}
                    strokeDasharray={Dimension.DASHARRAY_VISUALIZATION_BASELINE}
                    strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE}
                    x={(alertEvaluationTimeSeriesPoint) =>
                        props.xScale &&
                        props.xScale(alertEvaluationTimeSeriesPoint.timestamp)
                    }
                    y={(alertEvaluationTimeSeriesPoint) =>
                        props.yScale &&
                        props.yScale(alertEvaluationTimeSeriesPoint.expected)
                    }
                />
            )}

            {/* Current */}
            {props.current && (
                <LinePath
                    data={props.alertEvaluationTimeSeriesPoints}
                    defined={(alertEvaluationTimeSeriesPoint) =>
                        Number.isFinite(alertEvaluationTimeSeriesPoint.current)
                    }
                    stroke={Palette.COLOR_VISUALIZATION_STROKE_CURRENT}
                    strokeWidth={Dimension.WIDTH_VISUALIZATION_STROKE_CURRENT}
                    x={(alertEvaluationTimeSeriesPoint) =>
                        props.xScale &&
                        props.xScale(alertEvaluationTimeSeriesPoint.timestamp)
                    }
                    y={(alertEvaluationTimeSeriesPoint) =>
                        props.yScale &&
                        props.yScale(alertEvaluationTimeSeriesPoint.current)
                    }
                />
            )}
        </>
    );
};
