import axios from "axios";
import { duplicateKeyForArrayQueryParams } from "../../platform/utils/axios/axios.util";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
} from "../dto/rca.interfaces";

const BASE_URL_RCA = "/api/rca";

export const getAnomalyMetricBreakdown = async (
    id: number,
    params: AnomalyBreakdownRequest
): Promise<AnomalyBreakdown> => {
    const response = await axios.get(
        `${BASE_URL_RCA}/metrics/heatmap/anomaly/${id}`,
        {
            params,
            paramsSerializer: duplicateKeyForArrayQueryParams,
        }
    );

    return response.data;
};

/**
 * Take the input and format it into a URL the server understands.
 * See `/swagger#/Root%20Cause%20Analysis/dataCubeSummary` for more details on the endpoint.
 *
 * @param id - The ID of an anomaly to fetch the data for
 * @param params - See `./rca.interface.ts` for description of expected parameters
 */
export const getDimensionAnalysisForAnomaly = async (
    id: number,
    params: AnomalyDimensionAnalysisRequest
): Promise<AnomalyDimensionAnalysisData> => {
    const response = await axios.get(
        `${BASE_URL_RCA}/dim-analysis/anomaly/${id}`,
        {
            params,
            paramsSerializer: duplicateKeyForArrayQueryParams,
        }
    );

    return response.data;
};
