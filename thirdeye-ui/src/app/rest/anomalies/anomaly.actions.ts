import { useHTTPAction } from "../create-rest-action";
import { Anomaly } from "../dto/anomaly.interfaces";
import { getAnomaly as getAnomalyRest } from "./anomalies.rest";
import { GetAnomaly } from "./anomaly.interfaces";

export const useGetAnomaly = (): GetAnomaly => {
    const { data, makeRequest, status, errorMessage } = useHTTPAction<Anomaly>(
        getAnomalyRest
    );

    const getAnomaly = (id: number): Promise<Anomaly | undefined> => {
        return makeRequest(id);
    };

    return { anomaly: data, getAnomaly, status, errorMessage };
};
