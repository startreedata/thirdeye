import axios from "axios";
import { duplicateKeyForArrayQueryParams } from "../../platform/utils/axios/axios.util";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisRequest,
    CohortDetectionResponse,
    Investigation,
} from "../dto/rca.interfaces";
import { CohortRequestParams, GetCohortParams } from "./rca.interfaces";

const ANOMALY_ID_QUERY_PARAM_KEY = "id";
const ANOMALY_ID_FILTER_QUERY_PARAM_KEY = "anomaly.id";
const BASE_URL_RCA = "/api/rca";
const INVESTIGATIONS_ENDPOINT = `${BASE_URL_RCA}/investigations`;

export const getAnomalyMetricBreakdown = async (
    id: number,
    params: AnomalyBreakdownRequest
): Promise<AnomalyBreakdown> => {
    const queryParams = new URLSearchParams([
        [ANOMALY_ID_QUERY_PARAM_KEY, id.toString()],
    ]);
    const response = await axios.get(
        `${BASE_URL_RCA}/metrics/heatmap?${queryParams.toString()}`,
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
    const queryParams = new URLSearchParams([
        [ANOMALY_ID_QUERY_PARAM_KEY, id.toString()],
    ]);
    const response = await axios.get(
        `${BASE_URL_RCA}/dim-analysis?${queryParams.toString()}`,
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
        queryParams.set(
            ANOMALY_ID_FILTER_QUERY_PARAM_KEY,
            anomalyId.toString()
        );
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

export const getCohorts = async ({
    start,
    end,
    metricId,
    dimensions,
    query,
    threshold,
    percentage,
    resultSize,
}: GetCohortParams): Promise<CohortDetectionResponse> => {
    const requestPayload: CohortRequestParams = {
        start,
        end,
        metric: {
            id: metricId,
        },
    };

    if (resultSize) {
        requestPayload.resultSize = resultSize;
    }

    if (threshold) {
        requestPayload.threshold = threshold;
    }

    if (percentage) {
        requestPayload.percentage = percentage;
    }

    if (dimensions && dimensions.length > 0) {
        requestPayload.dimensions = dimensions;
    }

    if (query && query.length > 0) {
        requestPayload.where = query;
    }

    const response = await axios.post(
        "/api/rca/metrics/cohorts",
        requestPayload
    );

    return response.data;
};
