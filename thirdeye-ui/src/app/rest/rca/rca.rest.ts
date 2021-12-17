import axios from "axios";
import {
    AnomalyBreakdown,
    AnomalyBreakdownRequest,
} from "../dto/rca.interfaces";

const BASE_URL_RCA = "/api/rca";

export const getAnomalyMetricBreakdown = async (
    id: number,
    params: AnomalyBreakdownRequest
): Promise<AnomalyBreakdown> => {
    const response = await axios.get(
        `${BASE_URL_RCA}/metrics/breakdown/anomaly/${id}`,
        {
            params,
        }
    );

    return response.data;
};
