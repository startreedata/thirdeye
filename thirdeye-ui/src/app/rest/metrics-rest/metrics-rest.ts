import axios from "axios";
import { Metric } from "../dto/metric.interfaces";

const BASE_URL_METRICS = "/api/metrics";

export const getMetrics = async (id: number): Promise<Metric> => {
    const response = await axios.get(`${BASE_URL_METRICS}/${id}`);

    return response.data;
};

export const getAllMetrics = async (): Promise<Metric[]> => {
    const response = await axios.get(`${BASE_URL_METRICS}`);

    return response.data;
};
