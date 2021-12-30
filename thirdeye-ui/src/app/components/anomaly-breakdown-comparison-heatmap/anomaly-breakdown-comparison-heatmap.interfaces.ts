import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";

export interface AnomalyBreakdownComparisonHeatmapProps {
    anomalyId: number;
    comparisonOffset?: AnomalyBreakdownAPIOffsetValues;
}

export interface SummaryData {
    [key: string]: {
        count: number;
        percentage: number;
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
}

export interface AnomalyBreakdownComparisonDataByDimensionColumn {
    column: string;
    dimensionComparisonData: {
        [key: string]: AnomalyBreakdownComparisonData;
    };
}

export interface AnomalyFilterOptions {
    key: string;
    value: string;
}
