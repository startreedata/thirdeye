import { LogicalView, MetricAggFunction } from "./metric.interfaces";

export interface UiMetric {
    id: number;
    name: string;
    datasetId: number;
    datasetName: string;
    active: boolean;
    activeText: string;
    aggregationColumn: string;
    aggregationFunction: MetricAggFunction;
    views: LogicalView[];
    viewCount: string;
    rollupThreshold?: number;
}
