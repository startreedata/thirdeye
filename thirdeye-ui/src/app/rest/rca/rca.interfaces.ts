import { ActionHook } from "../actions.interfaces";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
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
