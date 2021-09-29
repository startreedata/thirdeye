import { LogicalMetric, Metric } from "../../dto/metric.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface FetchMetric extends ActionHook {
    metric: Metric | null;
    fetchMetric: (id: number) => Promise<void>;
}

export interface FetchAllMetrics extends ActionHook {
    metrics: Metric[] | null;
    fetchAllMetrics: () => Promise<void>;
}

export interface CreateMetric extends ActionHook {
    createMetric: (metric: LogicalMetric) => Promise<void>;
}

export interface UpdateMetric extends ActionHook {
    updateMetric: (metric: LogicalMetric) => Promise<void>;
}

export interface DeleteMetric extends ActionHook {
    deleteMetric: (id: number) => Promise<void>;
}
