export interface AnomalyCardProps {
    anomaly: AnomalyCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
    onDelete?: (anomalyCardData: AnomalyCardData) => void;
}

export interface AnomalyCardData {
    id: number;
    name: string;
    alertName: string;
    alertId: number;
    current: string;
    predicted: string;
    deviation: string;
    negativeDeviation: boolean;
    duration: string;
    startTime: string;
    endTime: string;
}
