import { Bar, Circle, Line } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { MouseHoverMarkerProps } from "./mouse-hover-marker.interfaces";

export const MouseHoverMarker: FunctionComponent<MouseHoverMarkerProps> = (
    props: MouseHoverMarkerProps
) => {
    return (
        <>
            {/* Mouse hover region  */}
            <Bar
                height={props.yScale.range()[0]}
                opacity={0}
                width={props.xScale.range()[1]}
                x={props.xScale.range()[0]}
                y={props.yScale.range()[1]}
                onMouseLeave={props.onMouseLeave}
                onMouseMove={props.onMouseMove}
            />

            {/* Mouse hover marker */}
            {props.alertEvaluationTimeSeriesTooltipPoint && (
                <>
                    <Line
                        from={{
                            x: props.xScale.range()[0],
                            y: props.yScale(
                                props.alertEvaluationTimeSeriesTooltipPoint
                                    .current
                            ),
                        }}
                        opacity={0.2}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER}
                        strokeDasharray={
                            Dimension.DASHARRAY_VISUALIZATION_HOVER_MARKER
                        }
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                        }
                        to={{
                            x: props.xScale(
                                props.alertEvaluationTimeSeriesTooltipPoint
                                    .timestamp
                            ),
                            y: props.yScale(
                                props.alertEvaluationTimeSeriesTooltipPoint
                                    .current
                            ),
                        }}
                    />

                    <Line
                        from={{
                            x: props.xScale(
                                props.alertEvaluationTimeSeriesTooltipPoint
                                    .timestamp
                            ),
                            y: props.yScale.range()[0],
                        }}
                        opacity={0.2}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER}
                        strokeDasharray={
                            Dimension.DASHARRAY_VISUALIZATION_HOVER_MARKER
                        }
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                        }
                        to={{
                            x: props.xScale(
                                props.alertEvaluationTimeSeriesTooltipPoint
                                    .timestamp
                            ),
                            y: props.yScale(
                                props.alertEvaluationTimeSeriesTooltipPoint
                                    .current
                            ),
                        }}
                    />

                    <Circle
                        cx={props.xScale(
                            props.alertEvaluationTimeSeriesTooltipPoint
                                .timestamp
                        )}
                        cy={props.yScale(
                            props.alertEvaluationTimeSeriesTooltipPoint.current
                        )}
                        fill={Palette.COLOR_VISUALIZATION_FILL_HOVER_MARKER}
                        opacity={0.7}
                        r={Dimension.RADIUS_VISUALIZATION_HOVER_MARKER}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER}
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                        }
                    />
                </>
            )}
        </>
    );
};
