import axios from "axios";
import { AlertTemplate } from "../dto/alert-template.interfaces";

const BASE_URL_ALERTS = "/api/alert-templates";

export const getAlertTemplate = async (id: number): Promise<AlertTemplate> => {
    const response = await axios.get(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAlertTemplates = async (): Promise<AlertTemplate[]> => {
    const response = await axios.get(BASE_URL_ALERTS);

    return response.data;
};

export const createAlertTemplate = async (
    alertTemplate: AlertTemplate
): Promise<AlertTemplate> => {
    const response = await axios.post(BASE_URL_ALERTS, [alertTemplate]);

    return response.data[0];
};

export const updateAlertTemplate = async (
    alertTemplate: AlertTemplate
): Promise<AlertTemplate> => {
    const response = await axios.put(BASE_URL_ALERTS, [alertTemplate]);

    return response.data[0];
};

export const deleteAlertTemplate = async (
    id: number
): Promise<AlertTemplate> => {
    const response = await axios.delete(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};
