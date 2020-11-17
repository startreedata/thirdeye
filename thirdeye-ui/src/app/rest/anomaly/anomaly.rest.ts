import axios, { AxiosResponse } from "axios";
import { Anomaly } from "../dto/anomaly.interfaces";

const BASE_URL_ANOMALIES = "/api/anomalies";

export const getAnomaly = async (id: number): Promise<Anomaly> => {
    const response: AxiosResponse = await axios.get(
        `${BASE_URL_ANOMALIES}/${id}`
    );

    return response.data;
};

export const getAllAnomalies = async (): Promise<Anomaly[]> => {
    const response: AxiosResponse = await axios.get(BASE_URL_ANOMALIES);

    return response.data;
};

export const deleteAnomaly = async (id: number): Promise<Anomaly> => {
    const response: AxiosResponse = await axios.delete(
        `${BASE_URL_ANOMALIES}/${id}`
    );

    return response.data;
};
