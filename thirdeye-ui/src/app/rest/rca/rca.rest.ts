import axios from "axios";
import { duplicateKeyForArrayQueryParams } from "../../platform/utils/axios/axios.util";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
    Investigation,
} from "../dto/rca.interfaces";

const BASE_URL_RCA = "/api/rca";
const INVESTIGATIONS_ENDPOINT = `${BASE_URL_RCA}/investigations`;

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

export const getInvestigations = async (
    anomalyId?: number
): Promise<Investigation[]> => {
    const queryParams = new URLSearchParams();
    let queryString = "";

    if (anomalyId) {
        queryParams.set("anomaly.id", anomalyId.toString());
        queryString = `?${queryParams.toString()}`;
    }

    const response = await axios.get(
        `${INVESTIGATIONS_ENDPOINT}${queryString}`
    );

    return response.data;
};

export const getInvestigation = async (
    investigationId: number
): Promise<Investigation> => {
    const response = await axios.get(
        `${INVESTIGATIONS_ENDPOINT}/${investigationId}`
    );

    return response.data;
};

export const deleteInvestigation = async (
    id: number
): Promise<Investigation> => {
    const response = await axios.delete(`${INVESTIGATIONS_ENDPOINT}/${id}`);

    return response.data;
};

export const createInvestigation = async (
    investigation: Investigation
): Promise<Investigation> => {
    const response = await axios.post(`${INVESTIGATIONS_ENDPOINT}`, [
        investigation,
    ]);

    return response.data[0];
};

export const updateInvestigation = async (
    investigation: Investigation
): Promise<Investigation> => {
    const response = await axios.put(`${INVESTIGATIONS_ENDPOINT}`, [
        investigation,
    ]);

    return response.data[0];
};
