/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AnomalyFilterOption } from "../../components/rca/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { Anomaly } from "./anomaly.interfaces";
import { Event } from "./event.interfaces";

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
    baselineOffset?: string;
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
    baselineOffset?: string;
    filters?: string[];
    summarySize?: number;
    depth?: number;
    oneSideError?: boolean;
    excludedDimensions?: string[];
    hierarchies?: string[];
}

export enum SavedStateKeys {
    QUERY_SEARCH_STRING = "querySearchString",
    CHART_FILTER_SET = "chartFilterSet",
    EVENT_SET = "eventSet",
}

export interface UIStateMetaData {
    [SavedStateKeys.QUERY_SEARCH_STRING]?: string;
    [SavedStateKeys.CHART_FILTER_SET]?: AnomalyFilterOption[][];
    [SavedStateKeys.EVENT_SET]?: Event[];
}

export interface Investigation {
    id: number;
    name: string;
    text: string;
    uiMetadata: UIStateMetaData;
    anomaly?: Partial<Anomaly>;
    created: number;
    updated: number;
    createdBy: {
        principal: string;
    };
    updatedBy: {
        principal: string;
    };
}
