import { Alert } from "./alert.interfaces";

export interface UiAlert {
    id: number;
    name: string;
    active: boolean;
    activeText: string;
    userId: number;
    createdBy: string;
    detectionTypes: string[];
    filteredBy: string[];
    datasetAndMetrics: UiAlertDatasetAndMetric[];
    subscriptionGroups: UiAlertSubscriptionGroup[];
    alert: Alert | null;
}

export interface UiAlertDatasetAndMetric {
    datasetId: number;
    datasetName: string;
    metricId: number;
    metricName: string;
}

export interface UiAlertSubscriptionGroup {
    id: number;
    name: string;
}
