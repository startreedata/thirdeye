export interface AnomalyCardProps {
    anomalyCardData: AnomalyCardData;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (anomalyCardData: AnomalyCardData) => void;
}

export interface AnomalyCardData {
    id: number;
    name: string;
    alertId: number;
    alertName: string;
    current: string;
    predicted: string;
    deviation: string;
    negativeDeviation: boolean;
    duration: string;
    startTime: string;
    endTime: string;
}
