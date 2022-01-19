import {
    AnomalyBreakdownAPIOffsetValues,
    AnomalyBreakdownAPIOffsetValuesV2,
} from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";

export interface AnomalyBreakdown {
    [key: string]: {
        [key: string]: number;
    };
}

export interface AnomalyBreakdownRequest {
    offset?: AnomalyBreakdownAPIOffsetValues;
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
    responseRows: {
        baselineValue: number;
        currentValue: number;
        sizeFactor: number;
        percentageChange: number;
        contributionChange: number;
        contributionToOverallChange: number;
        names: string[];
        otherDimensionValues: string[];
        moreOtherDimensionNumber: number;
        cost: number;
    }[];
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
    baselineOffset?: AnomalyBreakdownAPIOffsetValuesV2;
    filters?: string[];
    summarySize?: number;
    depth?: number;
    oneSideError?: boolean;
    excludedDimensions?: string[];
    hierarchies?: string[];
}
