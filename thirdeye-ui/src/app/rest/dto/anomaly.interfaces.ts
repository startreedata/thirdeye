import { Alert } from "./alert.interfaces";
import { Metric } from "./metric.interfaces";

export interface Anomaly {
    id: number;
    startTime: number;
    endTime: number;
    avgCurrentVal: number;
    avgBaselineVal: number;
    score: number;
    weight: number;
    impactToGlobal: number;
    sourceType: AnomalyResultSource;
    created: number;
    notified: boolean;
    message: string;
    alert: Alert;
    metric: Metric;
    children: Anomaly[];
    type: AnomalyType;
    severity: AnomalySeverity;
    child: boolean;
}

export enum AnomalyType {
    DEVIATION,
    TREND_CHANGE,
    DATA_SLA,
}

export enum AnomalySeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    DEFAULT,
}

export enum AnomalyResultSource {
    DEFAULT_ANOMALY_DETECTION,
    DATA_QUALITY_DETECTION,
    ANOMALY_REPLAY,
    USER_LABELED_ANOMALY,
}
