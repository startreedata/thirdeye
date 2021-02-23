import {
    LogicalView,
    MetricAggFunction,
} from "../../../rest/dto/metric.interfaces";

export interface MetricCardProps {
    metricCardData: MetricCardData;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (metricCardData: MetricCardData) => void;
}

export interface MetricCardData {
    id: number;
    name: string;
    datasetId: number;
    datasetName: string;
    active: boolean;
    activeText: string;
    aggregationColumn: string;
    aggregationFunction: MetricAggFunction;
    viewCount: string;
    views: LogicalView[];
}
