export interface UiAnomaly {
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
