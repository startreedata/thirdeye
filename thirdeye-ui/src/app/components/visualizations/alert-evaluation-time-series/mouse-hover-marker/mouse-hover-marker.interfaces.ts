import { ScaleLinear, ScaleTime } from "d3-scale";
import { MouseEvent } from "react";
import { AlertEvaluationTimeSeriesTooltipPoint } from "../alert-evaluation-time-series.interfaces";

export interface MouseHoverMarkerProps {
    alertEvaluationTimeSeriesTooltipPoint?: AlertEvaluationTimeSeriesTooltipPoint;
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
    onMouseMove?: (event: MouseEvent<SVGRectElement>) => void;
    onMouseLeave?: (event: MouseEvent<SVGRectElement>) => void;
}
