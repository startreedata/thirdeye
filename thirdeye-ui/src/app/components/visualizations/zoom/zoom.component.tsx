import { Backdrop, Typography } from "@material-ui/core";
import { Zoom as VisxZoom } from "@visx/visx";
import { TransformMatrix } from "@visx/zoom/lib/types";
import React, { FunctionComponent, useEffect, useRef, useState } from "react";
import { VisxZoomProps, ZoomProps } from "./zoom.interfaces";
import { useZoomStyles } from "./zoom.styles";

export const Zoom: FunctionComponent<ZoomProps> = (props: ZoomProps) => {
    const zoomClasses = useZoomStyles();

    const rectRef = useRef<SVGSVGElement>(null);
    const zoomRef = useRef<VisxZoomProps>();
    const clearTimer = useRef<NodeJS.Timeout>();
    const [controlInfoVisible, setControlInfoVisible] = useState(false);

    const handleWheel = (
        event: React.WheelEvent<SVGRectElement> | WheelEvent
    ): void => {
        if (event.ctrlKey) {
            event.preventDefault();
            zoomRef.current?.handleWheel(event);
            props.onChange && props.onChange(zoomRef.current);
            setControlInfoVisible(false);
        } else {
            showControlInfo();
        }
    };

    // Show control info
    // Hide it after 3s
    const showControlInfo = (): void => {
        if (clearTimer.current) {
            clearTimeout(clearTimer.current);
        }
        setControlInfoVisible(true);

        clearTimer.current = setTimeout(() => {
            setControlInfoVisible(false);
        }, 3000);
    };

    useEffect(() => {
        // Attach wheel event listener to perform zoom action
        rectRef.current?.addEventListener("wheel", handleWheel, {
            passive: false,
        });

        // Remove wheen event listener on unmount
        return () => rectRef.current?.removeEventListener("wheel", handleWheel);
    }, [rectRef.current]);

    // Restric zoom based on props
    const handleZoomEvent = (
        transformMatrix: TransformMatrix
    ): TransformMatrix => {
        let newTransformMaterix = {
            ...transformMatrix,
        };

        // Restric zoom to initial xScale if xAxisOnly flag enables
        if (props.xAxisOnly) {
            newTransformMaterix = {
                ...newTransformMaterix,
                scaleY: props.initialTransform.scaleY,
                translateY: props.initialTransform.translateY,
            };

            if (newTransformMaterix.scaleX < 1) {
                newTransformMaterix.scaleX = props.initialTransform.scaleX;
                newTransformMaterix.translateX =
                    props.initialTransform.translateX;
            }

            if (newTransformMaterix.scaleX === 1) {
                newTransformMaterix.translateX =
                    props.initialTransform.translateX;
            }
        }

        // Restric zoom to initial yScale if yAxisOnly flag enables
        if (props.yAxisOnly) {
            newTransformMaterix = {
                ...newTransformMaterix,
                scaleX: props.initialTransform.scaleX,
                translateX: props.initialTransform.translateX,
            };

            if (newTransformMaterix.scaleY < 1) {
                newTransformMaterix.scaleY = props.initialTransform.scaleY;
                newTransformMaterix.translateY =
                    props.initialTransform.translateY;
            }

            if (newTransformMaterix.scaleY === 1) {
                newTransformMaterix.translateY =
                    props.initialTransform.translateY;
            }
        }

        return newTransformMaterix;
    };

    return (
        <VisxZoom
            passive
            constrain={handleZoomEvent}
            height={props.svgHeight}
            transformMatrix={props.initialTransform}
            width={props.svgWidth}
        >
            {(zoom) => {
                zoomRef.current = zoom;

                return (
                    <div className={zoomClasses.container}>
                        <svg
                            height={props.svgHeight}
                            ref={rectRef}
                            style={{
                                cursor: zoom.isDragging ? "grabbing" : "grab",
                            }}
                            width={props.svgWidth}
                        >
                            {/* Renders chilldren within zoom container */}
                            {props.children(zoom)}
                        </svg>
                        <Backdrop
                            open={controlInfoVisible}
                            style={{ zIndex: 1 }}
                            onWheel={(event) => {
                                if (event.ctrlKey) {
                                    setControlInfoVisible(false);
                                }
                            }}
                        >
                            <Typography
                                className={zoomClasses.controlInfo}
                                variant="h4"
                            >
                                Use Control + scroll to zoom the chart
                            </Typography>
                        </Backdrop>
                    </div>
                );
            }}
        </VisxZoom>
    );
};
