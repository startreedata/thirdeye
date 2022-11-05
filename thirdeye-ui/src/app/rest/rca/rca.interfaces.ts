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
import { ActionHook } from "../actions.interfaces";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
    CohortDetectionResponse,
    Investigation,
} from "../dto/rca.interfaces";

export interface GetAnomalyMetricBreakdown extends ActionHook {
    anomalyMetricBreakdown: AnomalyBreakdown | null;
    getMetricBreakdown: (
        id: number,
        params: AnomalyBreakdownRequest
    ) => Promise<AnomalyBreakdown | undefined>;
}

export interface GetAnomalyDimensionAnalysis extends ActionHook {
    anomalyDimensionAnalysisData: AnomalyDimensionAnalysisData | null;
    getDimensionAnalysisData: (
        id: number,
        params: AnomalyDimensionAnalysisRequest
    ) => Promise<AnomalyDimensionAnalysisData | undefined>;
}

export interface GetInvestigations extends ActionHook {
    investigations: Investigation[] | null;
    getInvestigations: (
        alertId?: number
    ) => Promise<Investigation[] | undefined>;
}

export interface GetInvestigation extends ActionHook {
    investigation: Investigation | null;
    getInvestigation: (
        investigationId: number
    ) => Promise<Investigation | undefined>;
}

export interface GetCohorts extends ActionHook {
    cohortsResponse: CohortDetectionResponse | null;
    getCohorts: (
        params: GetCohortParams
    ) => Promise<CohortDetectionResponse | undefined>;
}

export interface GetCohortParams {
    start: number;
    end: number;
    metricId: number;
    dimensions?: string[];
    query?: string;
    threshold?: number;
    percentage?: number;
    resultSize?: number;
}

export interface CohortRequestParams {
    start: number;
    end: number;
    metric: {
        id: number;
    };
    dimensions?: string[];
    where?: string;
    threshold?: number;
    percentage?: number;
    timezone?: string;
    aggregate?: number;
    limit?: number;
    maxDepth?: number;
    resultSize?: number;
    generateEnumerationItems?: boolean;
    enumerationItemParamKey?: string;
}
