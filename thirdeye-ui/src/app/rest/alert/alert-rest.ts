import axios, { AxiosResponse } from "axios";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";

const BASE_URL_ALERTS = "/api/alerts";

export const getAlert = async (id: number): Promise<Alert> => {
    const response: AxiosResponse = await axios.get(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAllAlerts = async (): Promise<Alert[]> => {
    const response: AxiosResponse = await axios.get(BASE_URL_ALERTS);

    return response.data;
};

export const createAlert = async (alert: Alert): Promise<Alert> => {
    const response: AxiosResponse = await axios.post(BASE_URL_ALERTS, [alert]);

    return response.data[0];
};

export const updateAlert = async (alert: Alert): Promise<Alert> => {
    const response: AxiosResponse = await axios.put(BASE_URL_ALERTS, [alert]);

    return response.data[0];
};

export const deleteAlert = async (id: number): Promise<Alert> => {
    const response: AxiosResponse = await axios.delete(
        `${BASE_URL_ALERTS}/${id}`
    );

    return response.data;
};

export const getAlertPreview = async (
    alertEvaluation: AlertEvaluation
): Promise<AlertEvaluation> => {
    const response: AxiosResponse = await axios.post(
        `${BASE_URL_ALERTS}/preview`,
        alertEvaluation
    );

    return response.data;
};
