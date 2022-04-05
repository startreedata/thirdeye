import { Series } from "../time-series-chart.interfaces";

export interface LegendProps {
    series: Series[];
    onSeriesClick?: (idx: number) => void;
    colorScale: any;
}
