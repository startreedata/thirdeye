import { ActionHook } from "../actions.interfaces";
import { Metric } from "../dto/metric.interfaces";

export interface GetAllMetricsParams {
    datasetId?: number;
}
export interface GetMetrics extends ActionHook {
    metrics: Metric[] | null;
    getMetrics: (params?: GetAllMetricsParams) => Promise<Metric[] | undefined>;
}
