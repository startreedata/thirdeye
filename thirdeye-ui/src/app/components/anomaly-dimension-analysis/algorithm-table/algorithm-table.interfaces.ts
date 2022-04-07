import { AnomalyBreakdownAPIOffsetValues } from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../../rest/dto/rca.interfaces";

export interface AlgorithmTableProps {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    anomaly: Anomaly;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
}

export interface AlgorithmRowProps {
    anomaly: Anomaly;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
    dataset: string;
    metric: string;
    row: AnomalyDimensionAnalysisMetricRow;
    totalSum: number;
    dimensionColumns: string[];
}

export interface AlgorithmRowExpandedProps {
    anomaly: Anomaly;
    row: AnomalyDimensionAnalysisMetricRow;
    dimensionColumns: string[];
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
}
