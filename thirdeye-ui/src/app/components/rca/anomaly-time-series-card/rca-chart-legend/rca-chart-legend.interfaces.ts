import { Event } from "../../../../rest/dto/event.interfaces";
import { LegendProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface RCAChartLegendProps extends LegendProps {
    timeSeriesFiltersSet: AnomalyFilterOption[][];
    onRemoveBtnClick: (idx: number) => void;
    onEventSelectionChange: (events: Event[]) => void;
}
