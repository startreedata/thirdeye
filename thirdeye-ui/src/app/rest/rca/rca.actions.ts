import { useHTTPAction } from "../create-rest-action";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
} from "../dto/rca.interfaces";
import { GetAnomalyMetricBreakdown } from "./rca.interfaces";
import { getAnomalyMetricBreakdown } from "./rca.rest";

export const useGetAnomalyMetricBreakdown = (): GetAnomalyMetricBreakdown => {
    const {
        data,
        makeRequest,
        status,
        errorMessage,
    } = useHTTPAction<AnomalyBreakdown>(getAnomalyMetricBreakdown);

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
        errorMessage,
    };
};
