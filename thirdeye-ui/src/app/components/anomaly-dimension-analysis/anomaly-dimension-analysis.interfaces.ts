import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../rest/dto/rca.interfaces";

export interface AnomalyDimensionAnalysisProps {
    anomalyId: number;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
    anomaly: Anomaly;
}
export interface AnomalyDimensionAnalysisTableProps {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    anomaly: Anomaly;
}

export interface AnomalyDimensionAnalysisMetricRowProps {
    dataset: string;
    metric: string;
    row: AnomalyDimensionAnalysisMetricRow;
    totalSum: number;
    dimensionColumns: string[];
}

export interface SummaryData {
    [key: string]: {
        count: number;
        percentage: number;
        totalCount: number;
    };
}

export interface SummarizeDataFunctionParams {
    [key: string]: number;
}

export interface AnomalyBreakdownComparisonData {
    current: number;
    currentPercentage: number;
    comparison: number;
    comparisonPercentage: number;
    percentageDiff: number;
    currentTotalCount: number;
    comparisonTotalCount: number;
}

export interface AnomalyBreakdownComparisonDataByDimensionColumn {
    column: string;
    dimensionComparisonData: {
        [key: string]: AnomalyBreakdownComparisonData;
    };
}

export interface AnomalyFilterOption {
    key: string;
    value: string;
}
