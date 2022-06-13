import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { Event } from "../../../rest/dto/event.interfaces";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface AnomalyTimeSeriesCardProps {
    anomaly: Anomaly | null;
    events: Event[];
    timeSeriesFiltersSet: AnomalyFilterOption[][];
    onRemoveBtnClick: (idx: number) => void;
    isLoading: boolean;
}
