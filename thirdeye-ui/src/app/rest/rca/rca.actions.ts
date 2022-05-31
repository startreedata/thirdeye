import { useHTTPAction } from "../create-rest-action";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
    Investigation,
} from "../dto/rca.interfaces";
import {
    GetAnomalyDimensionAnalysis,
    GetAnomalyMetricBreakdown,
    GetInvestigation,
    GetInvestigations,
} from "./rca.interfaces";
import {
    getAnomalyMetricBreakdown,
    getDimensionAnalysisForAnomaly,
    getInvestigation as getInvestigationRest,
    getInvestigations as getInvestigationsRest,
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

export const useGetInvestigations = (): GetInvestigations => {
    const { data, makeRequest, status, errorMessages } = useHTTPAction<
        Investigation[]
    >(getInvestigationsRest);

    const getInvestigations = (
        anomalyId?: number
    ): Promise<Investigation[] | undefined> => {
        return makeRequest(anomalyId);
    };

    return {
        investigations: data,
        getInvestigations,
        status,
        errorMessages,
    };
};

export const useGetInvestigation = (): GetInvestigation => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Investigation>(getInvestigationRest);

    const getInvestigation = (
        investigationId: number
    ): Promise<Investigation | undefined> => {
        return makeRequest(investigationId);
    };

    return {
        investigation: data,
        getInvestigation,
        status,
        errorMessages,
    };
};
