import { useHTTPAction } from "../create-rest-action";
import { Metric } from "../dto/metric.interfaces";
import { GetAllMetricsParams, GetMetrics } from "./metrics.interface";
import { getAllMetrics } from "./metrics.rest";

export const useGetMetrics = (): GetMetrics => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Metric[]>(getAllMetrics);

    const getMetrics = (
        params: GetAllMetricsParams = {}
    ): Promise<Metric[] | undefined> => {
        return makeRequest(params);
    };

    return { metrics: data, getMetrics, status, errorMessages };
};
