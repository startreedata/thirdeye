import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface AnomalyTimeSeriesCardProps {
    anomaly: Anomaly;
    timeSeriesFiltersSet: AnomalyFilterOption[][];
    onRemoveBtnClick: (idx: number) => void;
}
