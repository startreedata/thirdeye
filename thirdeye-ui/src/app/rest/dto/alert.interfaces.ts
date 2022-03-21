import { DetectionEvaluation } from "./detection.interfaces";
import { Metric } from "./metric.interfaces";
import { SubscriptionGroup } from "./subscription-group.interfaces";
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
    filters: { [index: string]: string[] };
    nodes: { [index: string]: AlertNode };
    subscriptionGroups: SubscriptionGroup[];
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
