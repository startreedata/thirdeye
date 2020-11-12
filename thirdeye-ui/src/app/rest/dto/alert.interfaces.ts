import { DetectionEvaluation } from "./detection.interfaces";
import { Metric } from "./metric.interfaces";
import { User } from "./user.interfaces";

export interface Alert {
    id: number;
    name: string;
    description: string;
    cron: string;
    lastTimestamp: Date;
    active: boolean;
    created: Date;
    updated: Date;
    owner: User;
    detections: { [index: string]: AlertComponent };
    filters: { [index: string]: AlertComponent };
    qualityChecks: { [index: string]: AlertComponent };
}

export interface AlertComponent {
    name: string;
    type: string;
    metric: Metric;
    params: { [index: string]: unknown };
    alert: Alert;
}

export interface AlertEvaluation {
    alert: Alert;
    detectionEvaluations: { [index: string]: DetectionEvaluation };
    start: Date;
    end: Date;
    lastTimestamp: Date;
}
