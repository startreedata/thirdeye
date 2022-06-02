import { AnomalyBreakdownAPIOffsetValues } from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../../rest/dto/rca.interfaces";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis.interfaces";

export interface AlgorithmTableProps {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    anomaly: Anomaly;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
    onCheckClick?: (filters: AnomalyFilterOption[]) => void;
    chartTimeSeriesFilterSet: AnomalyFilterOption[][];
}

export interface AlgorithmRowProps {
    alertId: number;
    startTime: number;
    endTime: number;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
    dataset: string;
    metric: string;
    row: AnomalyDimensionAnalysisMetricRow;
    totalSum: number;
    dimensionColumns: string[];
    checked: boolean;
    onCheckClick?: (filters: AnomalyFilterOption[]) => void;
}

export interface AlgorithmRowExpandedProps {
    startTime: number;
    endTime: number;
    alertId: number;
    row: AnomalyDimensionAnalysisMetricRow;
    dimensionColumns: string[];
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
}
