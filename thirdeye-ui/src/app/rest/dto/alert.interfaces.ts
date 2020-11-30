import { DetectionEvaluation } from "./detection.interfaces";
import { Metric } from "./metric.interfaces";
import { User } from "./user.interfaces";

export interface Alert {
    id: number;
    name: string;
    description: string;
    cron: string;
    lastTimestamp: number;
    active: boolean;
    created: number;
    updated: number;
    owner: User;
    nodes: { [index: string]: AlertNode };
}

export interface AlertNode {
    name: string;
    type: AlertNodeType;
    subType: string;
    metric: Metric;
    params: { [index: string]: unknown };
    dependsOn: string[];
}

export enum AlertNodeType {
    DETECTION = "DETECTION",
    FILTER = "FILTER",
}

export interface AlertEvaluation {
    alert: Alert;
    detectionEvaluations: { [index: string]: DetectionEvaluation };
    start: number;
    end: number;
    lastTimestamp: number;
}
