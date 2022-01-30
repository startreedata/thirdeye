import { ProvidedZoom, TransformMatrix } from "@visx/zoom/lib/types";
import { ScaleLinear, ScaleTime } from "d3-scale";
import { MouseEvent, ReactNode } from "react";

// Zoom props
export interface ZoomProps {
    svgHeight: number;
    svgWidth: number;
    zoomHeight: number;
    zoomWidth: number;
    xAxisOnly?: boolean;
    yAxisOnly?: boolean;
    initialTransform: TransformMatrix;
    onChange?: (zoom?: VisxZoomProps) => void;
    children: (zoom: VisxZoomProps) => ReactNode;
}

export interface ZoomState {
    initialTransformMatrix: TransformMatrix;
    transformMatrix: TransformMatrix;
    isDragging: boolean;
}

export type VisxZoomProps = ProvidedZoom & ZoomState;

export interface MouseHoverMarkerProps {
    x?: number;
    y?: number;
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
    zoom?: VisxZoomProps;
    onMouseMove: (event: MouseEvent<SVGRectElement>) => void;
    onMouseLeave: (event: MouseEvent<SVGRectElement>) => void;
    onZoomChange?: (zoom: VisxZoomProps) => void;
}
