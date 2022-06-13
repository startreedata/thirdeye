import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { Event } from "../../../rest/dto/event.interfaces";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface AnalysisTabsProps {
    anomalyId: number;
    anomaly: Anomaly | null;
    onAddFilterSetClick: (filters: AnomalyFilterOption[]) => void;
    chartTimeSeriesFilterSet: AnomalyFilterOption[][];
    selectedEvents: Event[];
    onEventSelectionChange: (events: Event[]) => void;
    isLoading: boolean;
}
