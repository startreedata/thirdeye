import { Alert } from "../../rest/dto/alert.interfaces";

export interface AlertCardProps {
    alert: AlertCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
    onStateToggle?: (alertCardData: AlertCardData) => void;
    onDelete?: (alertCardData: AlertCardData) => void;
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
