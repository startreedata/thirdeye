import { ActionHook } from "../actions.interfaces";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
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
