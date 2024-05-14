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
import { AlertInsight } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../../rest/dto/rca.interfaces";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";

export interface TopContributorsTableProps {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    anomaly: Anomaly;
    comparisonOffset: string;
    onCheckClick?: (filters: AnomalyFilterOption[]) => void;
    chartTimeSeriesFilterSet: AnomalyFilterOption[][];
    alertInsight: AlertInsight | null;
}

export interface TopContributorsRowProps {
    anomaly: Anomaly;
    comparisonOffset: string;
    dataset: string;
    metric: string;
    row: AnomalyDimensionAnalysisMetricRow;
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    totalSum: number;
    dimensionColumns: string[];
    checked: boolean;
    onCheckClick?: (filters: AnomalyFilterOption[]) => void;
    timezone: string | undefined;
    hideTime: boolean | undefined;
    granularity: string | undefined;
}

export interface TopContributorsRowExpandedProps {
    alertId: number;
    row: AnomalyDimensionAnalysisMetricRow;
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    dimensionColumns: string[];
    comparisonOffset: string;
    timezone: string | undefined;
    hideTime: boolean | undefined;
    anomaly: Anomaly;
    granularity: string | undefined;
}

export interface ExtraDataForAnomalyDimensionAnalysisData {
    baselineStart: number;
    baselineEnd: number;
    anomalyStart: number;
    anomalyEnd: number;
}

export enum TopContributorsTableChangePercentSort {
    ASC = "asc",
    DESC = "desc",
}
