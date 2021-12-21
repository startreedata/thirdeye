export interface AnomalyBreakdownComparisonHeatmapProps {
    anomalyId: number;
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
