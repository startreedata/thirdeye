import { UiMetric } from "../../rest/dto/ui-metric.interfaces";

export interface MetricListProps {
    hideSearchBar?: boolean;
    metrics: UiMetric[] | null;
    onDelete?: (uiMetric: UiMetric) => void;
}
