export interface AnomalyCardData {
    id: number;
    name: string;
    alertName: string;
    alertId: number;
    currentAndPredicted: string;
    deviation: string;
    negativeDeviation: boolean;
    duration: string;
    startTime: string;
    endTime: string;
}

export interface AnomalyCardProps {
    anomaly: AnomalyCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
}
