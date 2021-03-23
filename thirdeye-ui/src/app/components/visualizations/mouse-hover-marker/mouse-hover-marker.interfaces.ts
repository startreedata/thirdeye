import { ScaleLinear, ScaleTime } from "d3-scale";
import { MouseEvent } from "react";
import { VisxZoomProps } from "../zoom/zoom.interfaces";

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
