import { ProvidedZoom, TransformMatrix } from "@visx/zoom/lib/types";
import { ReactNode } from "react";

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

export type VisxZoomProps = ProvidedZoom & ZoomState;

export interface ZoomState {
    initialTransformMatrix: TransformMatrix;
    transformMatrix: TransformMatrix;
    isDragging: boolean;
}
