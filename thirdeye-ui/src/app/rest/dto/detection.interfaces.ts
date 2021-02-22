import { Anomaly } from "./anomaly.interfaces";

export interface DetectionEvaluation {
    mape: number;
    data: DetectionData;
    anomalies: Anomaly[];
}

export interface DetectionData {
    timestamp: number[];
    current: number[];
    expected: number[];
    upperBound: number[];
    lowerBound: number[];
}
