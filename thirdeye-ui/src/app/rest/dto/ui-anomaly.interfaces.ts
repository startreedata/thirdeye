export interface UiAnomaly {
    id: number;
    name: string;
    alertId: number;
    alertName: string;
    current: string;
    currentVal: number;
    predicted: string;
    predictedVal: number;
    deviation: string;
    deviationVal: number;
    negativeDeviation: boolean;
    duration: string;
    durationVal: number;
    startTime: string;
    endTime: string;
}
