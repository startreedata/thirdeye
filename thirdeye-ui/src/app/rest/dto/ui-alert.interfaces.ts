import { Alert } from "./alert.interfaces";

export interface UiAlert {
    id: number;
    name: string;
    active: boolean;
    activeText: string;
    userId: number;
    createdBy: string;
    detectionTypes: string[];
    detectionTypesCount: string;
    filteredBy: string[];
    filteredByCount: string;
    datasetAndMetrics: UiAlertDatasetAndMetric[];
    datasetAndMetricsCount: string;
    subscriptionGroups: UiAlertSubscriptionGroup[];
    subscriptionGroupsCount: string;
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
