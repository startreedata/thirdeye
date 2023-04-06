/*
 * Copyright 2022 StarTree Inc
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
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

export interface AnomalyDimensionAnalysisProps {
    anomalyId: number;
    comparisonOffset: string;
    anomaly: Anomaly;
    chartTimeSeriesFilterSet: AnomalyFilterOption[][];
    onCheckClick: (filters: AnomalyFilterOption[]) => void;
    timezone: string | undefined;
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
