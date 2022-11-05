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
    metricId: number;
    metricName: string;
    duration: string;
    durationVal: number;
    startTime: string;
    startTimeVal: number;
    endTime: string;
    endTimeVal: number;
    datasetName: string;
    hasFeedback: boolean;
}
