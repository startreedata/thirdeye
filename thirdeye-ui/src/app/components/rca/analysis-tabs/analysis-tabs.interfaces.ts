import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface AnalysisTabsProps {
    anomalyId: number;
    anomaly: Anomaly;
    onAddFilterSetClick: (filters: AnomalyFilterOption[]) => void;
    chartTimeSeriesFilterSet: AnomalyFilterOption[][];
}
