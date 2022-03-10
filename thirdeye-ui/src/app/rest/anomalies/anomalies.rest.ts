import axios from "axios";
import { Anomaly, AnomalyFeedback } from "../dto/anomaly.interfaces";

const BASE_URL_ANOMALIES = "/api/anomalies";

export const getAnomaly = async (id: number): Promise<Anomaly> => {
    const response = await axios.get(`${BASE_URL_ANOMALIES}/${id}`);

    return response.data;
};

export const getAllAnomalies = async (): Promise<Anomaly[]> => {
    const response = await axios.get(`${BASE_URL_ANOMALIES}?isChild=false`);

    return response.data;
};

export const getAnomaliesByAlertId = async (
    alertId: number
): Promise<Anomaly[]> => {
    const response = await axios.get(
        `${BASE_URL_ANOMALIES}?isChild=false&alert.id=${alertId}`
    );

    return response.data;
};

export const getAnomaliesByTime = async (
    startTime: number,
    endTime: number
): Promise<Anomaly[]> => {
    const response = await axios.get(
        `${BASE_URL_ANOMALIES}?isChild=false&startTime=[gte]${startTime}&endTime=[lte]${endTime}`
    );

    return response.data;
};

export const getAnomaliesByAlertIdAndTime = async (
    alertId: number,
    startTime: number,
    endTime: number
): Promise<Anomaly[]> => {
    const response = await axios.get(
        `${BASE_URL_ANOMALIES}?isChild=false&alert.id=${alertId}&startTime=[gte]${startTime}&endTime=[lte]${endTime}`
    );

    return response.data;
};

export const deleteAnomaly = async (id: number): Promise<Anomaly> => {
    const response = await axios.delete(`${BASE_URL_ANOMALIES}/${id}`);

    return response.data;
};

export const updateAnomalyFeedback = async (
    anomalyId: number,
    feedback: AnomalyFeedback
): Promise<AnomalyFeedback> => {
    const response = await axios.post(
        `${BASE_URL_ANOMALIES}/${anomalyId}/feedback`,
        feedback
    );

    return response.data;
};
