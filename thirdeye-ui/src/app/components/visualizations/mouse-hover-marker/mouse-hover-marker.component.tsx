import { Bar, Circle, Line } from "@visx/visx";
import React, { FunctionComponent, MouseEvent, TouchEvent } from "react";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { MouseHoverMarkerProps } from "./mouse-hover-marker.interfaces";

export const MouseHoverMarker: FunctionComponent<MouseHoverMarkerProps> = (
    props: MouseHoverMarkerProps
) => {
    const handleMouseLeave = (event: MouseEvent<SVGRectElement>): void => {
        if (props.zoom && props.zoom.isDragging) {
            props.zoom.dragEnd();
        } else {
            props.onMouseLeave(event);
        }
    };

    const handleMouseMove = (event: MouseEvent<SVGRectElement>): void => {
        if (props.zoom && props.zoom.isDragging) {
            props.zoom.dragMove(event);
            props.onZoomChange && props.onZoomChange(props.zoom);
        } else {
            props.onMouseMove(event);
        }
    };

    const handleTouchMove = (event: TouchEvent<SVGRectElement>): void => {
        if (props.zoom) {
            props.zoom.dragMove(event);
            props.onZoomChange && props.onZoomChange(props.zoom);
        }
    };

    const handleClick = (event: MouseEvent<SVGRectElement>): void => {
        if (props.onMouseClick) {
            props.onMouseClick(event);
        }
    };

    return (
        <>
            {/* Mouse hover region  */}
            <Bar
                cursor={props.cursor || "default"}
                height={props.yScale && props.yScale.range()[0]}
                opacity={0}
                width={props.xScale && props.xScale.range()[1]}
                x={props.xScale && props.xScale.range()[0]}
                y={props.yScale && props.yScale.range()[1]}
                onClick={handleClick}
                onMouseDown={props.zoom && props.zoom.dragStart}
                onMouseLeave={handleMouseLeave}
                onMouseMove={handleMouseMove}
                onMouseUp={props.zoom && props.zoom.dragEnd}
                onTouchEnd={props.zoom && props.zoom.dragEnd}
                onTouchMove={handleTouchMove}
                onTouchStart={props.zoom && props.zoom.dragStart}
            />

            {/* Mouse hover marker */}
            {props.x && props.y && isFinite(props.y) && (
                <>
                    <Line
                        from={{
                            x: props.xScale && props.xScale.range()[0],
                            y: props.yScale && props.yScale(props.y),
                        }}
                        opacity={0.5}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER}
                        strokeDasharray={
                            Dimension.DASHARRAY_VISUALIZATION_HOVER_MARKER
                        }
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                        }
                        to={{
                            x: props.xScale && props.xScale(props.x),
                            y: props.yScale && props.yScale(props.y),
                        }}
                    />

                    <Line
                        from={{
                            x: props.xScale && props.xScale(props.x),
                            y: props.yScale && props.yScale.range()[0],
                        }}
                        opacity={0.5}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER}
                        strokeDasharray={
                            Dimension.DASHARRAY_VISUALIZATION_HOVER_MARKER
                        }
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                        }
                        to={{
                            x: props.xScale && props.xScale(props.x),
                            y: props.yScale && props.yScale(props.y),
                        }}
                    />

                    <Circle
                        cx={props.xScale && props.xScale(props.x)}
                        cy={props.yScale && props.yScale(props.y)}
                        fill={Palette.COLOR_VISUALIZATION_FILL_HOVER_MARKER}
                        r={Dimension.RADIUS_VISUALIZATION_HOVER_MARKER}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER}
                        strokeOpacity={0.5}
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                        }
                    />
                </>
            )}
        </>
    );
};
