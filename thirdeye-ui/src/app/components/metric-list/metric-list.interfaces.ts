import { UiMetric } from "../../rest/dto/ui-metric.interfaces";

export interface MetricListProps {
    hideSearchBar?: boolean;
    uiMetrics: UiMetric[] | null;
    onDelete?: (metricsCardData: UiMetric) => void;
}
