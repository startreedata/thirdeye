import axios, { AxiosResponse } from "axios";
import useSWR, { responseInterface } from "swr";
import { Anomaly } from "../dto/anomaly.interfaces";

const BASE_URL_ANOMALIES = "/api/anomalies";

export const useAnomaly = (id: number): responseInterface<Anomaly, Error> => {
    return useSWR(`${BASE_URL_ANOMALIES}/${id}`);
};

export const useAllAnomalies = (): responseInterface<Anomaly[], Error> => {
    return useSWR(BASE_URL_ANOMALIES);
};

export const deleteAnomaly = async (id: number): Promise<Anomaly> => {
    const response: AxiosResponse = await axios.delete(
        `${BASE_URL_ANOMALIES}/${id}`
    );

    return response.data;
};
