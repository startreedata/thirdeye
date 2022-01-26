import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface AnomalyBreakdownComparisonHeatmapProps {
    anomalyId: number;
    comparisonOffset?: AnomalyBreakdownAPIOffsetValues;
    anomaly?: Anomaly | null;
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
    baseline: number;
    metricValueDiff: number;
    metricValueDiffPercentage: number | null;
    currentContributionPercentage: number;
    baselineContributionPercentage: number;
    contributionDiff: number;
    currentTotalCount: number;
    baselineTotalCount: number;
}

export interface DimensionDisplayData {
    columnName: string;
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
