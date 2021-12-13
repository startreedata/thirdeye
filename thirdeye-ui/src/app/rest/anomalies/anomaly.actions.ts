import { useHTTPAction } from "../create-rest-action";
import { Anomaly } from "../dto/anomaly.interfaces";
import { BASE_URL_ANOMALIES } from "./anomalies.rest";
import { GetAnomaly } from "./anomaly.interfaces";

export const useGetAnomaly = (): GetAnomaly => {
    const {
        data,
        makeRequest,
        status,
        errorMessage,
    } = useHTTPAction<Anomaly>();

    const getAnomaly = (id: number): Promise<Anomaly | undefined> => {
        return makeRequest(`${BASE_URL_ANOMALIES}/${id}`);
    };

    return { anomaly: data, getAnomaly, status, errorMessage };
};
