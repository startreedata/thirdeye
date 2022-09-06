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
//  import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";

import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../../rest/dto/ui-anomaly.interfaces";

export interface MetricsReportEvaluationTimeSeriesProps {
    data: UiAnomaly;
    searchParams?: URLSearchParams | null;
}

export enum MetricReportEvaluationTimeSeriesStateAction {
    UPDATE,
}

export interface MetricsReportEvaluationTimeSeriesState {
    loading: boolean;
    noData: boolean;
    filteredAlertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    filteredAlertEvaluationAnomalies: Anomaly[];
    currentPlotVisible: boolean;
    anomaliesPlotVisible: boolean;
}

export interface AlertEvaluationTimeSeriesPoint {
    timestamp: number;
    current: number;
    expected: number;
    upperBound: number;
    lowerBound: number;
}
