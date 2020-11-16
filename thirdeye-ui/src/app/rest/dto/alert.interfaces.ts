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
    nodes: { [index: string]: AlertNode };
}

export interface AlertNode {
    name: string;
    type: string;
    subType: string;
    metric: Metric;
    params: { [index: string]: unknown };
    dependsOn: string[];
}

export interface AlertEvaluation {
    alert: Alert;
    detectionEvaluations: { [index: string]: DetectionEvaluation };
    start: Date;
    end: Date;
    lastTimestamp: Date;
}
