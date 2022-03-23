import axios from "axios";
import { Alert, AlertEvaluation, EditableAlert } from "../dto/alert.interfaces";

const BASE_URL_ALERTS = "/api/alerts";

export const getAlert = async (id: number): Promise<Alert> => {
    const response = await axios.get(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAllAlerts = async (): Promise<Alert[]> => {
    const response = await axios.get(BASE_URL_ALERTS);

    return response.data;
};

export const createAlert = async (alert: EditableAlert): Promise<Alert> => {
    const response = await axios.post(BASE_URL_ALERTS, [alert]);

    return response.data[0];
};

export const createAlerts = async (alerts: Alert[]): Promise<Alert[]> => {
    const response = await axios.post(BASE_URL_ALERTS, alerts);

    return response.data;
};

export const updateAlert = async (alert: Alert): Promise<Alert> => {
    const response = await axios.put(BASE_URL_ALERTS, [alert]);

    return response.data[0];
};

export const updateAlerts = async (alerts: Alert[]): Promise<Alert[]> => {
    const response = await axios.put(BASE_URL_ALERTS, alerts);

    return response.data;
};

export const deleteAlert = async (id: number): Promise<Alert> => {
    const response = await axios.delete(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAlertEvaluation = async (
    alertEvaluation: AlertEvaluation
): Promise<AlertEvaluation> => {
    const response = await axios.post(
        `${BASE_URL_ALERTS}/evaluate`,
        alertEvaluation
    );

    return response.data;
};
