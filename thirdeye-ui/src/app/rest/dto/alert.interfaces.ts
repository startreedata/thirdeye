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
    detections: Map<string, AlertComponent>;
    filters: Map<string, AlertComponent>;
    qualityChecks: Map<string, AlertComponent>;
}

export interface AlertComponent {
    name: string;
    type: string;
    metric: Metric;
    params: Map<string, unknown>;
    alert: Alert;
}
