import axios from "axios";
import { LogicalMetric, Metric } from "../dto/metric.interfaces";
import { GetAllMetricsParams } from "./metrics.interface";

const BASE_URL_METRICS = "/api/metrics";

export const getMetric = async (id: number): Promise<Metric> => {
    const response = await axios.get(`${BASE_URL_METRICS}/${id}`);

    return response.data;
};

export const getAllMetrics = async (
    params: GetAllMetricsParams = {}
): Promise<Metric[]> => {
    let url = BASE_URL_METRICS;

    if (params.datasetId) {
        const urlParams = new URLSearchParams([
            ["dataset.id", params.datasetId.toString()],
        ]);
        url += `?${urlParams.toString()}`;
    }

    const response = await axios.get(url);

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
