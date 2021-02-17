import { ScaleLinear, ScaleTime } from "d3-scale";
import { MouseEvent } from "react";

export interface MouseHoverMarkerProps {
    x?: number;
    y?: number;
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
    onMouseMove?: (event: MouseEvent<SVGRectElement>) => void;
    onMouseLeave?: (event: MouseEvent<SVGRectElement>) => void;
}
