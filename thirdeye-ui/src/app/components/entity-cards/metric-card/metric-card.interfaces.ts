import { UiMetric } from "../../../rest/dto/ui-metric.interfaces";

export interface MetricCardProps {
    uiMetric: UiMetric;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiMetric: UiMetric) => void;
}
