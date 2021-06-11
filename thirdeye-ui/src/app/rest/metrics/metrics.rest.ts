import axios from "axios";
import { LogicalMetric, Metric } from "../dto/metric.interfaces";

const BASE_URL_METRICS = "/api/metrics";

export const getMetric = async (id: number): Promise<Metric> => {
    const response = await axios.get(`${BASE_URL_METRICS}/${id}`);

    return response.data;
};

export const getAllMetrics = async (): Promise<Metric[]> => {
    const response = await axios.get(BASE_URL_METRICS);

    return response.data;
};

export const createMetric = async (metric: LogicalMetric): Promise<Metric> => {
    const response = await axios.post(BASE_URL_METRICS, [metric]);

    return response.data[0];
};

export const updateMetric = async (metric: LogicalMetric): Promise<Metric> => {
    const response = await axios.put(BASE_URL_METRICS, [metric]);

    return response.data[0];
};

export const deleteMetric = async (id: number): Promise<Metric> => {
    const response = await axios.delete(`${BASE_URL_METRICS}/${id}`);

    return response.data;
};
