/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { AnomalyBreakdown } from "../../../rest/dto/rca.interfaces";

export interface HeatMapProps {
    heatMapData: AnomalyBreakdown;
    anomalyFilters: AnomalyFilterOption[];
    onFilterChange: (filters: AnomalyFilterOption[]) => void;
    onHeightChange: (height: number) => void;
    dimensionsInOrder: string[];
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
