import { ScaleOrdinal } from "d3-scale";
import { AnomalyFilterOption } from "../../../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { Series } from "../../../../visualizations/time-series-chart/time-series-chart.interfaces";

export interface FilteredTimeSeriesProps {
    filterSet: AnomalyFilterOption[];
    onRemoveBtnClick: () => void;
    onCheckBoxClick: () => void;
    series: Series[];
    colorScale: ScaleOrdinal<string, string, never>;
}
