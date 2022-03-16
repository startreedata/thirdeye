import { useHTTPAction } from "../create-rest-action";
import { Anomaly } from "../dto/anomaly.interfaces";
import {
    getAnomalies as getAnomaliesRest,
    getAnomaly as getAnomalyRest,
} from "./anomalies.rest";
import {
    GetAnomalies,
    GetAnomaliesProps,
    GetAnomaly,
} from "./anomaly.interfaces";

export const useGetAnomaly = (): GetAnomaly => {
    const { data, makeRequest, status, errorMessage } =
        useHTTPAction<Anomaly>(getAnomalyRest);

    const getAnomaly = (id: number): Promise<Anomaly | undefined> => {
        return makeRequest(id);
    };

    return { anomaly: data, getAnomaly, status, errorMessage };
};

export const useGetAnomalies = (): GetAnomalies => {
    const { data, makeRequest, status, errorMessage } =
        useHTTPAction<Anomaly[]>(getAnomaliesRest);

    const getAnomalies = (
        getAnomalyParams: GetAnomaliesProps = {}
    ): Promise<Anomaly[] | undefined> => {
        return makeRequest(getAnomalyParams);
    };

    return {
        anomalies: data,
        getAnomalies,
        status,
        errorMessage,
    };
};
