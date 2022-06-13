import { Anomaly } from "../../../../rest/dto/anomaly.interfaces";

export interface AlertEvaluationTimeSeriesTooltipProps {
    alertEvaluationTimeSeriesTooltipPoint?: AlertEvaluationTimeSeriesTooltipPoint;
}

export interface AlertEvaluationTimeSeriesTooltipPoint {
    timestamp: number;
    current: number;
    expected: number;
    upperBound: number;
    lowerBound: number;
    anomalies: Anomaly[];
}
