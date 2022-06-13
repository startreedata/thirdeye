import { UiMetric } from "../../../rest/dto/ui-metric.interfaces";

export interface MetricCardProps {
    metric: UiMetric | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiMetric: UiMetric) => void;
    onEdit?: (id: number) => void;
}
