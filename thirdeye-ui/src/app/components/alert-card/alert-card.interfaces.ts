import { Alert } from "../../rest/dto/alert.interfaces";

export interface AlertDatasetAndMetric {
    datasetId: number;
    datasetName: string;
    metricId: number;
    metricName: string;
}

export interface AlertSubscriptionGroup {
    id: number;
    name: string;
}

export interface AlertCardData {
    id: number;
    name: string;
    active: boolean;
    activeText: string;
    userId: number;
    createdBy: string;
    detectionTypes: string[];
    filteredBy: string[];
    datasetAndMetrics: AlertDatasetAndMetric[];
    subscriptionGroups: AlertSubscriptionGroup[];
    alert: Alert | null;
}

export interface AlertCardProps {
    alert: AlertCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
    onAlertStateToggle?: (alert: AlertCardData) => void;
}
