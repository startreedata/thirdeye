import { ReactNode } from "react";
import { AlertEvaluationTimeSeriesTooltipPoint } from "../alert-evaluation-time-series.interfaces";

export interface TooltipProps {
    tooltipTop?: number;
    tooltipLeft?: number;
    alertEvaluationTimeSeriesTooltipPoint?: AlertEvaluationTimeSeriesTooltipPoint;
    children: ReactNode;
}
