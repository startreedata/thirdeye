import { useHTTPAction } from "../create-rest-action";
import { Anomaly } from "../dto/anomaly.interfaces";
import {
    getAnomaliesByAlertIdAndTime,
    getAnomaly as getAnomalyRest,
} from "./anomalies.rest";
import { GetAnomaly, GetAnomalyByAlertIdAndTime } from "./anomaly.interfaces";

export const useGetAnomaly = (): GetAnomaly => {
    const { data, makeRequest, status, errorMessage } =
        useHTTPAction<Anomaly>(getAnomalyRest);

    const getAnomaly = (id: number): Promise<Anomaly | undefined> => {
        return makeRequest(id);
    };

    return { anomaly: data, getAnomaly, status, errorMessage };
};

export const useGetAnomalyByAlertIdAndTime = (): GetAnomalyByAlertIdAndTime => {
    const { data, makeRequest, status, errorMessage } = useHTTPAction<
        Anomaly[]
    >(getAnomaliesByAlertIdAndTime);

    const getAnomalyByAlertIdAndTime = (
        alertId: number,
        startTime: number,
        endTime: number
    ): Promise<Anomaly[] | undefined> => {
        return makeRequest(alertId, startTime, endTime);
    };

    return {
        anomalies: data,
        getAnomalyByAlertIdAndTime,
        status,
        errorMessage,
    };
};
