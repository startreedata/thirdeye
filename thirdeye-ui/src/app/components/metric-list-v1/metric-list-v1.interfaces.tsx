import { UiMetric } from "../../rest/dto/ui-metric.interfaces";

export interface MetricListV1Props {
    metrics: UiMetric[] | null;
    onDelete?: (uiMetrics: UiMetric[]) => void;
}
