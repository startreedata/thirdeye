import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../../rest/dto/rca.interfaces";

export interface AlgorithmTableProps {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    anomaly: Anomaly;
}

export interface AlgorithmRowProps {
    anomaly: Anomaly;
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
}
