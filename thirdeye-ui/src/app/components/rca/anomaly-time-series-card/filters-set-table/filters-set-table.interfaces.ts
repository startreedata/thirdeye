import { AnomalyFilterOption } from "../../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface FiltersSetTableProps {
    timeSeriesFiltersSet: AnomalyFilterOption[][];
    onRemoveBtnClick: (idx: number) => void;
}
