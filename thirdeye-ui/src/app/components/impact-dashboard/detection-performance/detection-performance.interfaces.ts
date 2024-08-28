/*
 * Copyright 2024 StarTree Inc
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

export type DetectionPerformanceProps = {
    anomalies: Anomaly[] | null;
    previousPeriodAnomalies: Anomaly[] | null;
    selectedAnalysisPeriod: string;
    onAnalysisPeriodChange: (period: string) => void;
    analysisPeriods: string[];
};

export type AnomaliesGraphProps = {
    title: string;
    notificationText?: string;
    anomalies: Anomaly[] | null;
    previousPeriodAnomalies: Anomaly[] | null;
    selectedAnalysisPeriod: string;
};

export type TileRendererProps = {
    title: string;
    notificationText?: string;
};

export type TooltipData = {
    currentPeriod: number;
    previousPeriod: number;
    currentPeriodDate: string;
    previousPeriodDate: string;
};
