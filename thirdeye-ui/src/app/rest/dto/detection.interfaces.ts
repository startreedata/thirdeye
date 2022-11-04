import { Anomaly } from "./anomaly.interfaces";

export interface DetectionEvaluation {
    data: DetectionData;
    anomalies: Anomaly[];
    enumerationItem?: EnumerationItemInEvaluation;
}

export interface DetectionData {
    timestamp: number[];
    current: number[];
    expected: number[];
    upperBound: number[];
    lowerBound: number[];
}

export interface EnumerationItemParams {
    [key: string]: number | string;
}

export interface EnumerationItemInEvaluation {
    name: string;
    params: EnumerationItemParams;
}
