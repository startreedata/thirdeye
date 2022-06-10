///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import { AnomalyBreakdownAPIOffsetValues } from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../../rest/dto/rca.interfaces";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis.interfaces";

export interface AlgorithmTableProps {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData;
    anomaly: Anomaly;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
    onCheckClick?: (filters: AnomalyFilterOption[]) => void;
    chartTimeSeriesFilterSet: AnomalyFilterOption[][];
}

export interface AlgorithmRowProps {
    alertId: number;
    startTime: number;
    endTime: number;
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
    dataset: string;
    metric: string;
    row: AnomalyDimensionAnalysisMetricRow;
    totalSum: number;
    dimensionColumns: string[];
    checked: boolean;
    onCheckClick?: (filters: AnomalyFilterOption[]) => void;
}

export interface AlgorithmRowExpandedProps {
    startTime: number;
    endTime: number;
    alertId: number;
    row: AnomalyDimensionAnalysisMetricRow;
    dimensionColumns: string[];
    comparisonOffset: AnomalyBreakdownAPIOffsetValues;
}
