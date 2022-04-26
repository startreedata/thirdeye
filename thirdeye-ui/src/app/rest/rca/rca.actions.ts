import { useHTTPAction } from "../create-rest-action";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
} from "../dto/rca.interfaces";
import {
    GetAnomalyDimensionAnalysis,
    GetAnomalyMetricBreakdown,
} from "./rca.interfaces";
import {
    getAnomalyMetricBreakdown,
    getDimensionAnalysisForAnomaly,
} from "./rca.rest";

export const useGetAnomalyMetricBreakdown = (): GetAnomalyMetricBreakdown => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AnomalyBreakdown>(getAnomalyMetricBreakdown);

    const getMetricBreakdown = (
        id: number,
        params: AnomalyBreakdownRequest
    ): Promise<AnomalyBreakdown | undefined> => {
        return makeRequest(id, params);
    };

    return {
        anomalyMetricBreakdown: data,
        getMetricBreakdown,
        status,
        errorMessages,
    };
};

export const useGetAnomalyDimensionAnalysis =
    (): GetAnomalyDimensionAnalysis => {
        const { data, makeRequest, status, errorMessages } =
            useHTTPAction<AnomalyDimensionAnalysisData>(
                getDimensionAnalysisForAnomaly
            );

        const getDimensionAnalysisData = (
            id: number,
            params: AnomalyDimensionAnalysisRequest
        ): Promise<AnomalyDimensionAnalysisData | undefined> => {
            return makeRequest(id, params);
        };

        return {
            anomalyDimensionAnalysisData: data,
            getDimensionAnalysisData,
            status,
            errorMessages,
        };
    };
