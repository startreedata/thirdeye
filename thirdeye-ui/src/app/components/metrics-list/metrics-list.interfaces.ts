export interface MetricsListData {
    id: number;
    idText: string;
    name: string;
    datasetName: string;
    active: boolean;
    activeText: string;
    aggregationFunction: string;
    rollupThreshold: number;
    rollupThresholdText: string;
}

export interface MetricsListProps {
    metrics: MetricsListData[];
}
