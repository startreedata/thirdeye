import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { Anomaly } from "./anomaly.interfaces";

export interface AnomalyBreakdown {
    metric: {
        name: string;
        dataset: {
            name: string;
        };
    };
    current: {
        breakdown: {
            [key: string]: {
                [key: string]: number;
            };
        };
    };
    baseline: {
        breakdown: {
            [key: string]: {
                [key: string]: number;
            };
        };
    };
}

export interface AnomalyBreakdownRequest {
    baselineOffset?: AnomalyBreakdownAPIOffsetValues;
    timezone?: string;
    filters?: string[];
    limit?: number;
}

export interface AnomalyDimensionAnalysisDataGainerLoser {
    baselineValue: number;
    currentValue: number;
    sizeFactor: number;
    percentageChange: number;
    contributionChange: number;
    contributionToOverallChange: number;
    dimensionName: string;
    dimensionValue: string;
    cost: number;
}

export interface AnomalyDimensionAnalysisMetricRow {
    baselineValue: number;
    currentValue: number;
    sizeFactor: number;
    changePercentage: number | string; // Can be "NaN"
    contributionChange: number;
    contributionToOverallChange: number;
    names: string[];
    otherDimensionValues: string[];
    moreOtherDimensionNumber: number;
    cost: number;
}

export interface AnomalyDimensionAnalysisData {
    metric: {
        name: string;
        dataset: {
            name: string;
        };
    };
    baselineTotal: number;
    currentTotal: number;
    baselineTotalSize: number;
    currentTotalSize: number;
    globalRatio: number;
    dimensions: string[];
    responseRows: AnomalyDimensionAnalysisMetricRow[];
    gainer: AnomalyDimensionAnalysisDataGainerLoser[];
    loser: AnomalyDimensionAnalysisDataGainerLoser[];
    dimensionCosts: {
        name: string;
        cost: number;
    }[];
}

/**
 *
 * @param baselineOffset - Expected to be in ISO 8601 format(e.g. "P1W").
 *                         See https://en.wikipedia.org/wiki/ISO_8601#Durations
 * @param filters - dimension filters (e.g. ["dim1=val1", "dim2!=val2"])
 * @param summarySize - Number of entries to put in the summary.
 * @param depth - Number of dimensions to drill down by.
 * @param oneSideError - If true, only returns changes that have the same
 *                       direction as the global change.
 * @param excludedDimensions - List of dimension columns to exclude
 * @param hierarchies - Hierarchy among some dimensions.
 *                      The order will be respected in the result. An example of a
 *                      hierarchical group is {continent, country}.
 *                      Parameter format is [["continent","country"], ["dim1", "dim2", "dim3"]]
 */
export interface AnomalyDimensionAnalysisRequest {
    baselineOffset?: AnomalyBreakdownAPIOffsetValues;
    filters?: string[];
    summarySize?: number;
    depth?: number;
    oneSideError?: boolean;
    excludedDimensions?: string[];
    hierarchies?: string[];
}

export interface Investigation {
    id: number;
    name: string;
    text: string;
    uiMetadata: {
        [key: string]: unknown;
    };
    anomaly?: Anomaly;
    created: number;
    updated: number;
    createdBy: {
        principal: string;
    };
    updatedBy: {
        principal: string;
    };
}
